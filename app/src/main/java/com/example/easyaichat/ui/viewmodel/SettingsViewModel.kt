package com.example.easyaichat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyaichat.data.database.entities.APIKeyEntity
import com.example.easyaichat.data.database.entities.ChatEntity
import com.example.easyaichat.data.database.entities.DefaultAPIKeyEntity
import com.example.easyaichat.data.database.entities.ModelEntity
import com.example.easyaichat.data.database.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class SettingsViewModel : ViewModel(), KoinComponent {
    private val chatRepository: ChatRepository by inject()
    private val _apiKeys = MutableStateFlow<List<APIKeyEntity>>(emptyList())
    val apiKeys: StateFlow<List<APIKeyEntity>> = _apiKeys.asStateFlow()
    private val _defaultKey = MutableStateFlow<DefaultAPIKeyEntity?>(null)
    val defaultKey: StateFlow<DefaultAPIKeyEntity?> = _defaultKey.asStateFlow()

    private val _models = MutableStateFlow<List<String>>(emptyList())
    val models: StateFlow<List<String>> = _models.asStateFlow()

    init {
        initModelsIfNeeded()
        loadAPIKeys()
        loadDefaultKey()
        loadModels()
    }

    fun initModelsIfNeeded() {
        viewModelScope.launch {
            if (chatRepository.getModelCount() == 0) {
                val openAIModels = listOf(
                    ModelEntity(
                        model = "gpt-3.5-turbo",
                        canImage = false,
                        maxTokens = 4096,
                        provider = "OpenAI"
                    ),
                    ModelEntity(
                        model = "gpt-4",
                        canImage = true,
                        maxTokens = 8192,
                        provider = "OpenAI"
                    ),
                    ModelEntity(
                        model = "gpt-4o",
                        canImage = true,
                        maxTokens = 0,
                        provider = "OpenAI"
                    )
                )
                chatRepository.insertModels(openAIModels)

            }
        }
    }

    fun loadDefaultKey() {
        viewModelScope.launch {
            _defaultKey.value = chatRepository.getDefaultAPIKey()
        }
    }

    fun loadAPIKeys() {
        viewModelScope.launch {
            chatRepository.getAllAPIKeyEntities()
                .collect { apiKeys ->
                    _apiKeys.value = apiKeys
                }
        }
    }

    fun loadModels() {
        viewModelScope.launch {
            chatRepository.getModelEntitiesByProvider("OpenAI").collect { modelEntities ->
                _models.value = modelEntities.map { it.model }
            }
        }
    }

    fun addAPIKey(nickname: String, key: String, provider: String, defaultModel: String) {
        viewModelScope.launch {
            val newKey = APIKeyEntity(
                id = UUID.randomUUID(),
                apiKey = key,
                nickname = nickname,
                provider = provider,
                defaultModel = defaultModel
            )
            chatRepository.insertAPIKey(newKey)
            _apiKeys.value = _apiKeys.value + newKey
            if (_apiKeys.value.size == 1) {
                setDefaultAPIKey(newKey)
                loadAPIKeys()
            }

        }
        loadAPIKeys()
    }

    fun updateAPIKey(apiKey: APIKeyEntity) {
        viewModelScope.launch {
            chatRepository.updateAPIKey(apiKey)
            _apiKeys.value = _apiKeys.value.map { if (it.id == apiKey.id) apiKey else it }
            loadAPIKeys()
        }
        loadAPIKeys()
    }

    fun deleteAPIKey(apiKey: APIKeyEntity) {
        viewModelScope.launch {
            setRelevantChatsAPIKeyAndModelToNull(getChatsUsingKey(apiKey))
            chatRepository.deleteAPIKey(apiKey)
            _apiKeys.value = _apiKeys.value.filter { it.id != apiKey.id }
            loadAPIKeys()
        }
        loadAPIKeys()
    }

    private suspend fun getChatsUsingKey(apiKeyEntity: APIKeyEntity): List<ChatEntity> {
        return chatRepository.getChats()
            .first()
            .filter { it.keyId == apiKeyEntity.id }
            .toList()
    }

    private fun setRelevantChatsAPIKeyAndModelToNull(chats: List<ChatEntity>) {
        viewModelScope.launch {
            chats.forEach { chat ->
                chatRepository.updateChat(chat.copy(keyId = null, model = null))
            }
        }
    }

    fun setDefaultAPIKey(apiKeyEntity: APIKeyEntity) {
        viewModelScope.launch {
            val defaultAPIKeyEntity = DefaultAPIKeyEntity(apiKeyEntity.id)
            chatRepository.insertDefaultAPIKey(defaultAPIKeyEntity)
            _defaultKey.value = defaultAPIKeyEntity  // Update the flow directly
        }

    }

    fun deleteDefaultAPIKey(apiKey: APIKeyEntity) {
        viewModelScope.launch {
            val defaultAPIKeyEntity = chatRepository.getDefaultAPIKey()
            if (defaultAPIKeyEntity?.keyId == apiKey.id) {
                chatRepository.deleteDefaultAPIKey(defaultAPIKeyEntity)
                loadAPIKeys()
            }
            loadAPIKeys()
        }
    }
}