package edu.msudenver.cs3013.project03.prefs

import android.app.Application

// TODO-7-25-23: Implement the PreferenceApplication class to store the preferenceWrapper
//  object and initialize it in the onCreate() method
class PreferenceApplication : Application() {

    lateinit var preferenceWrapper: PreferenceWrapper

    // TODO-7-25-23: Implement the onCreate() method to initialize the preferenceWrapper object
    override fun onCreate() {
        super.onCreate()

        // TODO-Completed: Initialize preferenceWrapper object in onCreate() method to save dark mode preference
        preferenceWrapper = PreferenceWrapper(getSharedPreferences("prefs", MODE_PRIVATE))
    }
}
