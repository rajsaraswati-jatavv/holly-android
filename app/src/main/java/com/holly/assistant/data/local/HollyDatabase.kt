package com.holly.assistant.data.local

import android.content.Context
import androidx.room.*
import com.holly.assistant.data.model.*

/**
 * Holly Database
 * Main Room database for storing conversations, memories, and tasks
 */
@Database(
    entities = [
        ConversationMessage::class,
        UserMemory::class,
        ScheduledTask::class,
        Macro::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HollyDatabase : RoomDatabase() {
    
    abstract fun conversationDao(): ConversationDao
    abstract fun userMemoryDao(): UserMemoryDao
    abstract fun scheduledTaskDao(): ScheduledTaskDao
    abstract fun macroDao(): MacroDao

    companion object {
        @Volatile
        private var INSTANCE: HollyDatabase? = null

        fun getInstance(context: Context): HollyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): HollyDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                HollyDatabase::class.java,
                "holly_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
