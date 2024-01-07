package com.reality.rememberaiprototype.home.domain

import com.reality.rememberaiprototype.home.data.Memory

interface LocalRepository {

    suspend fun fetchAllMemories(): List<String>

    suspend fun saveMemory(memory: Memory)
}