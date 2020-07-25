package ru.zemlyanaya.monochrome.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Colour::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun colourDao(): IColourDao

    companion object{
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monochrome_database"
                )
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}