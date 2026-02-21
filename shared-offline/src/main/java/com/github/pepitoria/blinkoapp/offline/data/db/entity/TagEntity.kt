package com.github.pepitoria.blinkoapp.offline.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
  @PrimaryKey
  val id: Int,
  val name: String,
  val icon: String? = null,
  val parent: Int? = null,
)
