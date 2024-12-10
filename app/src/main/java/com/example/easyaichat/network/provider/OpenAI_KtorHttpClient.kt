package com.example.easyaichat.network.provider

import com.example.easyaichat.network.routes.openAI_route
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
class OpenAI_KtorHttpClient(
    private val engine: HttpClientEngine,
) {
    fun create(): HttpClient {
        return HttpClient(engine) {

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    //encodeDefaults = true
                })
            }
            defaultRequest {
                url(openAI_route)
                headers {
                    append("Content-Type", "application/json")
                }
            }
        }
    }
}
/*
val factory = HttpClientFactory(AndroidEngine())

// Get client for knowledge management
val kmClient = factory.getClient(ApiRoutes.KnowledgeManagement)

// Get client for provider 1
val provider1Client = factory.getClient(ApiRoutes.Provider1)

// Make requests
kmClient.get("/some-endpoint")
provider1Client.get("/different-endpoint")
 */