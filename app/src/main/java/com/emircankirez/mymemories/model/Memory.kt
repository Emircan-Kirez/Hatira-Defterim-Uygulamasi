package com.emircankirez.mymemories.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Memory(
    @ColumnInfo(name = "memoryName")
    var memoryName : String,

    @ColumnInfo(name = "comment")
    var comment : String,

    @ColumnInfo(name = "date")
    var date : String,

    @ColumnInfo(name = "image")
    var image : ByteArray

) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}