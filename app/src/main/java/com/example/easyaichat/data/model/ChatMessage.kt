package com.example.easyaichat.data.model

import android.net.Uri
import com.example.easyaichat.data.database.entities.MessageEntity
import java.util.UUID
import kotlin.uuid.Uuid

data class ChatMessage(
    val messageEntity: MessageEntity,
    /*
    The index of MessageImageEntity will determine membership within this list,
    and the subindex (of the MessageImageEntity) will determine the position in list.
     */
    val images: List<String> = emptyList()
) {
    val content: String get() = messageEntity.content
    val isUser: Boolean get() = messageEntity.isUser
    val index: Int get() = messageEntity.index
}