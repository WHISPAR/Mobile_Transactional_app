package com.example.regen.managers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.*

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
        val phoneNumber: String = "",
        val createdAt: Date = Date()
    )

    data class Transaction(
        val description: String = "",
        val amount: Double = 0.0,
        val timestamp: Date = Date(),
        val type: String = "", // "deposit", "withdrawal", "send"
        val category: String = "",
        val status: String = "completed" // "pending", "completed", "failed"
    )

    data class Budget(
        val id: String = UUID.randomUUID().toString(),
        val category: String,
        val iconName: String, // Store icon as string for Firestore
        val spent: Double = 0.0,
        val total: Double,
        val color: Int, // Store color as Int for Firestore
        val createdAt: Date = Date(),
        val userId: String = ""
    )

    // NEW: Pending deposit for real money processing
    data class PendingDeposit(
        val id: String = UUID.randomUUID().toString(),
        val userId: String,
        val amount: Double,
        val method: String, // "airtel", "mpamba", "bank"
        val reference: String,
        val status: String = "pending", // "pending", "verified", "completed", "failed"
        val createdAt: Date = Date(),
        val verifiedAt: Date? = null,
        val completedAt: Date? = null
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
    suspend fun createUserInFirestore(userId: String, name: String, email: String, phoneNumber: String = ""): Boolean {
        return try {
            val userData = UserData(
                name = name,
                email = email,
                phoneNumber = phoneNumber,
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

    suspend fun getRecentTransactions(userId: String, limit: Int): List<Transaction> {
        return try {
            val querySnapshot = db.collection("users")
                .document(userId)
                .collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Transaction::class.java)
            }
        } catch (e: Exception) {
            // Return empty list if there's an error or no transactions
            emptyList()
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

    // NEW: Deposit-specific functions
    suspend fun createPendingDeposit(deposit: PendingDeposit): Boolean {
        return try {
            db.collection("pending_deposits")
                .document(deposit.id)
                .set(deposit)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getPendingDeposits(userId: String): List<PendingDeposit> {
        return try {
            val querySnapshot = db.collection("pending_deposits")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(PendingDeposit::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun completeDeposit(depositId: String): Boolean {
        return try {
            // Get the pending deposit
            val depositDoc = db.collection("pending_deposits").document(depositId).get().await()
            val deposit = depositDoc.toObject(PendingDeposit::class.java)

            deposit?.let { pendingDeposit ->
                // Update user balance
                val userData = getUserData(pendingDeposit.userId)
                val newBalance = (userData?.balance ?: 0.0) + pendingDeposit.amount
                val balanceUpdated = updateUserBalance(pendingDeposit.userId, newBalance)

                if (balanceUpdated) {
                    // Add transaction record
                    val transaction = Transaction(
                        description = "Deposit via ${pendingDeposit.method.uppercase()}",
                        amount = pendingDeposit.amount,
                        timestamp = Date(),
                        type = "deposit",
                        category = "deposit",
                        status = "completed"
                    )
                    addTransaction(pendingDeposit.userId, transaction)

                    // Update budget if category exists
                    updateBudgetForDeposit(pendingDeposit.userId, pendingDeposit.amount)

                    // Mark deposit as completed
                    db.collection("pending_deposits")
                        .document(depositId)
                        .update(
                            mapOf(
                                "status" to "completed",
                                "completedAt" to Date()
                            )
                        )
                        .await()

                    true
                } else {
                    false
                }
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateBudgetForDeposit(userId: String, amount: Double) {
        try {
            val budgets = getUserBudgets(userId)
            val depositBudget = budgets.find { it.category.equals("deposit", ignoreCase = true) }

            depositBudget?.let { budget ->
                val newSpent = budget.spent + amount
                db.collection("budgets")
                    .document(budget.id)
                    .update("spent", newSpent)
                    .await()
            }
        } catch (e: Exception) {
            // Silently fail - budget update is optional for deposits
        }
    }

    // NEW: Budget enforcement functions
    suspend fun canSpendInCategory(userId: String, category: String, amount: Double): Boolean {
        return try {
            val budgets = getUserBudgets(userId)
            val categoryBudget = budgets.find { it.category.equals(category, ignoreCase = true) }

            categoryBudget?.let { budget ->
                // Check if spending this amount would exceed budget
                (budget.spent + amount) <= budget.total
            } ?: true // No budget for this category, allow spending
        } catch (e: Exception) {
            true // If there's an error, allow the transaction
        }
    }

    suspend fun updateBudgetSpendingForTransaction(userId: String, category: String, amount: Double): Boolean {
        return try {
            val budgets = getUserBudgets(userId)
            val relevantBudget = budgets.find { it.category.equals(category, ignoreCase = true) }

            relevantBudget?.let { budget ->
                val newSpent = budget.spent + amount
                db.collection("budgets")
                    .document(budget.id)
                    .update("spent", newSpent)
                    .await()
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    // NEW: Transaction category detection
    fun detectTransactionCategory(description: String, recipient: String = ""): String {
        val desc = description.lowercase()
        val recip = recipient.lowercase()

        return when {
            desc.contains("restaurant") || desc.contains("dining") ||
                    desc.contains("food") || desc.contains("cafe") ||
                    recip.contains("restaurant") || recip.contains("dining") -> "Dining"

            desc.contains("grocery") || desc.contains("supermarket") ||
                    desc.contains("market") -> "Groceries"

            desc.contains("transport") || desc.contains("bus") ||
                    desc.contains("taxi") || desc.contains("fuel") -> "Transport"

            desc.contains("movie") || desc.contains("entertainment") ||
                    desc.contains("netflix") || desc.contains("show") -> "Entertainment"

            desc.contains("utility") || desc.contains("electricity") ||
                    desc.contains("water") || desc.contains("bill") -> "Utilities"

            desc.contains("shopping") || desc.contains("mall") ||
                    desc.contains("store") -> "Shopping"

            desc.contains("deposit") -> "Deposit"

            else -> "Other"
        }
    }

    // Budget functions
    suspend fun saveBudget(budget: Budget): Boolean {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                db.collection("budgets")
                    .document(budget.id)
                    .set(budget.copy(userId = userId))
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserBudgets(userId: String): List<Budget> {
        return try {
            val querySnapshot = db.collection("budgets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Budget::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateBudgetSpending(budgetId: String, amount: Double): Boolean {
        return try {
            // Update budget spending when transactions occur
            val budgetDoc = db.collection("budgets").document(budgetId).get().await()
            val budget = budgetDoc.toObject(Budget::class.java)
            budget?.let {
                val newSpent = it.spent + amount
                db.collection("budgets")
                    .document(budgetId)
                    .update("spent", newSpent)
                    .await()
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteBudget(budgetId: String): Boolean {
        return try {
            db.collection("budgets")
                .document(budgetId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // NEW: Financial analytics
    suspend fun getMonthlySpending(userId: String, month: Int, year: Int): Map<String, Double> {
        return try {
            val startDate = Calendar.getInstance().apply {
                set(year, month, 1, 0, 0, 0)
            }.time

            val endDate = Calendar.getInstance().apply {
                set(year, month + 1, 1, 0, 0, 0)
            }.time

            val querySnapshot = db.collection("users")
                .document(userId)
                .collection("transactions")
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThan("timestamp", endDate)
                .whereEqualTo("type", "send")
                .get()
                .await()

            val spendingByCategory = mutableMapOf<String, Double>()

            querySnapshot.documents.forEach { document ->
                val transaction = document.toObject(Transaction::class.java)
                transaction?.let {
                    val category = it.category.ifEmpty { "Other" }
                    spendingByCategory[category] = spendingByCategory.getOrDefault(category, 0.0) + it.amount
                }
            }

            spendingByCategory
        } catch (e: Exception) {
            emptyMap()
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

    // NEW: Generate unique reference for deposits
    fun generateDepositReference(userId: String): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        val userPart = userId.takeLast(4).uppercase()
        return "REGEN${userPart}${timestamp}"
    }
}