package com.github.pepitoria.blinkoapp.offline.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.pepitoria.blinkoapp.offline.data.db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

  @Query("SELECT * FROM tags ORDER BY name ASC")
  fun getAllAsFlow(): Flow<List<TagEntity>>

  @Query("SELECT * FROM tags ORDER BY name ASC")
  suspend fun getAll(): List<TagEntity>

  @Query("SELECT * FROM tags WHERE id = :id")
  suspend fun getById(id: Int): TagEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(tags: List<TagEntity>)

  @Query("DELETE FROM tags")
  suspend fun deleteAll()
}
