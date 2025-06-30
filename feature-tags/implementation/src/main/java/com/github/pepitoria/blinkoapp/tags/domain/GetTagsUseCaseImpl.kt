package com.github.pepitoria.blinkoapp.tags.domain

import com.github.pepitoria.blinkoapp.tags.api.domain.BlinkoTag
import com.github.pepitoria.blinkoapp.tags.api.domain.GetTagsUseCase
import com.github.pepitoria.blinkoapp.tags.data.TagsRepository
import javax.inject.Inject

class GetTagsUseCaseImpl @Inject constructor(
    private val tagsRepository: TagsRepository,
) : GetTagsUseCase {

    override suspend operator fun invoke(): List<BlinkoTag> {
        return tagsRepository.getTags()
    }
}