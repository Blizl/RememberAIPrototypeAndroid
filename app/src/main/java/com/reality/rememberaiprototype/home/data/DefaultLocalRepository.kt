package com.reality.rememberaiprototype.home.data

import com.reality.rememberaiprototype.home.domain.LocalRepository

class DefaultLocalRepository(private val memoryDao: MemoryDao) : LocalRepository {
    override suspend fun fetchAllMemories(): List<String> {
        return memoryDao.getAll().map { it.path }
    }
}