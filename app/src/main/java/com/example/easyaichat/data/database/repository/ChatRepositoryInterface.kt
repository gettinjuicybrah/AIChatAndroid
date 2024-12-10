package com.example.easyaichat.data.database.repository

import com.example.easyaichat.data.database.entities.APIKeyEntity
import com.example.easyaichat.data.database.entities.ChatEntity
import com.example.easyaichat.data.database.entities.DefaultAPIKeyEntity
import com.example.easyaichat.data.database.entities.MessageEntity
import com.example.easyaichat.data.database.entities.MessageImageEntity
import com.example.easyaichat.data.database.entities.ModelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ChatRepositoryInterface {

    /*
    Chats
     */
    suspend fun getChats(): Flow<List<ChatEntity>>
    suspend fun getChat(id: UUID?): ChatEntity
    suspend fun insertChat(chat: ChatEntity)
    suspend fun deleteChat(chat: ChatEntity)
    suspend fun updateChat(chat: ChatEntity)

    suspend fun getChatMessages(chatId: UUID): Flow<List<MessageEntity>>
    suspend fun getChatImageMessages(chatId: UUID): Flow<List<MessageImageEntity>>

    suspend fun insertMessage(message: MessageEntity)
    suspend fun insertImageMessage(message: MessageImageEntity)

    /*
    Model
     */
    suspend fun getModelEntitiesByProvider(provider: String): Flow<List<ModelEntity>>
    suspend fun insertModel(modelEntity: ModelEntity)
    suspend fun getModelByName(name: String):ModelEntity
    suspend fun insertModels(models: List<ModelEntity>)
    suspend fun getModelCount(): Int
    fun getAllModelEntities():Flow<List<ModelEntity>>
    /*
    Default Key
     */

    suspend fun insertDefaultAPIKey(defaultAPIKeyEntity: DefaultAPIKeyEntity)
    suspend fun getDefaultAPIKey(): DefaultAPIKeyEntity?
    suspend fun deleteDefaultAPIKey(defaultAPIKeyEntity: DefaultAPIKeyEntity)
    /*
    APIKey
     */
    suspend fun insertAPIKey(apiKeyEntity: APIKeyEntity)
    suspend fun getAPIKey(apiKeyEntityId: UUID): String
    suspend fun getProvider(apiKeyEntityId: UUID): String
    fun getAllAPIKeyEntities(): Flow<List<APIKeyEntity>>
    suspend fun getAPIKeyEntity(id: UUID): APIKeyEntity?
    suspend fun updateAPIKey(apiKey: APIKeyEntity)
    suspend fun deleteAPIKey(apiKey: APIKeyEntity)
    suspend fun getAPIKeyEntityCount(): Int

}