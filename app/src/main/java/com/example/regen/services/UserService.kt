package com.example.regen.services

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val totalBalance: Double,
    val currency: String,
    val createdAt: com.google.firebase.Timestamp
)

object UserService {
    private val db = FirebaseFirestore.getInstance()

    // Get user profile
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val document = db.collection("users").document(uid).get().await()
            if (document.exists()) {
                UserProfile(
                    uid = uid,
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: "",
                    phone = document.getString("phone") ?: "",
                    totalBalance = document.getDouble("totalBalance") ?: 0.0,
                    currency = document.getString("currency") ?: "MWK",
                    createdAt = document.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Update user profile
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Boolean> {
        return try {
            db.collection("users").document(uid)
                .update(updates)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update user balance
    suspend fun updateBalance(uid: String, newBalance: Double): Result<Boolean> {
        return try {
            db.collection("users").document(uid)
                .update("totalBalance", newBalance)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}