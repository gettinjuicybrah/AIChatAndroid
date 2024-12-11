package com.example.easyaichat.network

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.easyaichat.data.model.ChatMessage
import com.example.easyaichat.network.provider.OpenAI_API
import com.example.easyaichat.network.request.ContentBlock
import com.example.easyaichat.network.request.ImageUrl
import com.example.easyaichat.network.request.MessageBlock
import com.example.easyaichat.network.request.OpenAIRequest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
This is meant to allow eventual extension to other API types in the future. Right now, only OpenAI is used.
But, you could easily extend by adding the corresponding classes for other providers, whether it be powered via
an HTTP engine for example, or even a dedicated library of some sort - the interface between the ViewModel and the handling of
the request is HERE.

That said, in it's current state, this service references an OpenAI_API object that it sends formatted data to.
Then, the OpenAI_API will send this data to a specified OpenAI endpoint (specified in '/network/routes/Routes') with catered structure.
Following, an expected structure is expected, and transformations are applied so the data can be used (persisted, displayed, etc.)

CALLED FROM:
    - ChatViewModel
CALLS:
    - OpenAI_API
 */
class LLM_APIService : KoinComponent {

    private val api: OpenAI_API by inject()
    private val contentResolver: ContentResolver by inject()
    suspend fun sendPrompt(
        history: List<ChatMessage>,
        provider: String,
        model: String,
        apiKey: String
    ): String {

        return when (provider) {
            "OpenAI" -> {
                val messages = history.map { chatMessage ->
                    //var content: List<ContentBlock> = emptyList()
                    var content = listOf(ContentBlock(type = "text", text = chatMessage.content))
                    MessageBlock(
                        role = if (chatMessage.isUser) "user" else "assistant",
                        //Means that the beginning of the content list will always be the associated content string with the ChatMessage
                        content  + chatMessage.images.map { imageUri -> //any images that exist come after, and already arrive sorted.
                            //for every imageUri that exists within the ChatMessage images list, a ContentBlock dataclass
                            //will be allocated, setting the type and passing the associated Uri.
                            val uri = Uri.parse(imageUri)
                            val encodedImage = "" + uri.toBase64(contentResolver, true)
                            ContentBlock(
                                type = "image_url",
                                image_url = ImageUrl(url = encodedImage)
                            )
                        }
                    )
                }

                val request = OpenAIRequest(
                    model = model,
                    messages = messages
                )

                api.sendPrompt(request, apiKey).getOrNull()?.response ?: "Default AI response"
            }

            else -> ""
        }
    }
    private fun Uri.toBase64(contentResolver: ContentResolver, includeDataUri: Boolean = false): String? {
        return try {
            val inputStream = contentResolver.openInputStream(this)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)

                if (includeDataUri) {
                    // Get MIME type from ContentResolver
                    var mimeType = contentResolver.getType(this) ?: "null"
                    if (mimeType == "image/jpeg") {mimeType = "image/jpg"}
                    "data:$mimeType;base64,$base64String"
                } else {
                    base64String
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}