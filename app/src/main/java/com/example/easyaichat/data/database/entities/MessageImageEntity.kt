package com.example.easyaichat.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "imageMessage"
)
data class MessageImageEntity @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey
    val id: UUID,
    val chatId: UUID,
    val uri: String,
    val index: Int,
    val subIndex: Int
)
