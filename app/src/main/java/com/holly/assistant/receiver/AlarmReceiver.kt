package com.holly.assistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.holly.assistant.util.CommandProcessor

/**
 * Alarm Receiver
 * Handles scheduled tasks and reminders
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        const val EXTRA_COMMAND = "command"
        const val EXTRA_TASK_ID = "task_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm triggered")
        
        val command = intent.getStringExtra(EXTRA_COMMAND)
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        
        if (command != null) {
            val processor = CommandProcessor(context)
            val result = processor.executeCommand(command)
            
            Log.d(TAG, "Executed scheduled command: $command, result: ${result.success}")
            
            // TODO: Update task lastExecuted time in database
        }
    }
}
