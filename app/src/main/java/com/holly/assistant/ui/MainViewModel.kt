package com.holly.assistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holly.assistant.data.local.HollyDatabase
import com.holly.assistant.data.model.ConversationMessage
import com.holly.assistant.service.ServiceState
import com.holly.assistant.service.HollyVoiceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val database: HollyDatabase
) : ViewModel() {

    private val _serviceState = MutableStateFlow<ServiceState>(ServiceState.Idle)
    val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()

    private val _recentMessages = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val recentMessages: StateFlow<List<ConversationMessage>> = _recentMessages.asStateFlow()

    init {
        loadRecentMessages()
    }

    private fun loadRecentMessages() {
        viewModelScope.launch {
            _recentMessages.value = database.conversationDao().getRecent(20)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            database.conversationDao().deleteAll()
            _recentMessages.value = emptyList()
        }
    }
}
