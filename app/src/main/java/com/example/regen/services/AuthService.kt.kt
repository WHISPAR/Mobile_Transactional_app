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
            // Validate inputs
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Please fill in all fields"))
            }

            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            // Create user in Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            // Update user profile with name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            result.user?.updateProfile(profileUpdates)?.await()

            // Create user document in Firestore
            val userData = hashMapOf(
                "uid" to result.user!!.uid,
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
            // Handle specific Firebase errors
            val errorMessage = when {
                e.message?.contains("email address is already in use") == true ->
                    "This email is already registered. Please sign in instead."
                e.message?.contains("invalid email") == true ->
                    "Please enter a valid email address."
                e.message?.contains("network error") == true ->
                    "Network error. Please check your internet connection."
                else -> e.message ?: "Failed to create account. Please try again."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    // Sign in with email and password
    suspend fun signIn(email: String, password: String): Result<Boolean> {
        return try {
            // Validate inputs
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Please enter email and password"))
            }

            val result = auth.signInWithEmailAndPassword(email, password).await()
            currentUser.value = result.user
            isAuthenticated.value = true
            Result.success(true)
        } catch (e: Exception) {
            // Handle specific Firebase errors
            val errorMessage = when {
                e.message?.contains("invalid credential") == true ->
                    "Invalid email or password. Please try again."
                e.message?.contains("user not found") == true ->
                    "No account found with this email. Please sign up first."
                e.message?.contains("network error") == true ->
                    "Network error. Please check your internet connection."
                else -> e.message ?: "Failed to sign in. Please try again."
            }
            Result.failure(Exception(errorMessage))
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
            if (email.isBlank()) {
                return Result.failure(Exception("Please enter your email address"))
            }
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("user not found") == true ->
                    "No account found with this email address."
                else -> e.message ?: "Failed to send reset email. Please try again."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    // Check if user is logged in
    fun checkAuthState() {
        currentUser.value = auth.currentUser
        isAuthenticated.value = auth.currentUser != null
    }
}