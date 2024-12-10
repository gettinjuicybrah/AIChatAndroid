package com.example.easyaichat.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "message"
)
data class MessageEntity @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey
    val id: UUID,
    val chatId: UUID,
    val content: String,
    //user will always be the top of the list (first elem.)
    val isUser: Boolean,
    val index: Int,
    val subIndex: Int
)
