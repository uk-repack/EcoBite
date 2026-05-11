package com.example.ecobite.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ecobite.data.local.entities.PantryItem
import com.example.ecobite.data.local.entities.WasteLog

@Database(
    entities = [PantryItem::class, WasteLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pantryDao(): PantryDao
    abstract fun wasteDao(): WasteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecobite_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

