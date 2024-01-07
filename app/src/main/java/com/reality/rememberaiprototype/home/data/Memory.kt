package com.reality.rememberaiprototype.home.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Memory(
    @PrimaryKey(autoGenerate = true)val uid: Int = 0,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "creation_date") val creationDate: Long
    )

fun Memory.toImage(): Image {
    return Image(imagePath = path, imageText = content ?: "")

}
