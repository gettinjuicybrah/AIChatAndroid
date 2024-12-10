package com.example.easyaichat.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "api_keys")
data class APIKeyEntity(
    @PrimaryKey
    val id: UUID,
    val apiKey: String,
    val nickname: String,
    val defaultModel: String,
    val provider: String
    )

@Entity(tableName = "default_key")

data class DefaultAPIKeyEntity(
    @PrimaryKey
    val keyId: UUID
)

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey
    val provider: String
)