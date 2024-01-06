package com.reality.rememberaiprototype.home.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory ORDER BY creation_date ASC")
    fun getAll(): List<Memory>
}