package com.example.easyaichat.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "chats"
)
data class ChatEntity (
    @PrimaryKey
    val id: UUID,
    val title: String,
    val keyId: UUID?,
    val model: String?
)