package com.github.pepitoria.blinkoapp.tags.api.domain

data class BlinkoTag(
    val name: String,
) {
    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlinkoTag) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}