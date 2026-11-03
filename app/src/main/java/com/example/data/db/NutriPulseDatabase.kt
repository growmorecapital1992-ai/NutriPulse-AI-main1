package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfileEntity::class, MealRecordEntity::class, HydrationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NutriPulseDatabase : RoomDatabase() {
    abstract fun dao(): NutriPulseDao

    companion object {
        @Volatile
        private var INSTANCE: NutriPulseDatabase? = null

        fun getDatabase(context: Context): NutriPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutriPulseDatabase::class.java,
                    "nutripulse_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
