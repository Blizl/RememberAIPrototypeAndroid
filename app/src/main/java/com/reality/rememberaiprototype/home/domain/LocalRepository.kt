package com.reality.rememberaiprototype.home.domain

import com.reality.rememberaiprototype.home.data.Memory

interface LocalRepository {

    suspend fun fetchAllMemories(): List<Memory>

    suspend fun saveMemory(memory: Memory)
}