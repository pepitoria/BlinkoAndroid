# Offline Mode with Sync Implementation Plan

## Executive Summary

This document outlines the implementation plan for adding offline-first functionality to BlinkoAndroid, allowing users to create, edit, and delete notes while offline with automatic synchronization when connectivity is restored.

---

## Table of Contents

1. [Current Architecture Analysis](#current-architecture-analysis)
2. [Proposed Architecture](#proposed-architecture)
3. [Database Schema Design](#database-schema-design)
4. [Sync Queue Mechanism](#sync-queue-mechanism)
5. [Conflict Resolution Strategy](#conflict-resolution-strategy)
6. [Connectivity Monitoring](#connectivity-monitoring)
7. [Repository Layer Changes](#repository-layer-changes)
8. [UI Integration](#ui-integration)
9. [Implementation Phases](#implementation-phases)
10. [Testing Strategy](#testing-strategy)
11. [File Reference](#file-reference)

---

## Current Architecture Analysis

### Data Layer

| Component | Technology | Notes |
|-----------|------------|-------|
| HTTP Client | Retrofit 3.0.0 + OkHttp 5.3.2 | All endpoints use POST |
| Local Storage | SharedPreferences | Only stores session/settings |
| Serialization | Gson | For JSON conversion |

**Key Finding:** No persistent database exists for notes. All data is fetched from server on each screen load.

### State Management

- **Pattern:** MVVM with StateFlow/MutableStateFlow
- **DI:** Hilt (Dagger 2)
- **Async:** Kotlin Coroutines with viewModelScope

### Connectivity Handling

Current implementation in `NotesApiClientNetImpl.kt`:
```kotlin
override fun isConnected(): Boolean {
    val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}
```

**Issues:**
- Uses deprecated `activeNetworkInfo` API
- Point-in-time check, not reactive
- No automatic retry when connection restored

### CRUD Operations

| Operation | Current Flow | Offline Behavior |
|-----------|--------------|------------------|
| CREATE | API call -> UI update | Returns error |
| READ | API call -> display | Returns error |
| UPDATE | API call -> UI update | Returns error |
| DELETE | API call -> remove from list | Returns error |

---

## Proposed Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION LAYER                             │
│  ┌─────────────────────┐  ┌──────────────────┐  ┌────────────────────┐ │
│  │ NoteListScreenVM    │  │ NoteEditScreenVM │  │ SyncStatusIndicator│ │
│  │ (observes sync)     │  │ (offline-aware)  │  │ (shows sync state) │ │
│  └─────────────────────┘  └──────────────────┘  └────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            DOMAIN LAYER                                  │
│  ┌────────────────────┐  ┌──────────────────┐  ┌────────────────────┐  │
│  │ NoteListUseCase    │  │ NoteUpsertUseCase│  │ SyncUseCase        │  │
│  │ (returns cached)   │  │ (queues offline) │  │ (orchestrates sync)│  │
│  └────────────────────┘  └──────────────────┘  └────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            DATA LAYER                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    OfflineFirstNoteRepository                    │   │
│  │  ┌────────────┐  ┌──────────────┐  ┌─────────────────────────┐ │   │
│  │  │LocalSource │  │ RemoteSource │  │ SyncQueueManager        │ │   │
│  │  │(Room DB)   │  │ (Retrofit)   │  │ (pending operations)    │ │   │
│  │  └────────────┘  └──────────────┘  └─────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌────────────────────────┐  ┌──────────────────────────────────────┐  │
│  │ ConnectivityMonitor   │  │ SyncWorker (WorkManager)              │  │
│  │ (reactive Flow)       │  │ (background sync)                      │  │
│  └────────────────────────┘  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Database | Room | Compile-time verification, Flow integration, migration support |
| Background Sync | WorkManager | Battery-aware, survives app restarts, network constraints |
| Sync Strategy | Offline-first | Read local immediately, write local first then sync |
| Conflict Detection | Timestamp-based | Uses server's `updatedAt` field |

---

## Database Schema Design

### Dependencies to Add

Add to `gradle/libs.versions.toml`:

```toml
[versions]
room = "2.6.1"
workmanager = "2.9.0"

[libraries]
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-work-runtime = { module = "androidx.work:work-runtime-ktx", version.ref = "workmanager" }
```

### Entity: NoteEntity

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: Int,                    // Server ID
    val localId: String,                        // UUID for unsynced notes
    val content: String,
    val type: Int,                              // 0=BLINKO, 1=NOTE, 2=TODO
    val isArchived: Boolean,
    val isRecycle: Boolean = false,
    val isShare: Boolean = false,
    val isTop: Boolean = false,
    val isReviewed: Boolean = false,
    val sharePassword: String? = null,
    val accountId: Int? = null,
    val createdAt: String,                      // ISO8601 from server
    val updatedAt: String,                      // ISO8601 from server
    val serverUpdatedAt: String?,               // Last known server version
    val localUpdatedAt: Long,                   // Local modification (epoch ms)
    val syncStatus: Int,                        // See SyncStatus enum
)
```

### Entity: SyncQueueEntity

```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val queueId: Long = 0,
    val noteLocalId: String,                    // Links to NoteEntity.localId
    val noteServerId: Int?,                     // Server ID if exists
    val operation: Int,                         // 0=CREATE, 1=UPDATE, 2=DELETE
    val payload: String,                        // JSON serialized request
    val createdAt: Long,                        // When queued (epoch ms)
    val retryCount: Int = 0,                    // Sync attempts
    val lastError: String? = null,              // Last failure message
)
```

### Entity: TagEntity

```kotlin
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val icon: String? = null,
    val parentId: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
```

### Sync Status Enum

```kotlin
enum class SyncStatus(val value: Int) {
    SYNCED(0),           // Fully synchronized
    PENDING_CREATE(1),   // Created locally, not on server
    PENDING_UPDATE(2),   // Modified locally, needs sync
    PENDING_DELETE(3),   // Marked for deletion
    CONFLICT(4),         // Server and local differ
}

enum class SyncOperation(val value: Int) {
    CREATE(0),
    UPDATE(1),
    DELETE(2),
}
```

### DAOs

#### NoteDao

```kotlin
@Dao
interface NoteDao {
    // === Queries ===

    @Query("SELECT * FROM notes WHERE syncStatus != 3 ORDER BY localUpdatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes
        WHERE type = :type AND isArchived = :archived AND syncStatus != 3
        ORDER BY localUpdatedAt DESC
    """)
    fun getNotesByTypeAndArchived(type: Int, archived: Boolean): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): NoteEntity?

    @Query("SELECT * FROM notes WHERE localId = :localId")
    suspend fun getNoteByLocalId(localId: String): NoteEntity?

    @Query("""
        SELECT * FROM notes
        WHERE content LIKE '%' || :searchTerm || '%' AND syncStatus != 3
    """)
    fun searchNotes(searchTerm: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE syncStatus IN (1, 2, 3)")
    suspend fun getPendingSyncNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE syncStatus = 4")
    fun getConflictNotes(): Flow<List<NoteEntity>>

    // === Mutations ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: Int)

    @Query("UPDATE notes SET syncStatus = :status WHERE localId = :localId")
    suspend fun updateSyncStatusByLocalId(localId: String, status: Int)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Int)

    @Query("DELETE FROM notes WHERE localId = :localId")
    suspend fun deleteNoteByLocalId(localId: String)

    // === Sync Helpers ===

    @Query("SELECT MAX(updatedAt) FROM notes WHERE syncStatus = 0")
    suspend fun getLatestSyncedTimestamp(): String?

    @Query("SELECT COUNT(*) FROM notes WHERE syncStatus IN (1, 2, 3)")
    fun getPendingCount(): Flow<Int>
}
```

#### SyncQueueDao

```kotlin
@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getAllPendingOperations(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE noteLocalId = :localId")
    suspend fun getOperationsForNote(localId: String): List<SyncQueueEntity>

    @Insert
    suspend fun insertOperation(operation: SyncQueueEntity)

    @Update
    suspend fun updateOperation(operation: SyncQueueEntity)

    @Delete
    suspend fun deleteOperation(operation: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE queueId = :queueId")
    suspend fun deleteOperationById(queueId: Long)

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getPendingOperationsCount(): Flow<Int>
}
```

#### TagDao

```kotlin
@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Query("DELETE FROM tags")
    suspend fun clearAllTags()
}
```

### Database

```kotlin
@Database(
    entities = [NoteEntity::class, SyncQueueEntity::class, TagEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BlinkoDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun tagDao(): TagDao
}
```

---

## Sync Queue Mechanism

### Queue Manager

The `SyncQueueManager` handles queueing operations with intelligent merging:

```kotlin
class SyncQueueManager @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val noteDao: NoteDao,
    private val gson: Gson,
) {
    suspend fun queueCreate(note: NoteEntity) {
        val payload = gson.toJson(note.toUpsertRequest())
        syncQueueDao.insertOperation(
            SyncQueueEntity(
                noteLocalId = note.localId,
                noteServerId = null,
                operation = SyncOperation.CREATE.value,
                payload = payload,
                createdAt = System.currentTimeMillis(),
            )
        )
        noteDao.updateSyncStatusByLocalId(note.localId, SyncStatus.PENDING_CREATE.value)
    }

    suspend fun queueUpdate(note: NoteEntity) {
        val existingOps = syncQueueDao.getOperationsForNote(note.localId)
        val hasPendingCreate = existingOps.any { it.operation == SyncOperation.CREATE.value }

        if (hasPendingCreate) {
            // Merge: update the CREATE payload instead of adding UPDATE
            existingOps.filter { it.operation == SyncOperation.CREATE.value }.forEach { op ->
                syncQueueDao.updateOperation(
                    op.copy(payload = gson.toJson(note.toUpsertRequest()))
                )
            }
        } else {
            // Replace any existing UPDATE with new one
            existingOps.filter { it.operation == SyncOperation.UPDATE.value }
                .forEach { syncQueueDao.deleteOperation(it) }

            syncQueueDao.insertOperation(
                SyncQueueEntity(
                    noteLocalId = note.localId,
                    noteServerId = note.id,
                    operation = SyncOperation.UPDATE.value,
                    payload = gson.toJson(note.toUpsertRequest()),
                    createdAt = System.currentTimeMillis(),
                )
            )
        }
        noteDao.updateSyncStatusByLocalId(note.localId, SyncStatus.PENDING_UPDATE.value)
    }

    suspend fun queueDelete(note: NoteEntity) {
        val existingOps = syncQueueDao.getOperationsForNote(note.localId)
        val hasPendingCreate = existingOps.any { it.operation == SyncOperation.CREATE.value }

        if (hasPendingCreate) {
            // Note never synced - just remove from queue and delete locally
            existingOps.forEach { syncQueueDao.deleteOperation(it) }
            noteDao.deleteNoteByLocalId(note.localId)
        } else {
            // Clear existing ops and add DELETE
            existingOps.forEach { syncQueueDao.deleteOperation(it) }

            syncQueueDao.insertOperation(
                SyncQueueEntity(
                    noteLocalId = note.localId,
                    noteServerId = note.id,
                    operation = SyncOperation.DELETE.value,
                    payload = gson.toJson(DeleteNoteRequest(ids = listOf(note.id))),
                    createdAt = System.currentTimeMillis(),
                )
            )
            noteDao.updateSyncStatusByLocalId(note.localId, SyncStatus.PENDING_DELETE.value)
        }
    }
}
```

### Queue Merge Logic

| Existing Op | New Op | Result |
|-------------|--------|--------|
| CREATE | UPDATE | Update CREATE payload |
| CREATE | DELETE | Remove from queue, delete locally |
| UPDATE | UPDATE | Replace UPDATE payload |
| UPDATE | DELETE | Remove UPDATE, add DELETE |
| DELETE | - | No further ops allowed |

### Sync Worker (WorkManager)

```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @Inject lateinit var syncQueueManager: SyncQueueManager
    @Inject lateinit var notesApiClient: NotesApiClient
    @Inject lateinit var authRepository: AuthenticationRepository

    override suspend fun doWork(): Result {
        val session = authRepository.getSession() ?: return Result.failure()

        val operations = syncQueueManager.getAllPendingOperations()
        var hasFailures = false

        for (op in operations) {
            try {
                when (SyncOperation.values().first { it.value == op.operation }) {
                    SyncOperation.CREATE -> processCreate(op, session)
                    SyncOperation.UPDATE -> processUpdate(op, session)
                    SyncOperation.DELETE -> processDelete(op, session)
                }
                syncQueueManager.markCompleted(op.queueId)
            } catch (e: Exception) {
                syncQueueManager.markFailed(op.queueId, e.message)
                hasFailures = true
            }
        }

        return if (hasFailures) Result.retry() else Result.success()
    }

    companion object {
        fun enqueueImmediate(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork("sync", ExistingWorkPolicy.REPLACE, request)
        }

        fun enqueueOnConnectivity(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork("sync", ExistingWorkPolicy.KEEP, request)
        }
    }
}
```

---

## Conflict Resolution Strategy

### Detection

Conflicts are detected during UPDATE sync by comparing timestamps:

```kotlin
private suspend fun processUpdate(op: SyncQueueEntity, session: BlinkoSession) {
    val note = noteDao.getNoteByLocalId(op.noteLocalId) ?: return

    // Fetch current server version
    val serverResult = notesApiClient.noteListByIds(
        session.url, session.token,
        NoteListByIdsRequest(ids = listOf(note.id))
    )

    when (serverResult) {
        is ApiResult.ApiSuccess -> {
            val serverNote = serverResult.value.firstOrNull()
            if (serverNote != null && serverNote.updatedAt != note.serverUpdatedAt) {
                // CONFLICT: Server was modified since we last synced
                noteDao.updateSyncStatus(note.id, SyncStatus.CONFLICT.value)
                throw ConflictException(note, serverNote)
            }
        }
        is ApiResult.ApiErrorResponse -> throw SyncException(serverResult.message)
    }

    // No conflict - proceed with update
    val result = notesApiClient.upsertNote(session.url, session.token, note.toUpsertRequest())
    // ... handle result
}
```

### Resolution Options

```kotlin
sealed class ConflictResolution {
    object KeepLocal : ConflictResolution()   // Overwrite server with local
    object KeepServer : ConflictResolution()  // Discard local changes
    data class Merge(val content: String) : ConflictResolution()  // Manual merge
}

class ConflictResolver @Inject constructor(
    private val noteDao: NoteDao,
    private val syncQueueManager: SyncQueueManager,
) {
    suspend fun resolve(conflict: ConflictInfo, resolution: ConflictResolution) {
        when (resolution) {
            is ConflictResolution.KeepLocal -> {
                // Update serverUpdatedAt and re-queue
                val note = conflict.localNote.copy(
                    serverUpdatedAt = conflict.serverNote.updatedAt,
                    syncStatus = SyncStatus.PENDING_UPDATE.value,
                )
                noteDao.updateNote(note)
                syncQueueManager.queueUpdate(note)
            }

            is ConflictResolution.KeepServer -> {
                // Replace local with server version
                val entity = conflict.serverNote.toNoteEntity(
                    localId = conflict.localNote.localId,
                    syncStatus = SyncStatus.SYNCED.value,
                )
                noteDao.updateNote(entity)
            }

            is ConflictResolution.Merge -> {
                val merged = conflict.localNote.copy(
                    content = resolution.content,
                    serverUpdatedAt = conflict.serverNote.updatedAt,
                    localUpdatedAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.PENDING_UPDATE.value,
                )
                noteDao.updateNote(merged)
                syncQueueManager.queueUpdate(merged)
            }
        }
    }
}
```

### User-Configurable Policy

```kotlin
enum class AutoConflictPolicy {
    PREFER_LOCAL,      // Always keep local changes
    PREFER_SERVER,     // Always keep server version
    PREFER_NEWEST,     // Compare timestamps, keep newest
    ALWAYS_ASK,        // Always show conflict dialog (default)
}
```

---

## Connectivity Monitoring

Replace deprecated point-in-time check with reactive monitoring:

```kotlin
@Singleton
class ConnectivityMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow(checkCurrentConnectivity())
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionType = MutableStateFlow(getCurrentConnectionType())
    val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            _connectionType.value = getCurrentConnectionType()
            // Trigger sync when connectivity restored
            SyncWorker.enqueueImmediate(context)
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
            _connectionType.value = ConnectionType.NONE
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            _connectionType.value = getCurrentConnectionType()
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    private fun checkCurrentConnectivity(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getCurrentConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return ConnectionType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.OTHER
        }
    }

    fun cleanup() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

enum class ConnectionType {
    WIFI, CELLULAR, ETHERNET, OTHER, NONE
}
```

---

## Repository Layer Changes

### Updated NoteRepository Interface

```kotlin
interface NoteRepository {
    // Existing methods (keep for compatibility)
    suspend fun list(url: String, token: String, type: Int, archived: Boolean = false): BlinkoResult<List<BlinkoNote>>
    suspend fun search(url: String, token: String, searchTerm: String): BlinkoResult<List<BlinkoNote>>
    suspend fun listByIds(url: String, token: String, id: Int): BlinkoResult<List<BlinkoNote>>
    suspend fun listByIds(url: String, token: String, ids: List<Int>): BlinkoResult<List<BlinkoNote>>
    suspend fun upsertNote(blinkoNote: BlinkoNote): BlinkoResult<BlinkoNote>
    suspend fun delete(url: String, token: String, id: Int): BlinkoResult<Boolean>

    // New Flow-based methods for offline support
    fun listAsFlow(type: Int, archived: Boolean): Flow<List<BlinkoNote>>
    fun searchAsFlow(searchTerm: String): Flow<List<BlinkoNote>>
    val pendingSyncCount: Flow<Int>
    val conflicts: Flow<List<BlinkoNote>>

    // Sync control
    suspend fun refreshFromServer(): BlinkoResult<Unit>
    suspend fun forceSyncNow(): BlinkoResult<Unit>
}
```

### OfflineFirstNoteRepository Implementation

```kotlin
class OfflineFirstNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val remoteApiClient: NotesApiClient,
    private val syncQueueManager: SyncQueueManager,
    private val connectivityMonitor: ConnectivityMonitor,
    private val authRepository: AuthenticationRepository,
    @ApplicationContext private val context: Context,
) : NoteRepository {

    override val pendingSyncCount: Flow<Int> = noteDao.getPendingCount()

    override val conflicts: Flow<List<BlinkoNote>> = noteDao.getConflictNotes()
        .map { entities -> entities.map { it.toBlinkoNote() } }

    // === READ Operations (Local-First) ===

    override suspend fun list(
        url: String,
        token: String,
        type: Int,
        archived: Boolean,
    ): BlinkoResult<List<BlinkoNote>> {
        return BlinkoResult.Success(
            noteDao.getNotesByTypeAndArchived(type, archived)
                .first()
                .map { it.toBlinkoNote() }
        )
    }

    override fun listAsFlow(type: Int, archived: Boolean): Flow<List<BlinkoNote>> {
        return noteDao.getNotesByTypeAndArchived(type, archived)
            .map { entities -> entities.map { it.toBlinkoNote() } }
    }

    override fun searchAsFlow(searchTerm: String): Flow<List<BlinkoNote>> {
        return noteDao.searchNotes(searchTerm)
            .map { entities -> entities.map { it.toBlinkoNote() } }
    }

    // === WRITE Operations (Local + Queue) ===

    override suspend fun upsertNote(blinkoNote: BlinkoNote): BlinkoResult<BlinkoNote> {
        val localId = UUID.randomUUID().toString()
        val isCreate = blinkoNote.id == null
        val now = System.currentTimeMillis()

        val noteEntity = NoteEntity(
            id = blinkoNote.id ?: -now.toInt(),  // Temporary negative ID for new notes
            localId = localId,
            content = blinkoNote.content,
            type = blinkoNote.type.value,
            isArchived = blinkoNote.isArchived,
            createdAt = Instant.now().toString(),
            updatedAt = Instant.now().toString(),
            serverUpdatedAt = null,
            localUpdatedAt = now,
            syncStatus = if (isCreate) SyncStatus.PENDING_CREATE.value
                        else SyncStatus.PENDING_UPDATE.value,
        )

        // Save locally first
        noteDao.insertNote(noteEntity)

        // Queue for sync
        if (isCreate) {
            syncQueueManager.queueCreate(noteEntity)
        } else {
            syncQueueManager.queueUpdate(noteEntity)
        }

        // Trigger immediate sync if online
        if (connectivityMonitor.isConnected.value) {
            SyncWorker.enqueueImmediate(context)
        }

        return BlinkoResult.Success(noteEntity.toBlinkoNote())
    }

    override suspend fun delete(url: String, token: String, id: Int): BlinkoResult<Boolean> {
        val note = noteDao.getNoteById(id) ?: return BlinkoResult.Error.NOTFOUND

        syncQueueManager.queueDelete(note)

        if (connectivityMonitor.isConnected.value) {
            SyncWorker.enqueueImmediate(context)
        }

        return BlinkoResult.Success(true)
    }

    // === SYNC Operations ===

    override suspend fun refreshFromServer(): BlinkoResult<Unit> {
        if (!connectivityMonitor.isConnected.value) {
            return BlinkoResult.Error(-1, "No network connection")
        }

        val session = authRepository.getSession() ?: return BlinkoResult.Error.MISSING_USER_DATA

        val result = remoteApiClient.noteList(
            url = session.url,
            token = session.token,
            noteListRequest = NoteListRequest(),
        )

        return when (result) {
            is ApiResult.ApiSuccess -> {
                mergeServerNotes(result.value)
                BlinkoResult.Success(Unit)
            }
            is ApiResult.ApiErrorResponse -> {
                BlinkoResult.Error(result.code ?: -1, result.message ?: "Unknown error")
            }
        }
    }

    private suspend fun mergeServerNotes(serverNotes: List<NoteResponse>) {
        val pendingNotes = noteDao.getPendingSyncNotes()
        val pendingIds = pendingNotes.map { it.id }.toSet()

        // Only insert/update notes that don't have pending local changes
        val notesToInsert = serverNotes
            .filter { it.id !in pendingIds }
            .map { response ->
                response.toNoteEntity(
                    localId = UUID.randomUUID().toString(),
                    syncStatus = SyncStatus.SYNCED.value,
                )
            }

        noteDao.insertNotes(notesToInsert)
    }

    override suspend fun forceSyncNow(): BlinkoResult<Unit> {
        SyncWorker.enqueueImmediate(context)
        return BlinkoResult.Success(Unit)
    }
}
```

---

## UI Integration

### Extended BlinkoNote Model

```kotlin
data class BlinkoNote(
    val id: Int? = null,
    val content: String,
    val type: BlinkoNoteType,
    val isArchived: Boolean,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,  // NEW
    val localId: String? = null,                      // NEW
) {
    companion object {
        val EMPTY = BlinkoNote(
            content = "",
            type = BlinkoNoteType.BLINKO,
            isArchived = false,
        )
    }

    val isPending: Boolean
        get() = syncStatus in listOf(
            SyncStatus.PENDING_CREATE,
            SyncStatus.PENDING_UPDATE,
            SyncStatus.PENDING_DELETE
        )

    val hasConflict: Boolean
        get() = syncStatus == SyncStatus.CONFLICT
}
```

### SyncState Model

```kotlin
data class SyncState(
    val isOnline: Boolean,
    val connectionType: ConnectionType,
    val isSyncing: Boolean,
    val pendingCount: Int,
    val conflictCount: Int,
    val lastSyncTime: Long?,
    val lastError: String?,
)
```

### Updated ViewModel

```kotlin
@HiltViewModel
class NoteListScreenViewModel @Inject constructor(
    private val noteRepository: OfflineFirstNoteRepository,
    private val connectivityMonitor: ConnectivityMonitor,
    // ... existing dependencies
) : BlinkoViewModel() {

    private val _noteType = MutableStateFlow(BlinkoNoteType.BLINKO)
    val noteType = _noteType.asStateFlow()

    // Notes now come from Flow (reactive to DB changes)
    val notes: StateFlow<List<BlinkoNote>> = _noteType.flatMapLatest { type ->
        noteRepository.listAsFlow(type.value, archived = false)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Sync state
    val syncState: StateFlow<SyncState> = combine(
        connectivityMonitor.isConnected,
        connectivityMonitor.connectionType,
        noteRepository.pendingSyncCount,
        noteRepository.conflicts.map { it.size },
    ) { isOnline, connectionType, pendingCount, conflictCount ->
        SyncState(
            isOnline = isOnline,
            connectionType = connectionType,
            isSyncing = false,
            pendingCount = pendingCount,
            conflictCount = conflictCount,
            lastSyncTime = null,
            lastError = null,
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, SyncState(...))

    // Pull-to-refresh now triggers server sync
    fun refresh() {
        viewModelScope.launch {
            noteRepository.refreshFromServer()
        }
    }
}
```

### Sync Status Composables

```kotlin
@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Connection icon
        Icon(
            imageVector = if (syncState.isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
            contentDescription = if (syncState.isOnline) "Online" else "Offline",
            tint = if (syncState.isOnline) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp),
        )

        // Pending count badge
        if (syncState.pendingCount > 0) {
            Badge(
                containerColor = MaterialTheme.colorScheme.secondary,
            ) {
                Text("${syncState.pendingCount}")
            }
        }

        // Conflict warning
        if (syncState.conflictCount > 0) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "${syncState.conflictCount} conflicts",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
        }

        // Syncing indicator
        if (syncState.isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
fun NoteSyncStatusIcon(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier,
) {
    when (syncStatus) {
        SyncStatus.SYNCED -> { /* No indicator */ }

        SyncStatus.PENDING_CREATE,
        SyncStatus.PENDING_UPDATE -> {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "Pending sync",
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                modifier = modifier.size(16.dp),
            )
        }

        SyncStatus.PENDING_DELETE -> {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = "Pending delete",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                modifier = modifier.size(16.dp),
            )
        }

        SyncStatus.CONFLICT -> {
            Icon(
                imageVector = Icons.Default.SyncProblem,
                contentDescription = "Sync conflict",
                tint = MaterialTheme.colorScheme.error,
                modifier = modifier.size(16.dp),
            )
        }
    }
}
```

### Conflict Resolution Dialog

```kotlin
@Composable
fun ConflictResolutionDialog(
    localNote: BlinkoNote,
    serverNote: BlinkoNote,
    onResolve: (ConflictResolution) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync Conflict") },
        text = {
            Column {
                Text("This note was modified both locally and on the server.")

                Spacer(modifier = Modifier.height(16.dp))

                Text("Local version:", fontWeight = FontWeight.Bold)
                Text(localNote.content.take(100) + if (localNote.content.length > 100) "..." else "")

                Spacer(modifier = Modifier.height(8.dp))

                Text("Server version:", fontWeight = FontWeight.Bold)
                Text(serverNote.content.take(100) + if (serverNote.content.length > 100) "..." else "")
            }
        },
        confirmButton = {
            TextButton(onClick = { onResolve(ConflictResolution.KeepLocal) }) {
                Text("Keep Local")
            }
        },
        dismissButton = {
            TextButton(onClick = { onResolve(ConflictResolution.KeepServer) }) {
                Text("Keep Server")
            }
        },
    )
}
```

---

## Implementation Phases

### Phase 1: Foundation (New Module)

**Create `shared-offline/` module structure:**

```
shared-offline/
├── build.gradle.kts
└── src/main/java/com/github/pepitoria/blinkoapp/shared/offline/
    ├── database/
    │   ├── BlinkoDatabase.kt
    │   ├── Converters.kt
    │   ├── entity/
    │   │   ├── NoteEntity.kt
    │   │   ├── SyncQueueEntity.kt
    │   │   └── TagEntity.kt
    │   └── dao/
    │       ├── NoteDao.kt
    │       ├── SyncQueueDao.kt
    │       └── TagDao.kt
    ├── connectivity/
    │   └── ConnectivityMonitor.kt
    ├── sync/
    │   ├── SyncQueueManager.kt
    │   ├── SyncWorker.kt
    │   ├── SyncStatus.kt
    │   └── ConflictResolver.kt
    └── di/
        └── OfflineModule.kt
```

**Tasks:**
1. Create module with `build.gradle.kts` including Room, WorkManager deps
2. Define entities and DAOs
3. Create BlinkoDatabase
4. Add Hilt module for DI

### Phase 2: Connectivity & Sync Infrastructure

**Tasks:**
1. Implement `ConnectivityMonitor` with reactive Flow
2. Implement `SyncQueueManager` with queue logic
3. Implement `SyncWorker` with WorkManager
4. Wire up auto-sync on connectivity change

### Phase 3: Repository Layer

**Modify existing files:**
- `feature-notes/api/.../NoteRepository.kt` - Add Flow methods
- `feature-notes/implementation/.../di/NotesModule.kt` - Bind new impl

**Create new files:**
- `feature-notes/implementation/.../data/local/NoteLocalDataSource.kt`
- `feature-notes/implementation/.../data/repository/OfflineFirstNoteRepository.kt`
- `feature-notes/implementation/.../data/mapper/NoteEntityMapper.kt`

**Tasks:**
1. Extend `BlinkoNote` with sync fields
2. Create entity <-> domain mappers
3. Implement `OfflineFirstNoteRepository`
4. Update DI to use new repository

### Phase 4: UI Integration

**Modify existing files:**
- `feature-notes/implementation/.../presentation/NoteListScreenViewModel.kt`
- `feature-notes/implementation/.../presentation/NoteListScreenComposable.kt`
- `feature-notes/implementation/.../presentation/NoteEditScreenViewModel.kt`

**Create new files:**
- `shared-ui/src/.../sync/SyncStatusIndicator.kt`
- `shared-ui/src/.../sync/SyncState.kt`
- `feature-notes/implementation/.../presentation/conflict/ConflictResolutionDialog.kt`

**Tasks:**
1. Update ViewModels to use Flow-based data
2. Add sync state observation
3. Create sync status UI components
4. Add conflict resolution dialog

### Phase 5: Testing & Polish

**Tasks:**
1. Unit tests for DAOs, SyncQueueManager, Repository
2. Integration tests for sync flow
3. UI tests for indicators
4. Manual testing of offline scenarios

---

## Testing Strategy

### Unit Tests

```kotlin
// NoteDao tests
@Test
fun `insertNote and getNoteById returns same note`()

@Test
fun `getPendingSyncNotes returns only pending notes`()

@Test
fun `searchNotes filters by content`()

// SyncQueueManager tests
@Test
fun `queueCreate adds operation and updates status`()

@Test
fun `queueUpdate merges with existing create`()

@Test
fun `queueDelete removes pending create and deletes locally`()

// Repository tests
@Test
fun `list returns local data when offline`()

@Test
fun `upsertNote saves locally and queues for sync`()

@Test
fun `refreshFromServer merges without overwriting pending`()
```

### Integration Tests

```kotlin
@Test
fun `offline create syncs when connectivity restored`() {
    // 1. Simulate offline
    // 2. Create note
    // 3. Verify queued
    // 4. Simulate online
    // 5. Verify synced to server
}

@Test
fun `concurrent edits trigger conflict`() {
    // 1. Create note, sync
    // 2. Edit locally
    // 3. Simulate server edit
    // 4. Trigger sync
    // 5. Verify conflict state
}
```

### Manual Test Cases

| Scenario | Steps | Expected |
|----------|-------|----------|
| Offline create | Airplane mode -> Create note -> Disable airplane | Note syncs, status updates |
| Offline edit | Edit existing note offline -> Go online | Changes sync |
| Offline delete | Delete note offline -> Go online | Note removed from server |
| Conflict | Edit same note on two devices | Conflict dialog appears |
| Pull refresh | Swipe down on list | Server data fetched |

---

## File Reference

### New Files to Create

| Path | Purpose |
|------|---------|
| `shared-offline/build.gradle.kts` | Module config |
| `shared-offline/.../database/BlinkoDatabase.kt` | Room database |
| `shared-offline/.../database/entity/NoteEntity.kt` | Note table |
| `shared-offline/.../database/entity/SyncQueueEntity.kt` | Queue table |
| `shared-offline/.../database/entity/TagEntity.kt` | Tags cache |
| `shared-offline/.../database/dao/NoteDao.kt` | Note queries |
| `shared-offline/.../database/dao/SyncQueueDao.kt` | Queue queries |
| `shared-offline/.../database/dao/TagDao.kt` | Tag queries |
| `shared-offline/.../connectivity/ConnectivityMonitor.kt` | Network state |
| `shared-offline/.../sync/SyncQueueManager.kt` | Queue logic |
| `shared-offline/.../sync/SyncWorker.kt` | Background sync |
| `shared-offline/.../sync/SyncStatus.kt` | Status enum |
| `shared-offline/.../sync/ConflictResolver.kt` | Conflict handling |
| `shared-offline/.../di/OfflineModule.kt` | Hilt bindings |
| `feature-notes/.../data/repository/OfflineFirstNoteRepository.kt` | New repository |
| `feature-notes/.../data/mapper/NoteEntityMapper.kt` | Mappers |
| `shared-ui/.../sync/SyncStatusIndicator.kt` | UI component |
| `shared-ui/.../sync/SyncState.kt` | UI state |
| `feature-notes/.../presentation/conflict/ConflictResolutionDialog.kt` | Conflict UI |

### Files to Modify

| Path | Changes |
|------|---------|
| `gradle/libs.versions.toml` | Add Room, WorkManager |
| `settings.gradle.kts` | Include shared-offline module |
| `feature-notes/api/.../model/BlinkoNote.kt` | Add sync fields |
| `feature-notes/api/.../NoteRepository.kt` | Add Flow methods |
| `feature-notes/implementation/.../di/NotesModule.kt` | Bind new repo |
| `feature-notes/.../presentation/NoteListScreenViewModel.kt` | Use Flow |
| `feature-notes/.../presentation/NoteListScreenComposable.kt` | Add indicators |
| `app/build.gradle.kts` | Depend on shared-offline |

---

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Data loss during migration | Export notes before upgrade, implement migration |
| Sync loop | Debounce rapid changes, track last sync time |
| Queue buildup | Limit queue size, expire old operations |
| Battery drain | Use WorkManager constraints, batch operations |
| Conflict complexity | Start with simple last-write-wins, add manual later |

---

## Future Enhancements

1. **Attachment sync** - Queue file uploads separately
2. **Incremental sync** - Use `updatedAt` cursor for delta sync
3. **Merge tool** - Side-by-side diff for conflict resolution
4. **Sync settings** - WiFi-only, sync frequency
5. **Sync history** - Log of sync operations for debugging
