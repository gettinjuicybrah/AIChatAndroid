package com.example.easyaichat.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easyaichat.data.database.entities.APIKeyEntity
import com.example.easyaichat.data.database.entities.ChatEntity
import com.example.easyaichat.data.database.entities.MessageEntity
import com.example.easyaichat.data.database.entities.MessageImageEntity
import com.example.easyaichat.data.database.entities.ModelEntity
import com.example.easyaichat.data.database.entities.ProviderEntity
import com.example.easyaichat.data.database.repository.ChatRepository
import com.example.easyaichat.data.model.ChatMessage
import com.example.easyaichat.network.LLM_APIService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

//Default chat is null, and this can be changed if we get here by clicking on an existing chat
//within the chatlist fragment, which will just pass that corresponding ChatEntity as a param.
/*
**`_editableTitle`**: Holds the temporary, editable title.
- **`setEditableTitle`**: Updates `_editableTitle` as the user types without persisting immediately.
- **`saveChatTitle`**: Persists `_editableTitle` to `ChatEntity` via the repository.
- **`clearError`**: Resets any error messages in the UI state.
 */
class ChatViewModel(
    private val chatId: UUID? = null
) : ViewModel(), KoinComponent {
    private val _newChatId = MutableSharedFlow<UUID>()
    val newChatId = _newChatId.asSharedFlow()

    private val chatRepository: ChatRepository by inject()
    private val llmApiService: LLM_APIService by inject()

    private val _providerToModelMap = MutableStateFlow<Map<String, List<ModelEntity>>>(emptyMap())
    val providerToModelMap: StateFlow<Map<String, List<ModelEntity>>> =
        _providerToModelMap.asStateFlow()

    private val _messages: MutableStateFlow<List<ChatMessage>> = MutableStateFlow(emptyList())
    val messages: StateFlow<List<ChatMessage>>
        get() = _messages.asStateFlow()

    private val _editableTitle = MutableStateFlow<String>("")
    val editableTitle: StateFlow<String> = _editableTitle.asStateFlow()

    data class ChatUiState(
        val chatEntity: ChatEntity? = null,
        val selectedApiKeyEntity: APIKeyEntity? = null,
        val selectedModelEntity: ModelEntity? = null,
        val availableProviders: List<String> = emptyList(),
        val availableModels: List<ModelEntity> = emptyList(),
        val initialProviderSelection: String? = null,
        val initialModelSelection: String? = null,
        val isLoading: Boolean = false,
        val isUpdatingTitle: Boolean = false,
        val error: String? = null,
        val apiKeyEntities: List<APIKeyEntity> = emptyList()
    )

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    val selectedImages: StateFlow<List<String>> = _selectedImages.asStateFlow()

    init {
        viewModelScope.launch {
            if (chatId == null) {
                initNewChat()
            } else {
                initPersistedChat()
            }
            initializeChat()
            _uiState.value.chatEntity?.let { chat ->
                _editableTitle.value = chat.title
            }
        }
    }

    /**
     * Updates the temporary editable title based on user input.
     */
    fun setEditableTitle(newTitle: String) {
        _editableTitle.value = newTitle
    }

    /**
     * Saves the temporary editable title to the persisted chat entity.
     */
    fun saveChatTitle() {
        viewModelScope.launch {
            val currentTitle = _editableTitle.value.trim()

            _uiState.value.chatEntity?.let { currentChat ->
                if (currentChat.title != currentTitle) {
                    val updatedChat = currentChat.copy(title = currentTitle)
                    chatRepository.updateChat(updatedChat)
                    _uiState.update { it.copy(chatEntity = updatedChat, error = null) }
                }
            }
        }
    }

    private suspend fun initializeChat() {
        try {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                chatRepository.getAllAPIKeyEntities(),
                chatRepository.getAllModelEntities()
            ) { apiKeys, models ->
                Pair(apiKeys, models)
            }.collect { (apiKeys, allModels) ->
                val selectedApiKey = determineAPIKey()
                val availableModels = selectedApiKey?.provider?.let { provider ->
                    allModels.filter { it.provider == provider }
                } ?: emptyList()
                val selectedModel = determineModel()

                _uiState.update { state ->
                    state.copy(
                        selectedApiKeyEntity = selectedApiKey,
                        availableProviders = apiKeys.map { it.provider }.distinct(),
                        availableModels = availableModels,
                        selectedModelEntity = selectedModel,
                        isLoading = false
                    )
                }

                // Update providerToModelMap
                providerToModelMapGen(apiKeys, allModels)
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private fun initNewChat() {
        viewModelScope.launch {
            chatRepository.getAllAPIKeyEntities().collect { list ->
                _uiState.update {
                    it.copy(
                        selectedApiKeyEntity = determineAPIKey(),
                        selectedModelEntity = determineModel(),
                        apiKeyEntities = list
                    )
                }
            }
        }
    }

    fun clearSelectedImages() {
        _selectedImages.value = emptyList()
    }

    fun updateSelectedAPIKey(apiKeyEntity: APIKeyEntity) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    selectedApiKeyEntity = apiKeyEntity,
                    selectedModelEntity = when {
                        // For existing chats, keep the current model if it's compatible
                        state.chatEntity != null &&
                                providerToModelMap.value[apiKeyEntity.provider]?.any {
                                    it.model == state.chatEntity.model
                                } == true -> state.selectedModelEntity
                        // Otherwise use the default model
                        else -> providerToModelMap.value[apiKeyEntity.provider]?.find {
                            it.model == apiKeyEntity.defaultModel
                        }
                    }
                )
            }
        }
    }

    // Takes in as params to ensure it is working on the latest data.
    private suspend fun providerToModelMapGen(
        apiKeys: List<APIKeyEntity>,
        models: List<ModelEntity>
    ) {
        val providerToModelMap = apiKeys
            .map { it.provider }
            .distinct()
            .associateWith { provider ->
                models.filter { it.provider == provider }
            }
        _providerToModelMap.value = providerToModelMap

        // Debug log to verify models
        providerToModelMap.forEach { (provider, models) ->
            Log.d("ChatViewModel", "Provider: $provider, Models: ${models.map { it.model }}")
        }
    }

    private fun initPersistedChat() {
        viewModelScope.launch {
            chatRepository.getAllAPIKeyEntities().collect { list ->

                _uiState.update {
                    it.copy(
                        chatEntity = chatRepository.getChat(chatId),
                        selectedApiKeyEntity = determineAPIKey(),
                        selectedModelEntity = determineModel(),
                        apiKeyEntities = list
                    )
                }
                loadMessages(chatId!!)
            }
        }
    }

    /*
    Get the default apikey for either a new chat or a persisted one. Regardless, will default to null, meaning that a key is not assigned
    and may not exist
     */
    //suspend so can be called within other coroutines
    suspend fun determineAPIKey(): APIKeyEntity? {
        return if (chatId == null) {
            chatRepository.getDefaultAPIKey()?.let { chatRepository.getAPIKeyEntity(it.keyId) }
        } else {
            chatRepository.getChat(chatId)?.keyId?.let { chatRepository.getAPIKeyEntity(it) }
        }
    }

    // suspend so can be called within other coroutines
    suspend fun determineModel(): ModelEntity? {
        return if (chatId == null) {
            uiState.value.selectedApiKeyEntity?.defaultModel?.let { chatRepository.getModelByName(it) }
        } else {
            uiState.value.chatEntity?.model?.let { chatRepository.getModelByName(it) }
        }
    }

    fun setApiKey(apiKeyEntity: APIKeyEntity) {
        _uiState.update {
            it.copy(selectedApiKeyEntity = apiKeyEntity)
        }
    }

    fun setModel(modelEntity: ModelEntity) {
        _uiState.update {
            it.copy(selectedModelEntity = modelEntity)
        }
    }

    //load messages associated with the persisted chat
    private fun loadMessages(chatId: UUID) {
        viewModelScope.launch {
            chatRepository.getChatMessages(chatId)
                .combine(chatRepository.getChatImageMessages(chatId)) { chatMessages, chatImageMessages ->
                    chatMessages.map { message ->
                        val associatedImages = chatImageMessages
                            .filter { it.index == message.index }
                            .sortedBy { it.subIndex }
                            .map { it.uri }

                        ChatMessage(
                            message,
                            images = associatedImages
                        )
                    }
                }.collect { combinedMessages ->
                    _messages.value = combinedMessages
                }
        }
    }

    fun sendMessage(content: String, images: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) } // Start loading
                val apiKeyEntity = uiState.value.selectedApiKeyEntity
                    ?: throw IllegalStateException("No API key selected")

                if (content.isNotEmpty() || images.isNotEmpty()) {
                    if (chatId == null) {
                        //userMessage is just a placeholder that will be overwritten if the actual message is to be created.
                        //Avoids persisting data if there is an error
                        var userMessage = MessageEntity(
                            id = UUID.randomUUID(),
                            chatId = UUID.randomUUID(),
                            content = content,
                            isUser = true,
                            index = 0,
                            subIndex = 0
                        )
                        //So it can be passed into LLM_APIService, keeping the segregation logic solely here.
                        val messageList: List<ChatMessage> =
                            listOf(ChatMessage(userMessage, images))
                        ChatMessage(userMessage, images)
                        // Handle new chat creation
                        val aiResponse = llmApiService.sendPrompt(
                            history = messageList,
                            provider = apiKeyEntity.provider,
                            model = apiKeyEntity.defaultModel,
                            apiKey = apiKeyEntity.apiKey
                        )
                        val newChatId = UUID.randomUUID()
                        val newChat = ChatEntity(
                            id = newChatId,
                            title = content.take(30),
                            keyId = apiKeyEntity.id,
                            model = apiKeyEntity.defaultModel
                        )

                        // Create and insert user message
                        userMessage = MessageEntity(
                            id = UUID.randomUUID(),
                            chatId = newChatId,
                            content = content,
                            isUser = true,
                            index = 0,
                            subIndex = 0
                        )
                        chatRepository.insertChat(newChat)
                        chatRepository.insertMessage(userMessage)

                        // Associate images with user message
                        images.forEachIndexed { subIndex, uri ->
                            val imageEntity = MessageImageEntity(
                                id = UUID.randomUUID(),
                                chatId = newChatId,
                                uri = uri,
                                index = userMessage.index,
                                subIndex = subIndex
                            )
                            chatRepository.insertImageMessage(imageEntity)
                        }

                        // Create and insert AI message
                        val aiMessage = MessageEntity(
                            id = UUID.randomUUID(),
                            chatId = newChatId,
                            content = aiResponse,
                            isUser = false,
                            index = 1,
                            subIndex = 0
                        )
                        chatRepository.insertMessage(aiMessage)
                        // Emit the new chat ID
                        /*
                        - A `MutableSharedFlow` named `_newChatId` is introduced to emit new chat IDs.
- After successfully creating a new chat and inserting relevant messages, `_newChatId.emit(newChatId)` is called to notify observers.
                         */
                        _newChatId.emit(newChatId)
                        // Update UI state with new chat
                        _uiState.update { it.copy(chatEntity = newChat) }
                        loadMessages(newChatId)
                    } else {
                        // Handle existing chat
                        val currentMessages = messages.value
                        val nextUserIndex =
                            (currentMessages.maxOfOrNull { it.messageEntity.index } ?: -1) + 1

                        // Create and insert user message
                        val userMessage = MessageEntity(
                            id = UUID.randomUUID(),
                            chatId = chatId,
                            content = content,
                            isUser = true,
                            index = nextUserIndex,
                            subIndex = 0
                        )
                        chatRepository.insertMessage(userMessage)

                        // Associate images with user message
                        images.forEachIndexed { subIndex, uri ->
                            val imageEntity = MessageImageEntity(
                                id = UUID.randomUUID(),
                                chatId = chatId,
                                uri = uri,
                                index = userMessage.index,
                                subIndex = subIndex
                            )
                            chatRepository.insertImageMessage(imageEntity)
                        }

                        // Update conversation history
                        val updatedHistory = currentMessages + ChatMessage(userMessage, images)

                        // Get AI response with history
                        val aiResponse = llmApiService.sendPrompt(
                            history = updatedHistory,
                            provider = apiKeyEntity.provider,
                            model = apiKeyEntity.defaultModel,
                            apiKey = apiKeyEntity.apiKey
                        )

                        // Create and insert AI message
                        val aiMessage = MessageEntity(
                            id = UUID.randomUUID(),
                            chatId = chatId,
                            content = aiResponse,
                            isUser = false,
                            index = nextUserIndex + 1,
                            subIndex = 0
                        )
                        chatRepository.insertMessage(aiMessage)
                    }
                } else {
                    throw IllegalArgumentException("Cannot send empty message without images")
                }
            } catch (e: Exception) {
                // Handle exceptions, possibly update UI state with error
                _uiState.update { it.copy(error = e.message) }
                Log.e("ChatViewModel", "Error sending message: ${e.message}", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) } // End loading
            }
        }
    }

    //adds image to current input message.
    fun addImage(uri: String) {
        _selectedImages.update { it + uri }
    }


    /**
     * Clears any existing error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // Save any pending changes when ViewModel is cleared
        viewModelScope.launch {
            _uiState.value.chatEntity?.let { chat ->
                chatRepository.updateChat(chat)
            }
        }
    }
}


