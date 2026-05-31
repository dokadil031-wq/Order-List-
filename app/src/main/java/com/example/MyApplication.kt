package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("FATAL_CRASH", "Uncaught exception in thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize Firebase", e)
        }
    }
}
