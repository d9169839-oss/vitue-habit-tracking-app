package com.virtue.habittracker

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/** Debug builds use a registered Firebase App Check debug token for local testing. */
internal object AppCheckInstaller {
    fun install() {
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
    }
}
