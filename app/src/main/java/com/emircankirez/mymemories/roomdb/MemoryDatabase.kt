package com.emircankirez.mymemories.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.emircankirez.mymemories.model.Memory

@Database(entities = [Memory::class], version = 1)
abstract class MemoryDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}