package com.github.pepitoria.blinkoapp.tags.domain

import com.github.pepitoria.blinkoapp.tags.data.TagsRepository
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val tagsRepository: TagsRepository,
) {

    suspend operator fun invoke(): List<BlinkoTag> {
        return tagsRepository.getTags()
    }
}