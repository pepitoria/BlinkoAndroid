package com.github.pepitoria.blinkoapp.domain.data

import com.github.pepitoria.blinkoapp.domain.model.BlinkoSession


interface AuthenticationRepository {
    fun saveSession(url: String, token: String)
    fun getSession(): BlinkoSession?
    fun logout()
}