package com.example.easyaichat.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
/*
@Serializable
data class OpenAIRequestNoImages(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7
) {
    @Serializable
    data class Message(
        val role: String,
        val content: String
    )
}
 */

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<MessageBlock>
)

//An element of the Message list in the JSON request
@Serializable
data class MessageBlock(
    //user or assistant
    val role: String,
    val content: List<ContentBlock>
)

//An element of a specific message list element. Itself is a list who's element contain either text or image.
@Serializable
data class ContentBlock(
    //will be either "text" or "image_url"
    val type: String,
    val text: String? = null,
    val image_url: ImageUrl? = null
)

@Serializable
data class ImageUrl(
    //base64 encoding of the image.
    //appending to front will be data:image/jpeg;base64,ENCODINGHERE
    val url: String,
    val detail: String = "low"
)