package com.github.pepitoria.blinkoapp.offline.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.github.pepitoria.blinkoapp.offline.connectivity.ServerReachabilityMonitor
import com.github.pepitoria.blinkoapp.offline.data.db.dao.NoteDao
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncOperation
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val syncQueueManager: SyncQueueManager,
  private val noteDao: NoteDao,
  private val syncExecutor: SyncExecutor,
  private val serverReachabilityMonitor: ServerReachabilityMonitor,
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    Timber.d("SyncWorker started")

    if (!syncQueueManager.hasPendingOperations()) {
      Timber.d("No pending operations, exiting")
      return Result.success()
    }

    if (!serverReachabilityMonitor.shouldAttemptServerCall()) {
      Timber.d("Server not reachable, will retry later")
      return Result.retry()
    }

    var hasFailures = false
    var processedCount = 0
    val maxOperations = 50 // Limit operations per run

    while (processedCount < maxOperations) {
      val operation = syncQueueManager.getNextPendingOperation() ?: break

      if (operation.retryCount >= MAX_RETRIES) {
        Timber.w("Operation ${operation.queueId} exceeded max retries, skipping")
        syncQueueManager.markOperationComplete(operation.queueId)
        continue
      }

      val result = try {
        val syncOp = SyncOperation.fromValue(operation.operation)
        val payload = SyncPayload.fromJson(operation.payload)

        when (syncOp) {
          SyncOperation.CREATE -> syncExecutor.executeCreate(operation.noteLocalId, payload)
          SyncOperation.UPDATE -> syncExecutor.executeUpdate(operation.noteLocalId, payload)
          SyncOperation.DELETE -> syncExecutor.executeDelete(operation.noteLocalId, operation.noteServerId)
        }
      } catch (e: Exception) {
        Timber.e(e, "Error executing sync operation ${operation.queueId}")
        SyncResult.Failure(e.message ?: "Unknown error")
      }

      when (result) {
        is SyncResult.Success -> {
          Timber.d("Operation ${operation.queueId} completed successfully")
          syncQueueManager.markOperationComplete(operation.queueId)

          result.serverId?.let { serverId ->
            noteDao.updateAfterSync(
              localId = operation.noteLocalId,
              serverId = serverId,
              serverUpdatedAt = result.serverUpdatedAt,
            )
          }
        }
        is SyncResult.Conflict -> {
          Timber.w("Conflict detected for operation ${operation.queueId}")
          noteDao.updateSyncStatus(operation.noteLocalId, SyncStatus.CONFLICT.value)
          syncQueueManager.markOperationComplete(operation.queueId)
        }
        is SyncResult.Failure -> {
          Timber.w("Operation ${operation.queueId} failed: ${result.error}")
          syncQueueManager.markOperationFailed(operation.queueId, result.error)
          hasFailures = true
        }
        is SyncResult.Deleted -> {
          Timber.d("Note ${operation.noteLocalId} deleted successfully")
          noteDao.deleteByLocalId(operation.noteLocalId)
          syncQueueManager.markOperationComplete(operation.queueId)
        }
      }

      processedCount++
    }

    return if (hasFailures) {
      Timber.d("SyncWorker completed with failures, will retry")
      Result.retry()
    } else {
      Timber.d("SyncWorker completed successfully, processed $processedCount operations")
      Result.success()
    }
  }

  companion object {
    const val WORK_NAME = "blinko_sync_worker"
    private const val MAX_RETRIES = 5

    fun enqueue(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(constraints)
        .setBackoffCriteria(
          BackoffPolicy.EXPONENTIAL,
          30,
          TimeUnit.SECONDS,
        )
        .build()

      WorkManager.getInstance(context)
        .enqueueUniqueWork(
          WORK_NAME,
          ExistingWorkPolicy.REPLACE,
          workRequest,
        )

      Timber.d("SyncWorker enqueued")
    }

    fun cancel(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
      Timber.d("SyncWorker cancelled")
    }
  }
}

sealed class SyncResult {
  data class Success(
    val serverId: Int? = null,
    val serverUpdatedAt: String? = null,
  ) : SyncResult()

  data object Deleted : SyncResult()

  data class Conflict(val serverUpdatedAt: String?) : SyncResult()

  data class Failure(val error: String) : SyncResult()
}
