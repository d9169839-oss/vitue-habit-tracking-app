package com.virtue.habittracker.presentation.auth
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.virtue.habittracker.BuildConfig
import kotlinx.coroutines.CancellationException

class GoogleCredentialClient(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    suspend fun getIdToken(): String {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        try {
            val credential = credentialManager.getCredential(context, request).credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return GoogleIdTokenCredential.createFrom(credential.data).idToken
            }
            throw IllegalStateException("Google did not return a supported credential.")
        } catch (error: GoogleIdTokenParsingException) {
            throw IllegalStateException("Could not read the Google sign-in credential.", error)
        } catch (error: CancellationException) {
            throw error
        }
    }
}
