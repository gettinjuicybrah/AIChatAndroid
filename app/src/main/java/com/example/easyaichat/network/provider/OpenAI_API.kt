package com.example.easyaichat.network.provider

import android.content.Context
import android.util.Log
import com.example.easyaichat.network.request.OpenAIRequest
import com.example.easyaichat.network.response.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonNull.serializer
import org.koin.core.component.KoinComponent
import java.io.File
import java.io.FileWriter
import java.io.IOException

class OpenAI_API(private val httpClient: HttpClient,
                 private val context: Context) : KoinComponent {
    fun writeToFile(fileName: String, content: String) {
        try {
            // Use internal storage
            val file = File(context.filesDir, fileName)
            val fileWriter = FileWriter(file, true) // 'true' appends to the file
            fileWriter.write(content)
            fileWriter.flush()
            fileWriter.close()
            println("Data written to file: ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    suspend fun sendPrompt(openAIRequest: OpenAIRequest, apiKey: String): Result<OpenAIResponse> {
        println("UNENCODED: "+ openAIRequest)
        println("REQUEST IN OPENAI API: " + Json.encodeToString(openAIRequest))
        val s = Json.encodeToString(openAIRequest)
        Json.parseToJsonElement(Json.encodeToString(openAIRequest))
        writeToFile("MISSINGSHIT", Json.encodeToString(openAIRequest))
        val maxSize = 1024
        var i = 0
        while (i <= s.length/2) {
            Log.d("openairequest", s.substring(i, Math.min(s.length, i + maxSize)))
            i += maxSize
        }
        Log.d("openairequest", "PART 1 FINISHED PROCEESING THE ENTIRE REQUEST")
        println()
        val response = httpClient.post() {
            headers {
                append("Authorization", "Bearer ${apiKey}")
            }
            setBody(openAIRequest)
        }
        while (i < s.length) {
            Log.d("openairequest", s.substring(i, Math.min(s.length, i + maxSize)))
            i += maxSize
        }
        Log.d("openairequest", "PART 2 FINISHED PROCEESING THE ENTIRE REQUEST")
        println("RESPONSE: " + response.bodyAsText())
        return Result.success(response.body())
    }
    /*
suspend fun sendPrompt(promptRequest: PromptRequest): Result<OpenAIResponse> {
    val messages = promptRequest.messages.map { messageMap ->
        OpenAIRequest.Message(
            role = messageMap["role"] as String,
            content = messageMap["content"] as String
        )
    }

    val request = OpenAIRequest(
        model = promptRequest.model,
        messages = messages
    )
    println(request.toString() + "OPENAI API LINE 29")
    Log.d("tag: ", request.toString())
    val response = httpClient.post() {
        headers {
            append("Authorization", "Bearer ${promptRequest.apiKey}")
        }
        setBody(request)  // Now using the serializable class
    }
    println("LINE 37 IN OPENAI API")
    println(response.bodyAsText())
    Log.d("tag: ", response.bodyAsText())
    return Result.success(response.body())
}

 */
    /*
    suspend fun sendPrompt(promptRequest: PromptRequest): Result<OpenAIResponse> {
        val request = OpenAIRequest(
            model = promptRequest.model,
            messages = promptRequest.messages.map { message ->
                MessageBlock(
                    role = message.role,
                    content = message.content.map { contentBlock ->
                        ContentBlock(
                            type = contentBlock.type,
                            text = contentBlock.text,
                            image_url = contentBlock.image_url
                        )
                    }
                )
            }
        )

        // Logging the request for debugging
        Log.d("OPENAI_API_REQUEST", Json.encodeToString(request))

        return try {
            val response: HttpResponse = httpClient.post {
                headers {
                    append("Authorization", "Bearer ${promptRequest.apiKey}")
                }
                setBody(request)
            }

            val responseBody = response.bodyAsText()
            Log.d("OPENAI_RESPONSE", responseBody)

            if (response.status.isSuccess()) {
                val parsedResponse = Json.decodeFromString<OpenAIResponse>(responseBody)
                Result.success(parsedResponse)
            } else {
                Log.e("OpenAI_API_Error", "Error Response: $responseBody")
                Result.failure(Exception("API Error: $responseBody"))
            }
        } catch (e: Exception) {
            Log.e("OpenAI_API_Exception", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

*/
    /*
    suspend fun sendPromptWithHistory(promptRequest: PromptRequest): Result<OpenAIResponse> {
        // Similar implementation as sendPrompt
        val messages = promptRequest.messages.map { messageMap ->
            OpenAIRequest.Message(
                role = messageMap["role"] as String,
                content = messageMap["content"] as String
            )
        }

        val request = OpenAIRequest(
            model = promptRequest.model,
            messages = messages
        )

        val response = httpClient.post() {
            headers {
                append("Authorization", "Bearer ${promptRequest.apiKey}")
            }
            setBody(request)
        }
        return Result.success(response.body())
    }

     */

}
