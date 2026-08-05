package com.nch.todoapp.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.nch.todoapp.R
import com.nch.todoapp.data.manager.TodoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    private val todoManager: TodoManager,
    private val sessionManager: SessionManager
) : AuthRepository {

    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            clearLocalGuestFlag()
            publishUser(firebaseUser.toAuthUser())
        } else if (isLocalGuest()) {
            publishUser(localGuestUser())
        } else {
            publishUser(null)
        }
    }

    init {
        // Restore local guest before the first auth callback if needed.
        if (auth.currentUser == null && isLocalGuest()) {
            publishUser(localGuestUser())
        } else {
            publishUser(auth.currentUser?.toAuthUser())
        }
        auth.addAuthStateListener(authStateListener)
    }

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> {
        val activity = context.findActivity()
            ?: return Result.failure(IllegalStateException("Sign-in requires an Activity context"))

        val webClientId = context.getString(R.string.default_web_client_id)
        if (webClientId.startsWith("REPLACE_WITH_") ||
            !webClientId.endsWith(".apps.googleusercontent.com")
        ) {
            return Result.failure(
                IllegalStateException(context.getString(R.string.login_web_client_missing))
            )
        }

        return runCatching {
            clearLocalGuestFlag()
            val idToken = requestGoogleIdToken(activity, webClientId)
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user?.toAuthUser()
                ?: throw IllegalStateException("Firebase returned no user after Google sign-in")
            publishUser(user)
            user
        }.onFailure { e ->
            Log.e(TAG, "Google sign-in failed", e)
        }
    }

    override suspend fun signInAsGuest(): Result<AuthUser> {
        return runCatching {
            // Prefer Firebase Anonymous Auth when enabled in the console.
            val anonymous = runCatching {
                auth.signInAnonymously().await().user
            }.getOrNull()

            val user = if (anonymous != null) {
                clearLocalGuestFlag()
                anonymous.toAuthUser()
            } else {
                // Offline / demo fallback when Anonymous Auth is disabled.
                prefs.edit().putBoolean(KEY_LOCAL_GUEST, true).apply()
                localGuestUser()
            }
            publishUser(user)
            user
        }.onFailure { e ->
            Log.e(TAG, "Guest sign-in failed", e)
        }
    }

    override suspend fun signOut() {
        todoManager.clearSession()
        clearLocalGuestFlag()
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }.onFailure { Log.w(TAG, "Failed to clear credential state", it) }
        auth.signOut()
        publishUser(null)
    }

    private fun publishUser(user: AuthUser?) {
        sessionManager.setUserId(user?.uid)
        _currentUser.value = user
    }

    private fun isLocalGuest(): Boolean = prefs.getBoolean(KEY_LOCAL_GUEST, false)

    private fun clearLocalGuestFlag() {
        prefs.edit().remove(KEY_LOCAL_GUEST).apply()
    }

    private fun localGuestUser(): AuthUser = AuthUser(
        uid = LOCAL_GUEST_UID,
        displayName = "Guest",
        email = null,
        photoUrl = null,
        isGuest = true
    )

    private suspend fun requestGoogleIdToken(activity: Activity, webClientId: String): String {
        return try {
            requestIdToken(
                activity,
                GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(webClientId)
                            .setAutoSelectEnabled(false)
                            .build()
                    )
                    .build()
            )
        } catch (e: GetCredentialException) {
            Log.d(TAG, "GetGoogleIdOption failed, falling back to Sign in with Google", e)
            requestIdToken(
                activity,
                GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetSignInWithGoogleOption.Builder(webClientId).build()
                    )
                    .build()
            )
        }
    }

    private suspend fun requestIdToken(activity: Activity, request: GetCredentialRequest): String {
        val result = credentialManager.getCredential(activity, request)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                return GoogleIdTokenCredential.createFrom(credential.data).idToken
            } catch (e: GoogleIdTokenParsingException) {
                throw IllegalStateException("Invalid Google ID token", e)
            }
        }
        throw IllegalStateException("Unexpected credential type: ${credential::class.java.name}")
    }

    private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
        uid = uid,
        displayName = if (isAnonymous) "Guest" else displayName,
        email = email,
        photoUrl = photoUrl?.toString(),
        isGuest = isAnonymous
    )

    private fun Context.findActivity(): Activity? {
        var current: Context? = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    companion object {
        private const val TAG = "FirebaseAuthRepository"
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_LOCAL_GUEST = "local_guest"
        const val LOCAL_GUEST_UID = "guest_local"
    }
}
