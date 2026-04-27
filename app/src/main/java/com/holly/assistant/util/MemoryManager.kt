package com.holly.assistant.util

import android.content.Context
import android.util.Log
import com.holly.assistant.data.local.HollyDatabase
import com.holly.assistant.data.model.UserMemory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Memory Manager
 * Handles Holly's memory system for remembering user preferences, facts, and conversations
 */
class MemoryManager(private val context: Context) {

    companion object {
        private const val TAG = "MemoryManager"
    }

    private val database = HollyDatabase.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Remember a fact about the user
     */
    fun remember(key: String, value: String, category: String = "fact") {
        scope.launch {
            val memory = UserMemory(
                key = key,
                value = value,
                category = category,
                createdAt = System.currentTimeMillis()
            )
            database.userMemoryDao().insert(memory)
            Log.d(TAG, "Remembered: $key = $value")
        }
    }

    /**
     * Recall a fact about the user
     */
    suspend fun recall(key: String): String? {
        val memory = database.userMemoryDao().getByKey(key)
        return memory?.value
    }

    /**
     * Search memories
     */
    suspend fun search(query: String): List<UserMemory> {
        return database.userMemoryDao().search("%$query%")
    }

    /**
     * Get all memories by category
     */
    suspend fun getByCategory(category: String): List<UserMemory> {
        return database.userMemoryDao().getByCategory(category)
    }

    /**
     * Forget a memory
     */
    fun forget(key: String) {
        scope.launch {
            val memory = database.userMemoryDao().getByKey(key)
            memory?.let {
                database.userMemoryDao().delete(it)
                Log.d(TAG, "Forgot: $key")
            }
        }
    }

    /**
     * Build context string for LLM from memories
     */
    suspend fun buildMemoryContext(): String {
        val memories = database.userMemoryDao().getAll()
        if (memories.isEmpty()) return ""
        
        val sb = StringBuilder("User information I know:\n")
        memories.forEach { memory ->
            sb.append("- ${memory.key}: ${memory.value}\n")
        }
        return sb.toString()
    }
}
