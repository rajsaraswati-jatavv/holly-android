package com.holly.assistant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Conversation Message Entity
 * Stores chat history between user and Holly
 */
@Entity(tableName = "conversations")
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "holly"
    val content: String,
    val timestamp: Long,
    val sessionId: String? = null
)

/**
 * User Memory Entity
 * Stores important user information for personalization
 */
@Entity(tableName = "user_memories")
data class UserMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String, // "preference", "fact", "event", "contact"
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis()
)

/**
 * Scheduled Task Entity
 * Stores scheduled commands and reminders
 */
@Entity(tableName = "scheduled_tasks")
data class ScheduledTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val command: String,
    val scheduledTime: Long,
    val repeatType: RepeatType = RepeatType.NONE,
    val isActive: Boolean = true,
    val lastExecuted: Long? = null
)

enum class RepeatType {
    NONE, DAILY, WEEKLY, MONTHLY, CUSTOM
}

/**
 * Macro Entity
 * Stores recorded sequences of actions
 */
@Entity(tableName = "macros")
data class Macro(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val triggerPhrase: String,
    val actions: String, // JSON serialized list of actions
    val createdAt: Long = System.currentTimeMillis()
)
