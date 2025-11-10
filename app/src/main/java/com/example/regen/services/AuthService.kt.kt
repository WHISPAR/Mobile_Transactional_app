package com.example.regen.services

import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AuthService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser = mutableStateOf(auth.currentUser)
    val isAuthenticated = mutableStateOf(auth.currentUser != null)

    // Sign up with email and password
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String = ""
    ): Result<Boolean> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            // Update user profile with name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            result.user?.updateProfile(profileUpdates)?.await()

            // Create user document in Firestore
            val userData = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "totalBalance" to 0.0,
                "currency" to "MWK"
            )

            db.collection("users").document(result.user!!.uid)
                .set(userData)
                .await()

            currentUser.value = result.user
            isAuthenticated.value = true
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign in with email and password
    suspend fun signIn(email: String, password: String): Result<Boolean> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            currentUser.value = result.user
            isAuthenticated.value = true
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign out
    fun signOut() {
        auth.signOut()
        currentUser.value = null
        isAuthenticated.value = false
    }

    // Get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Reset password
    suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}