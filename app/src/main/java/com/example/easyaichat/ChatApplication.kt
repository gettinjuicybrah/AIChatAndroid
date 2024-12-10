package com.example.easyaichat

import android.app.Application
import com.example.easyaichat.data.database.repository.ChatRepository
import com.example.easyaichat.di.initKoin
import org.koin.android.ext.koin.androidContext

class ChatApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidContext(this@ChatApplication)
        }
    }

}