package com.example.easyaichat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyaichat.data.database.entities.ChatEntity
import com.example.easyaichat.data.database.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChatListViewModel : ViewModel(), KoinComponent {
    private val chatRepository: ChatRepository by inject()

    private val _chat = MutableStateFlow<List<ChatEntity>>(emptyList())
    val chat: StateFlow<List<ChatEntity>> = _chat.asStateFlow()
/*
Need to add the delete and edit title.
if delete, ensure all associated data throughout the data is deleted as well
(associated by said things foreign key pointing to chatid)
 */
    init {
        loadChats()
    }
    private fun loadChats(){
        viewModelScope.launch {
            chatRepository.getChats().collect {
                _chat.value = it
            }
        }
    }

    fun deleteChat(chatEntity: ChatEntity){
        viewModelScope.launch{
            chatRepository.deleteChat(chatEntity)
            loadChats()
        }
    }

}