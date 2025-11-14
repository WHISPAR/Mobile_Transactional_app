package com.example.regen.managers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date

// DataStore for local caching
val Context.dataStore by preferencesDataStore(name = "user_preferences")

object UserManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // DataStore keys
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")

    // User data class matching your Firestore structure
    data class UserData(
        val name: String = "",
        val email: String = "",
        val balance: Double = 0.0,
        val createdAt: Date = Date()
    )

    data class Transaction(
        val description: String = "",
        val amount: Double = 0.0,
        val timestamp: Date = Date(),
        val type: String = "" // "deposit", "withdrawal", "send"
    )

    // Save user data locally
    suspend fun saveUserDataLocally(context: Context, userId: String, name: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
        }
    }

    // Get locally stored user data
    fun getLocalUserName(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_NAME_KEY]
        }
    }

    fun getLocalUserEmail(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    }

    fun getLocalUserId(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    // Clear local data
    suspend fun clearLocalData(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Firestore operations
    suspend fun createUserInFirestore(userId: String, name: String, email: String): Boolean {
        return try {
            val userData = UserData(
                name = name,
                email = email,
                balance = 0.0,
                createdAt = Date()
            )

            db.collection("users")
                .document(userId)
                .set(userData)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserData(userId: String): UserData? {
        return try {
            val document = db.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                document.toObject(UserData::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserBalance(userId: String, newBalance: Double): Boolean {
        return try {
            val data = mapOf("balance" to newBalance)
            db.collection("users")
                .document(userId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addTransaction(userId: String, transaction: Transaction): Boolean {
        return try {
            db.collection("users")
                .document(userId)
                .collection("transactions")
                .add(transaction)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentUserBalance(userId: String): Double {
        return try {
            val userData = getUserData(userId)
            userData?.balance ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    // Get current authenticated user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Check if user is authenticated
    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }
}