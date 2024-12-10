package com.example.easyaichat.data.database

import androidx.room.TypeConverter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import java.util.UUID
class ChatTypeConverters {
    @TypeConverter
    fun fromUUID(uuid: UUID) : String {
        return uuid.toString()
    }


    @TypeConverter
    fun toUUID(uuid: String) : UUID {
        return UUID.fromString(uuid)
    }
}