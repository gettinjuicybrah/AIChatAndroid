package com.example.easyaichat.data.database.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.easyaichat.data.database.ChatDatabase
import com.example.easyaichat.data.database.dao.ChatDao
import com.example.easyaichat.data.database.entities.APIKeyEntity
import com.example.easyaichat.data.database.entities.ChatEntity
import com.example.easyaichat.data.database.entities.DefaultAPIKeyEntity
import com.example.easyaichat.data.database.entities.MessageEntity
import com.example.easyaichat.data.database.entities.MessageImageEntity
import com.example.easyaichat.data.database.entities.ModelEntity
import com.example.easyaichat.data.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID


class ChatRepository(
    private val chatDao: ChatDao
) : ChatRepositoryInterface {

    /*
    Chats
     */
    override suspend fun getChats(): Flow<List<ChatEntity>> = chatDao.getChats()
    override suspend fun getChat(id: UUID?): ChatEntity = chatDao.getChat(id)
    override suspend fun insertChat(chat: ChatEntity) {
        chatDao.insertChat(chat)
    }
    override suspend fun deleteChat(chat: ChatEntity) {
        chatDao.deleteChat(chat)
    }
    override suspend fun updateChat(chat: ChatEntity) {

        chatDao.updateChat(chat)

    }
    override suspend fun insertMessage(message: MessageEntity) {
        Log.d("ChatRepository", "Inserting message: $message")
        chatDao.insertMessage(message)
    }

    override suspend fun insertImageMessage(message: MessageImageEntity) {
        chatDao.insertImageMessage(message)
    }

    override suspend fun insertAPIKey(apiKeyEntity: APIKeyEntity) {
        chatDao.insertAPIKey(apiKeyEntity)
    }

    override suspend fun insertModel(modelEntity: ModelEntity) {
        chatDao.insertModel(modelEntity)
    }
    override suspend fun getChatMessages(chatId: UUID): Flow<List<MessageEntity>> {
        return chatDao.getMessages(chatId)
            .onEach { messages ->
                Log.d("ChatRepository", "Fetched ${messages.size} messages for chatId: $chatId")
            }
    }


    override suspend fun getChatImageMessages(chatId: UUID): Flow<List<MessageImageEntity>> {
        return chatDao.getImageMessages(chatId)
            .onEach { images ->
                Log.d("ChatRepository", "Fetched ${images.size} image messages for chatId: $chatId")
            }
    }
/*
apikey
 */
    override suspend fun getAPIKeyEntityCount(): Int {
        return chatDao.getAPIKeyEntityCount()
    }

    override fun getAllAPIKeyEntities(): Flow<List<APIKeyEntity>> {
        return chatDao.getAllAPIKeyEntities()
    }

    override suspend fun getAPIKeyEntity(id: UUID): APIKeyEntity? {
        return chatDao.getAPIKeyEntity(id)
    }



    override suspend fun updateAPIKey(apiKey: APIKeyEntity) {
        chatDao.updateAPIKey(apiKey)
    }

    override suspend fun deleteAPIKey(apiKey: APIKeyEntity) {
        chatDao.deleteAPIKey(apiKey)
    }

    override suspend fun getAPIKey(apiKeyEntityId: UUID): String{
        return chatDao.getAPIKey(apiKeyEntityId)
    }

    override suspend fun getProvider(apiKeyEntityId: UUID): String {
        return chatDao.getProvider(apiKeyEntityId)
    }
/*
default apikey
 */
    override suspend fun insertDefaultAPIKey(defaultAPIKeyEntity: DefaultAPIKeyEntity) {
        chatDao.insertDefaultAPIKey(defaultAPIKeyEntity)
    }
    override suspend fun getDefaultAPIKey(): DefaultAPIKeyEntity? {
        return chatDao.getDefaultAPIKey()
    }
    override suspend fun deleteDefaultAPIKey(defaultAPIKeyEntity: DefaultAPIKeyEntity){
        chatDao.deleteDefaultAPIKey(defaultAPIKeyEntity)
    }
   /*
   ModelEntities
    */
   override suspend fun getModelByName(name: String): ModelEntity {
       return chatDao.getModelEntityByName(name)
   }
    override suspend fun getModelEntitiesByProvider(provider: String): Flow<List<ModelEntity>> {
        return chatDao.getModelEntitiesByProvider(provider)
    }
    override suspend fun insertModels(models: List<ModelEntity>){
        chatDao.insertModels(models)
    }
    override suspend fun getModelCount(): Int {
        return chatDao.getModelCount()
    }
    override fun getAllModelEntities():Flow<List<ModelEntity>>{
        return chatDao.getAllModelEntities()
    }
}