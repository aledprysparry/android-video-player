package com.aledparry.player

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/** Forces the premium dark theme app-wide so system dialogs match the UI. */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
