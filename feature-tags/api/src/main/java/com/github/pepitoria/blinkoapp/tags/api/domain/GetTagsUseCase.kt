package com.github.pepitoria.blinkoapp.tags.api.domain

interface GetTagsUseCase {
  suspend operator fun invoke(): List<BlinkoTag>
}