package com.reality.rememberaiprototype.home.data

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Memory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}