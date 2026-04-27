package com.holly.assistant.data.local

import androidx.room.*
import com.holly.assistant.data.model.*

/**
 * Conversation DAO
 */
@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 100): List<ConversationMessage>
    
    @Query("SELECT * FROM conversations WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getSince(since: Long): List<ConversationMessage>
    
    @Query("SELECT * FROM conversations WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getBySession(sessionId: String): List<ConversationMessage>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ConversationMessage): Long
    
    @Query("DELETE FROM conversations WHERE timestamp < :before")
    suspend fun deleteOld(before: Long)
    
    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}

/**
 * User Memory DAO
 */
@Dao
interface UserMemoryDao {
    @Query("SELECT * FROM user_memories ORDER BY lastAccessed DESC")
    suspend fun getAll(): List<UserMemory>
    
    @Query("SELECT * FROM user_memories WHERE category = :category")
    suspend fun getByCategory(category: String): List<UserMemory>
    
    @Query("SELECT * FROM user_memories WHERE `key` = :key")
    suspend fun getByKey(key: String): UserMemory?
    
    @Query("SELECT * FROM user_memories WHERE `key` LIKE :pattern OR value LIKE :pattern")
    suspend fun search(pattern: String): List<UserMemory>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: UserMemory): Long
    
    @Update
    suspend fun update(memory: UserMemory)
    
    @Delete
    suspend fun delete(memory: UserMemory)
}

/**
 * Scheduled Task DAO
 */
@Dao
interface ScheduledTaskDao {
    @Query("SELECT * FROM scheduled_tasks WHERE isActive = 1 ORDER BY scheduledTime ASC")
    suspend fun getActive(): List<ScheduledTask>
    
    @Query("SELECT * FROM scheduled_tasks ORDER BY scheduledTime ASC")
    suspend fun getAll(): List<ScheduledTask>
    
    @Query("SELECT * FROM scheduled_tasks WHERE scheduledTime BETWEEN :start AND :end")
    suspend fun getBetween(start: Long, end: Long): List<ScheduledTask>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: ScheduledTask): Long
    
    @Update
    suspend fun update(task: ScheduledTask)
    
    @Delete
    suspend fun delete(task: ScheduledTask)
}

/**
 * Macro DAO
 */
@Dao
interface MacroDao {
    @Query("SELECT * FROM macros")
    suspend fun getAll(): List<Macro>
    
    @Query("SELECT * FROM macros WHERE triggerPhrase = :phrase")
    suspend fun getByTrigger(phrase: String): Macro?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(macro: Macro): Long
    
    @Delete
    suspend fun delete(macro: Macro)
}
