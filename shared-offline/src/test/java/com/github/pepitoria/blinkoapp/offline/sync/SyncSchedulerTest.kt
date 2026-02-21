package com.github.pepitoria.blinkoapp.offline.sync

import android.content.Context
import com.github.pepitoria.blinkoapp.offline.connectivity.ConnectivityMonitor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyncSchedulerTest {

  private lateinit var context: Context
  private lateinit var connectivityMonitor: ConnectivityMonitor
  private lateinit var syncQueueManager: SyncQueueManager
  private lateinit var syncScheduler: SyncScheduler
  private lateinit var isConnectedFlow: MutableStateFlow<Boolean>

  @BeforeEach
  fun setup() {
    context = mockk(relaxed = true)
    connectivityMonitor = mockk()
    syncQueueManager = mockk()

    isConnectedFlow = MutableStateFlow(false)
    every { connectivityMonitor.isConnected } returns isConnectedFlow

    mockkObject(SyncWorker.Companion)
    every { SyncWorker.enqueue(any()) } returns Unit

    syncScheduler = SyncScheduler(context, connectivityMonitor, syncQueueManager)
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(SyncWorker.Companion)
  }

  @Test
  fun `startObserving triggers sync when connectivity restored and has pending operations`() = runBlocking {
    coEvery { syncQueueManager.hasPendingOperations() } returns true

    syncScheduler.startObserving()

    // Simulate going online
    isConnectedFlow.value = true
    // Wait for coroutine to process (real time)
    Thread.sleep(200)

    coVerify { syncQueueManager.hasPendingOperations() }
    io.mockk.verify { SyncWorker.enqueue(context) }
  }

  @Test
  fun `startObserving does not trigger sync when no pending operations`() = runBlocking {
    coEvery { syncQueueManager.hasPendingOperations() } returns false

    syncScheduler.startObserving()

    // Simulate going online
    isConnectedFlow.value = true
    Thread.sleep(200)

    coVerify { syncQueueManager.hasPendingOperations() }
    io.mockk.verify(exactly = 0) { SyncWorker.enqueue(any()) }
  }

  @Test
  fun `startObserving does not trigger sync when offline`() = runBlocking {
    coEvery { syncQueueManager.hasPendingOperations() } returns true

    syncScheduler.startObserving()

    // Stay offline (already false)
    Thread.sleep(200)

    coVerify(exactly = 0) { syncQueueManager.hasPendingOperations() }
    io.mockk.verify(exactly = 0) { SyncWorker.enqueue(any()) }
  }

  @Test
  fun `triggerSync triggers sync when has pending operations`() = runBlocking {
    coEvery { syncQueueManager.hasPendingOperations() } returns true

    syncScheduler.triggerSync()
    Thread.sleep(200)

    coVerify { syncQueueManager.hasPendingOperations() }
    io.mockk.verify { SyncWorker.enqueue(context) }
  }

  @Test
  fun `triggerSync does not trigger sync when no pending operations`() = runBlocking {
    coEvery { syncQueueManager.hasPendingOperations() } returns false

    syncScheduler.triggerSync()
    Thread.sleep(200)

    coVerify { syncQueueManager.hasPendingOperations() }
    io.mockk.verify(exactly = 0) { SyncWorker.enqueue(any()) }
  }

  @Test
  fun `startObserving only starts once`() = runBlocking {
    coEvery { syncQueueManager.hasPendingOperations() } returns true

    syncScheduler.startObserving()
    syncScheduler.startObserving() // Second call should be ignored

    // Simulate going online
    isConnectedFlow.value = true
    Thread.sleep(200)

    // Should only trigger once despite multiple startObserving calls
    coVerify(exactly = 1) { syncQueueManager.hasPendingOperations() }
  }
}
