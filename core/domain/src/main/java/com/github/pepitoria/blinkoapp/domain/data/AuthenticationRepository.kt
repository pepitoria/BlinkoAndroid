package com.github.pepitoria.blinkoapp.domain.data

import com.github.pepitoria.blinkoapp.domain.data.model.session.SessionDto


interface AuthenticationRepository {
    fun saveSession(sessionDto: SessionDto)
    fun getSession(): SessionDto?
    fun logout()
}