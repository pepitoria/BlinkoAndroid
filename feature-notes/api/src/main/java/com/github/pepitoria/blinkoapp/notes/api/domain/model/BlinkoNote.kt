package com.github.pepitoria.blinkoapp.notes.api.domain.model

data class BlinkoNote(
  val id: Int? = null,
  val localId: String? = null,
  val content: String,
  val type: BlinkoNoteType,
  val isArchived: Boolean,
  val syncStatus: SyncStatus = SyncStatus.SYNCED,
  val updatedAt: String? = null,
) {
  val isPending: Boolean
    get() = syncStatus == SyncStatus.PENDING_CREATE ||
      syncStatus == SyncStatus.PENDING_UPDATE ||
      syncStatus == SyncStatus.PENDING_DELETE

  val hasConflict: Boolean
    get() = syncStatus == SyncStatus.CONFLICT

  companion object {
    val EMPTY = BlinkoNote(
      content = "",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )
  }
}
