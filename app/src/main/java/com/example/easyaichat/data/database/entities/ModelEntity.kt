package com.example.easyaichat.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "model")
data class ModelEntity (
    @PrimaryKey
    val model:String,
    val canImage: Boolean,
    val maxTokens: Int,
    val provider: String
)
