package com.github.pepitoria.blinkoapp.tags.data

import com.github.pepitoria.blinkoapp.tags.api.domain.BlinkoTag


interface TagsRepository {
    suspend fun getTags(): List<BlinkoTag>
}