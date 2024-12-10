package com.example.easyaichat.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.easyaichat.data.database.entities.APIKeyEntity
import com.example.easyaichat.data.database.entities.ChatEntity
import com.example.easyaichat.data.database.entities.DefaultAPIKeyEntity
import com.example.easyaichat.data.database.entities.MessageEntity
import com.example.easyaichat.data.database.entities.MessageImageEntity
import com.example.easyaichat.data.database.entities.ModelEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import java.util.UUID

@Dao
interface ChatDao {
    /*
    Chats
     */
    @Insert
    suspend fun insertChat(chat: ChatEntity)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Delete
    suspend fun deleteChat(chat: ChatEntity)

    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Insert
    suspend fun insertImageMessage(message: MessageImageEntity)

    @Query("SELECT * FROM message WHERE chatId = :id")
    fun getMessages(id: UUID): Flow<List<MessageEntity>>

    @Query("SELECT * FROM imageMessage WHERE chatId = :id")
    fun getImageMessages(id: UUID): Flow<List<MessageImageEntity>>

    @Query("SELECT id, title, keyId, model FROM chats WHERE id = :id")
    suspend fun getChat(id: UUID?): ChatEntity

    @Query("SELECT * FROM chats")
    fun getChats(): Flow<List<ChatEntity>>

    /*
    ApiKey
     */
    @Insert
    suspend fun insertAPIKey(apiKeyEntity: APIKeyEntity)

    @Query("Select * from api_keys WHERE id = :id")
    suspend fun getAPIKeyById(id: UUID): APIKeyEntity

    @Update
    suspend fun updateAPIKey(apiKeyEntity: APIKeyEntity)

    @Delete
    suspend fun deleteAPIKey(apiKeyEntity: APIKeyEntity)

    @Query("SELECT * FROM api_keys")
    fun getAllAPIKeyEntities(): Flow<List<APIKeyEntity>>

    @Query("SELECT apiKey FROM api_keys WHERE id = :apiKeyEntityId")
    suspend fun getAPIKey(apiKeyEntityId: UUID): String

    @Query("SELECT * FROM api_keys WHERE id = :id")
    suspend fun getAPIKeyEntity(id: UUID): APIKeyEntity

    @Query("SELECT COUNT(*) FROM api_keys")
    fun getAPIKeyEntityCount(): Int

    @Query("SELECT provider FROM api_keys WHERE apiKey = :apiKeyEntityId")
    suspend fun getProvider(apiKeyEntityId: UUID): String

    /*
    Default apikey
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefaultAPIKey(defaultAPIKeyEntity: DefaultAPIKeyEntity)

    @Query("Select * FROM default_key")
    suspend fun getDefaultAPIKey(): DefaultAPIKeyEntity

    @Delete
    suspend fun deleteDefaultAPIKey(defaultAPIKeyEntity: DefaultAPIKeyEntity)

    /*
    Modelentity
     */
    @Query("Select * from model WHERE model = :name")
    suspend fun getModelEntityByName(name: String): ModelEntity

    @Query("SELECT * from model WHERE provider = :provider")
    fun getModelEntitiesByProvider(provider: String): Flow<List<ModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<ModelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ModelEntity)

    @Query("SELECT COUNT(*) FROM model")
    suspend fun getModelCount(): Int

    @Query("SELECT * FROM model")
    fun getAllModelEntities():Flow<List<ModelEntity>>

}