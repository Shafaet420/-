package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TallyEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KathHisabDatabase : RoomDatabase() {
    abstract fun tallyDao(): TallyDao

    companion object {
        @Volatile
        private var instance: KathHisabDatabase? = null

        fun getInstance(context: Context): KathHisabDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KathHisabDatabase::class.java,
                    "kath_hisab.db"
                ).build().also { instance = it }
            }
        }
    }
}
