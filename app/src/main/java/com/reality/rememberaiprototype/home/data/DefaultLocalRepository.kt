package com.reality.rememberaiprototype.home.data

import com.reality.rememberaiprototype.home.domain.LocalRepository
import timber.log.Timber

class DefaultLocalRepository(private val memoryDao: MemoryDao) : LocalRepository {
    override suspend fun fetchAllMemories(): List<Memory> {
        return memoryDao.getAll()
    }

    override suspend fun saveMemory(memory: Memory) {
        Timber.e("Saving ${memory.path} to Room!")
        memoryDao.insert(memory)
    }
}