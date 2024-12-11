package com.example.easyaichat.di

import com.example.easyaichat.data.database.ChatDatabase
import com.example.easyaichat.data.database.DBFactory
import com.example.easyaichat.data.database.repository.ChatRepository
import com.example.easyaichat.data.database.repository.ChatRepositoryInterface
import com.example.easyaichat.network.provider.OpenAI_KtorHttpClient
import com.example.easyaichat.ui.viewmodel.*
import io.ktor.client.HttpClient
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import com.example.easyaichat.network.LLM_APIService

import com.example.easyaichat.network.provider.OpenAI_API
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel

val platformModule = module {

    single {
        OkHttpClient.Builder()
            .build()
    }

    single{
        DBFactory(androidContext())
    }
    single<ChatDatabase>{
        val dbFactory: DBFactory = get()
        dbFactory.createDatabase()
    }

    single { get<ChatDatabase>().getChatDao()}

    singleOf(::ChatRepository).bind<ChatRepositoryInterface>()

    viewModelOf(::ChatListViewModel)

    viewModel { parameters ->
        ChatViewModel(chatId = parameters.getOrNull())
    }

    viewModelOf(::SettingsViewModel)
    single {
        OpenAI_KtorHttpClient(OkHttp.create())
    }
    single<HttpClient>{
        val openAIClient: OpenAI_KtorHttpClient = get()
        openAIClient.create()
    }
    single { androidContext().contentResolver }
    singleOf(::LLM_APIService)
    single{OpenAI_API(get())}

}