package com.reality.rememberaiprototype.home.domain

interface LocalRepository {

    suspend fun fetchAllMemory(): List<String>
}