package com.example.xcuci

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.xcuci.utils.PreferenceManager
import java.util.Locale

class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val preferenceManager = PreferenceManager(newBase)
        val languageCode = preferenceManager.getAppLanguage()
        val context = updateLokal(newBase, languageCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        updateBahasa(this)
    }

    private fun updateLokal(context: Context, bahasaKode: String): Context {
        val lokal = Locale(bahasaKode)
        Locale.setDefault(lokal)
        val resources: Resources = context.resources
        val konfig: Configuration = resources.configuration
        konfig.setLocale(lokal)
        konfig.setLayoutDirection(lokal)

        return context.createConfigurationContext(konfig)
    }

    private fun updateActivity(activity: Activity) {
        val preferenceManager = PreferenceManager(activity)
        val languageCode = preferenceManager.getAppLanguage()
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = activity.resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun updateBahasa(activity: Activity) {
        updateActivity(activity)
    }
}