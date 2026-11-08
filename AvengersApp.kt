package com.avengers.ai

import android.app.Application
import android.content.Context
import com.avengers.ai.ai.AiProvider
import com.avengers.ai.ai.ChatRepository
import com.avengers.ai.ai.ProviderFactory
import com.avengers.ai.data.ChatStore
import com.avengers.ai.data.SettingsStore
import com.avengers.ai.phone.ActionExecutor

class AvengersApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Simple manual dependency container (no DI framework, fewer build failures). */
class AppContainer(val appContext: Context) {
    val settings = SettingsStore(appContext)
    val store = ChatStore(appContext)
    val actions = ActionExecutor(appContext)
    val provider: AiProvider = ProviderFactory.create(settings)
    val repo = ChatRepository(settings, store, provider, actions)
}
