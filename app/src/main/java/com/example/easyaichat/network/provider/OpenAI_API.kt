package com.example.easyaichat.network.provider

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
import org.koin.core.component.KoinComponent

class OpenAI_API(private val httpClient: HttpClient) : KoinComponent {

    suspend fun sendPrompt(openAIRequest: OpenAIRequest, apiKey: String): Result<OpenAIResponse> {
        println("UNENCODED: "+ openAIRequest)
        println("REQUEST IN OPENAI API: " + Json.encodeToString(openAIRequest))
        val s = Json.encodeToString(openAIRequest)
        Json.parseToJsonElement(Json.encodeToString(openAIRequest))

        val response = httpClient.post() {
            headers {
                append("Authorization", "Bearer ${apiKey}")
            }
            setBody(openAIRequest)
        }

        Log.d("openairequest", "PART 2 FINISHED PROCEESING THE ENTIRE REQUEST")
        println("RESPONSE: " + response.bodyAsText())
        return Result.success(response.body())
    }

}
