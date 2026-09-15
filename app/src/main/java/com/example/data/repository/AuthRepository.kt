package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.data.local.AppPreferences
import com.example.data.local.UserAccountDao
import com.example.data.local.UserAccountEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context,
    private val appPreferences: AppPreferences,
    private val userAccountDao: UserAccountDao
) {
    companion object {
        private const val TAG = "AuthRepository"
        // Firebase Web Client ID configured for genzii-a3240
        const val WEB_CLIENT_ID = "501442912601-bh46edlrik8hb3kvsgh4vquhinds0ml5.apps.googleusercontent.com"
    }

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val currentFirebaseUser: FirebaseUser?
        get() = try {
            firebaseAuth.currentUser
        } catch (e: Exception) {
            null
        }

    fun syncCurrentUserOnStartup() {
        try {
            val user = currentFirebaseUser
            if (user != null) {
                val email = user.email ?: "user@genzii.app"
                val name = user.displayName?.takeIf { it.isNotBlank() }
                    ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                val avatar = user.photoUrl?.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    appPreferences.setUserProfile(name, email, avatar)
                    appPreferences.setLoggedIn(true)
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    appPreferences.setLoggedIn(false)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync current user on startup: ${e.message}")
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initiating Google Sign-In with Web Client ID: $WEB_CLIENT_ID")
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(context = activityContext, request = request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.d(TAG, "Received Google ID token, authenticating with Firebase...")

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    handleSuccessfulAuth(user, "google")
                    return@withContext Result.success(Unit)
                }
            }
            Result.failure(Exception("Could not retrieve Google account credentials."))
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Result.failure(Exception("Google Sign-In was cancelled."))
        } catch (e: Exception) {
            Log.w(TAG, "Google Credential Manager error: ${e.message}")
            Result.failure(Exception(e.localizedMessage ?: "Google Sign-In failed. Please try again or use Email."))
        }
    }

    suspend fun signInWithFacebook(activityContext: Context): Result<Unit> = withContext(Dispatchers.IO) {
        Result.failure(Exception("Facebook sign-in requires Facebook provider setup. Please sign in with Email or Google."))
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(Exception("Please enter a valid email address."))
        }
        if (trimmedPass.isBlank()) {
            return@withContext Result.failure(Exception("Please enter your password."))
        }

        try {
            Log.d(TAG, "Attempting Firebase signInWithEmailAndPassword for $trimmedEmail")
            val authResult = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
            val user = authResult.user ?: throw Exception("Authentication returned no user.")
            handleSuccessfulAuth(user, "email")
            Log.d(TAG, "Firebase signInWithEmailAndPassword succeeded for ${user.uid}")
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.w(TAG, "FirebaseAuthInvalidUserException: ${e.message}")
            Result.failure(Exception("No account found with this email. Please create an account."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.w(TAG, "FirebaseAuthInvalidCredentialsException: ${e.message}")
            Result.failure(Exception("Incorrect password or email. Please verify your credentials."))
        } catch (e: com.google.firebase.FirebaseTooManyRequestsException) {
            Log.w(TAG, "FirebaseTooManyRequestsException: ${e.message}")
            Result.failure(Exception("Too many failed attempts. Please try again later."))
        } catch (e: com.google.firebase.FirebaseNetworkException) {
            Log.w(TAG, "FirebaseNetworkException: ${e.message}")
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signIn error: ${e.message}")
            val msg = e.localizedMessage ?: "Sign in failed. Please try again."
            Result.failure(Exception(msg))
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        val displayName = name.trim().ifBlank { trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() } }

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(Exception("Please enter a valid email address."))
        }
        if (trimmedPass.length < 6) {
            return@withContext Result.failure(Exception("Password must be at least 6 characters long."))
        }

        try {
            Log.d(TAG, "Attempting Firebase createUserWithEmailAndPassword for $trimmedEmail")
            val createResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
            val user = createResult.user ?: throw Exception("Account creation returned no user.")
            try {
                user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(displayName).build()).await()
            } catch (profileEx: Exception) {
                Log.w(TAG, "Profile update exception: ${profileEx.message}")
            }
            handleSuccessfulAuth(user, "email", customName = displayName)
            Log.d(TAG, "Firebase createUserWithEmailAndPassword succeeded for ${user.uid}")
            Result.success(Unit)
        } catch (collision: FirebaseAuthUserCollisionException) {
            Log.w(TAG, "FirebaseAuthUserCollisionException: ${collision.message}")
            Result.failure(Exception("An account already exists with this email. Please sign in instead."))
        } catch (weakPass: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
            Log.w(TAG, "FirebaseAuthWeakPasswordException: ${weakPass.message}")
            Result.failure(Exception("Password is too weak. Please use at least 6 characters."))
        } catch (invalidCreds: FirebaseAuthInvalidCredentialsException) {
            Log.w(TAG, "FirebaseAuthInvalidCredentialsException: ${invalidCreds.message}")
            Result.failure(Exception("The email address is improperly formatted."))
        } catch (netEx: com.google.firebase.FirebaseNetworkException) {
            Log.w(TAG, "FirebaseNetworkException: ${netEx.message}")
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Log.w(TAG, "Firebase createUserWithEmailAndPassword error: ${e.message}")
            val msg = e.localizedMessage ?: "Sign up failed. Please try again."
            Result.failure(Exception(msg))
        }
    }

    private suspend fun handleSuccessfulAuth(
        user: FirebaseUser,
        provider: String,
        customName: String? = null,
        customEmail: String? = null
    ) {
        val email = customEmail ?: user.email ?: "user@genzii.app"
        val displayName = customName?.takeIf { it.isNotBlank() }
            ?: user.displayName?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val avatarUrl = user.photoUrl?.toString()

        appPreferences.setUserProfile(displayName, email, avatarUrl)
        appPreferences.setLoggedIn(true)
        appPreferences.setCompletedOnboarding(true)
        userAccountDao.insertUser(
            UserAccountEntity(
                email = email,
                uid = user.uid,
                name = displayName,
                avatarUrl = avatarUrl,
                isPro = true,
                authProvider = provider
            )
        )
    }

    suspend fun resetPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !trimmed.contains("@")) {
            return@withContext Result.failure(Exception("Please enter a valid email address."))
        }
        try {
            firebaseAuth.sendPasswordResetEmail(trimmed).await()
            Result.success("Password reset instructions sent to $trimmed")
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.w(TAG, "Firebase resetPassword user not found: ${e.message}")
            Result.failure(Exception("No account found with this email."))
        } catch (e: Exception) {
            Log.w(TAG, "Firebase resetPassword error: ${e.message}")
            Result.failure(Exception(e.localizedMessage ?: "Failed to send reset email. Please try again."))
        }
    }

    suspend fun updatePassword(newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (newPass.length < 6) {
            return@withContext Result.failure(Exception("Password must be at least 6 characters."))
        }
        try {
            val user = currentFirebaseUser ?: throw Exception("User is not authenticated.")
            user.updatePassword(newPass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase updatePassword: ${e.message}")
            Result.failure(Exception(e.localizedMessage ?: "Failed to update password."))
        }
    }

    suspend fun updateDisplayName(name: String, email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = currentFirebaseUser
            if (user != null) {
                user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase updateProfile: ${e.message}")
        }
        appPreferences.setUserProfile(name, email)
        userAccountDao.updateProfile(email, name)
        Result.success(Unit)
    }

    suspend fun updateAvatar(avatarUri: String, email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = currentFirebaseUser
            if (user != null && avatarUri.isNotBlank()) {
                val uri = android.net.Uri.parse(avatarUri)
                user.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(uri).build()).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase updatePhotoUri: ${e.message}")
        }
        appPreferences.updateUserAvatar(avatarUri)
        userAccountDao.updateAvatar(email, avatarUri)
        Result.success(Unit)
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signOut: ${e.message}")
        }
        appPreferences.clearSession()
        Result.success(Unit)
    }

    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = currentFirebaseUser
            if (user != null) {
                user.delete().await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase delete user: ${e.message}")
        }
        appPreferences.clearSession()
        Result.success(Unit)
    }
}
