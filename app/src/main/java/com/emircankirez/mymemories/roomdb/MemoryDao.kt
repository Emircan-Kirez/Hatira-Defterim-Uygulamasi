package com.emircankirez.mymemories.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.emircankirez.mymemories.model.Memory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface MemoryDao {
    @Query("SELECT * FROM Memory ORDER BY id DESC")
    fun getAll() : Flowable<List<Memory>>

    @Query("SELECT * FROM Memory WHERE id = :id")
    fun getMemoryById(id : Int) : Flowable<Memory>

    @Insert
    fun insert(memory: Memory) : Completable

    @Delete
    fun delete(memory: Memory) : Completable
}