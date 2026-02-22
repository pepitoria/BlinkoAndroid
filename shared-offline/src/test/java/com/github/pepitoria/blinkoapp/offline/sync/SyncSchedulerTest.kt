package com.github.pepitoria.blinkoapp.offline.sync

import android.content.Context
import com.github.pepitoria.blinkoapp.offline.connectivity.ServerReachabilityMonitor
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
  private lateinit var serverReachabilityMonitor: ServerReachabilityMonitor
  private lateinit var syncQueueManager: SyncQueueManager
  private lateinit var syncScheduler: SyncScheduler
  private lateinit var isOnlineFlow: MutableStateFlow<Boolean>

  @BeforeEach
  fun setup() {
    context = mockk(relaxed = true)
    serverReachabilityMonitor = mockk()
    syncQueueManager = mockk()

    isOnlineFlow = MutableStateFlow(false)
    every { serverReachabilityMonitor.isOnline } returns isOnlineFlow

    mockkObject(SyncWorker.Companion)
    every { SyncWorker.enqueue(any()) } returns Unit

    syncScheduler = SyncScheduler(context, serverReachabilityMonitor, syncQueueManager)
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(SyncWorker.Companion)
  }

  @Test
  fun `startObserving triggers sync when server becomes reachable and has pending operations`() = runBlocking {
    coEvery { syncQueueManager.hasPendingOperations() } returns true

    syncScheduler.startObserving()

    // Simulate server becoming reachable
    isOnlineFlow.value = true
    // Wait for coroutine to process (real time)
    Thread.sleep(200)

    coVerify { syncQueueManager.hasPendingOperations() }
    io.mockk.verify { SyncWorker.enqueue(context) }
  }

  @Test
  fun `startObserving does not trigger sync when no pending operations`() = runBlocking {
    coEvery { syncQueueManager.hasPendingOperations() } returns false

    syncScheduler.startObserving()

    // Simulate server becoming reachable
    isOnlineFlow.value = true
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

    // Simulate server becoming reachable
    isOnlineFlow.value = true
    Thread.sleep(200)

    // Should only trigger once despite multiple startObserving calls
    coVerify(exactly = 1) { syncQueueManager.hasPendingOperations() }
  }
}
