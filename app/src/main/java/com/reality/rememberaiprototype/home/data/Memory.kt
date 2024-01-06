package com.reality.rememberaiprototype.home.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Memory(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "creation_date") val creationDate: Date
    )