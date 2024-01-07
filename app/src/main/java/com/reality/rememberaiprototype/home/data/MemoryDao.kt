package com.reality.rememberaiprototype.home.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory ORDER BY creation_date DESC")
    fun getAll(): List<Memory>

    @Insert
    fun insert(memory: Memory)
}