package com.ckh.calculator

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ckh.calculator.dao.HistoryDao
import com.ckh.calculator.model.History

@Database(entities = [History::class],version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract  fun historyDao(): HistoryDao
}