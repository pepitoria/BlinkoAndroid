package com.github.pepitoria.blinkoapp.tags.data

import com.github.pepitoria.blinkoapp.tags.domain.BlinkoTag
import javax.inject.Inject

class TagMapper @Inject constructor() {

  fun toBlinkoTag(responseTag: ResponseTag): BlinkoTag {
    return BlinkoTag(
      name = responseTag.name?: "",
    )
  }
}