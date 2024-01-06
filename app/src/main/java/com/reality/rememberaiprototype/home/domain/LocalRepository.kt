package com.reality.rememberaiprototype.home.domain

interface LocalRepository {

    suspend fun fetchAllMemories(): List<String>
}