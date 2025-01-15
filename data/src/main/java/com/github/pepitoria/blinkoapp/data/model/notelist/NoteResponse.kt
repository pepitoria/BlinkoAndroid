package com.github.pepitoria.blinkoapp.data.model.notelist

import com.google.gson.annotations.SerializedName

data class NoteResponse(
  @SerializedName("id") var id: Int? = null,
  @SerializedName("type") var type: Int? = null,
  @SerializedName("content") var content: String? = null,
  @SerializedName("isArchived") var isArchived: Boolean? = null,
  @SerializedName("isRecycle") var isRecycle: Boolean? = null,
  @SerializedName("isShare") var isShare: Boolean? = null,
  @SerializedName("isTop") var isTop: Boolean? = null,
  @SerializedName("isReviewed") var isReviewed: Boolean? = null,
  @SerializedName("sharePassword") var sharePassword: String? = null,
  @SerializedName("metadata") var metadata: String? = null,
  @SerializedName("accountId") var accountId: Int? = null,
  @SerializedName("createdAt") var createdAt: String? = null,
  @SerializedName("updatedAt") var updatedAt: String? = null,
  @SerializedName("attachments") var attachments: ArrayList<Attachments> = arrayListOf(),
  @SerializedName("tags") var tags: ArrayList<Tags> = arrayListOf(),
  @SerializedName("references") var references: ArrayList<References> = arrayListOf(),
  @SerializedName("referencedBy") var referencedBy: ArrayList<ReferencedBy> = arrayListOf()
)

data class Attachments (
  @SerializedName("id"            ) var id            : Int?     = null,
  @SerializedName("isShare"       ) var isShare       : Boolean? = null,
  @SerializedName("sharePassword" ) var sharePassword : String?  = null,
  @SerializedName("name"          ) var name          : String?  = null,
  @SerializedName("path"          ) var path          : String?  = null,
  @SerializedName("size"          ) var size          : String?  = null,
  @SerializedName("noteId"        ) var noteId        : Int?     = null,
  @SerializedName("createdAt"     ) var createdAt     : String?  = null,
  @SerializedName("updatedAt"     ) var updatedAt     : String?  = null,
  @SerializedName("type"          ) var type          : String?  = null
)

data class Tag (
  @SerializedName("id"        ) var id        : Int?    = null,
  @SerializedName("name"      ) var name      : String? = null,
  @SerializedName("icon"      ) var icon      : String? = null,
  @SerializedName("parent"    ) var parent    : Int?    = null,
  @SerializedName("createdAt" ) var createdAt : String? = null,
  @SerializedName("updatedAt" ) var updatedAt : String? = null
)

data class Tags (
  @SerializedName("id"     ) var id     : Int? = null,
  @SerializedName("noteId" ) var noteId : Int? = null,
  @SerializedName("tagId"  ) var tagId  : Int? = null,
  @SerializedName("tag"    ) var tag    : Tag? = Tag()
)

data class References (
  @SerializedName("toNoteId" ) var toNoteId : Int? = null
)


data class ReferencedBy (
  @SerializedName("fromNoteId" ) var fromNoteId : Int? = null
)