package com.example.config

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

object FirebaseAppCheckConfig {
    private const val TAG = "FirebaseAppCheckConfig"
    private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
    private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
    private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
    private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

    fun configureAppCheck(context: Context, intent: Intent?) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }

            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val app = FirebaseApp.getInstance()
                if (BuildConfig.DEBUG) {
                    val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
                    if (!debugToken.isNullOrEmpty()) {
                        val persistenceKey = app.persistenceKey
                        val prefs = context.getSharedPreferences(
                            "$DEBUG_STORE_PREFIX$persistenceKey",
                            Context.MODE_PRIVATE
                        )
                        // Synchronous commit() to prevent race condition
                        prefs.edit().putString(DEBUG_SECRET_KEY, debugToken).commit()
                        Log.d(TAG, "App Check debug token configured and persisted synchronously.")
                    } else {
                        Log.w(TAG, "No FIREBASE_APPCHECK_DEBUG_TOKEN in launch Intent extra.")
                    }

                    FirebaseAppCheck.getInstance(app).installAppCheckProviderFactory(
                        DebugAppCheckProviderFactory.getInstance()
                    )
                    Log.d(TAG, "DebugAppCheckProviderFactory installed for development/VPN environment.")
                } else {
                    val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
                    if (resId != 0) {
                        val siteKey = context.getString(resId)
                        if (siteKey.isNotBlank()) {
                            FirebaseAppCheck.getInstance(app).installAppCheckProviderFactory(
                                RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                            )
                            Log.d(TAG, "RecaptchaAppCheckProviderFactory installed for production.")
                        }
                    } else {
                        FirebaseAppCheck.getInstance(app).installAppCheckProviderFactory(
                            DebugAppCheckProviderFactory.getInstance()
                        )
                        Log.d(TAG, "DebugAppCheckProviderFactory installed as release fallback.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase App Check initialization exception handled safely", e)
        }
    }
}
