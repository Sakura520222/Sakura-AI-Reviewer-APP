package com.sakura_ai_reviewer

import android.app.Application
import com.sakura_ai_reviewer.core.auth.SessionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SakuraApplication : Application() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager.initialize()
    }
}
