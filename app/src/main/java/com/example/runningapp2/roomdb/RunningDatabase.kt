package com.example.runningapp2.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RunEntity::class], version = 1)

@TypeConverters(Convertors::class)
abstract class RunningDatabase: RoomDatabase() {

    abstract fun getRunDao(): RunDao
}