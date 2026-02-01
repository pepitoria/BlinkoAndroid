package com.github.pepitoria.blinkoapp.shared.domain.data

import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoSession
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoUser

interface AuthenticationRepository {
    fun saveSession(url: String, token: String)
    fun saveSession(url: String, userName: String, password: String, token: String)
    fun getSession(): BlinkoSession?
    suspend fun login(url: String, userName: String, password: String): BlinkoResult<BlinkoUser>
    suspend fun login(): BlinkoResult<BlinkoUser>
    fun logout()
}
