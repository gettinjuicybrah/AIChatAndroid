package com.example.easyaichat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.easyaichat.data.database.dao.ChatDao
import com.example.easyaichat.data.database.entities.APIKeyEntity
import com.example.easyaichat.data.database.entities.ChatEntity
import com.example.easyaichat.data.database.entities.DefaultAPIKeyEntity
import com.example.easyaichat.data.database.entities.MessageEntity
import com.example.easyaichat.data.database.entities.MessageImageEntity
import com.example.easyaichat.data.database.entities.ModelEntity

@Database(
    version = 1, entities = [ChatEntity::class,
        APIKeyEntity::class,
        MessageEntity::class,
        MessageImageEntity::class,
        ModelEntity::class,
        DefaultAPIKeyEntity::class
    ],
    exportSchema = false
)
@TypeConverters(ChatTypeConverters::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun getChatDao(): ChatDao
}

internal const val dbFileName = "chat_db.db"