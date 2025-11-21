package com.example.regen.managers

import android.content.Context
import android.os.Parcelable
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*

object UserManager {
    private val db = FirebaseFirestore.getInstance()

    data class UserData(
        val uid: String = "",
        val name: String = "",
        val email: String = "",
        val phone: String = "",
        val balance: Double = 0.0,
        val currency: String = "MWK",
        val createdAt: Date = Date()
    )

    data class Transaction(
        val id: String = UUID.randomUUID().toString(),
        val description: String,
        val amount: Double,
        val timestamp: Date,
        val type: String, // "deposit", "withdrawal", "send"
        val category: String = "",
        val status: String = "completed"
    )

    // Add Budget data class - Removed Parcelable implementation
    data class Budget(
        val id: String = UUID.randomUUID().toString(),
        val category: String = "",
        val iconName: String = "",
        val spent: Double = 0.0,
        val total: Double = 0.0,
        val color: Int = 0, // Keep as Int for compatibility
        val createdAt: Date = Date()
    )

    // Add PendingDeposit data class
    data class PendingDeposit(
        val id: String = UUID.randomUUID().toString(),
        val userId: String = "",
        val amount: Double = 0.0,
        val method: String = "",
        val reference: String = "",
        val status: String = "pending",
        val createdAt: Date = Date()
    )

    // Create user in Firestore
    suspend fun createUserInFirestore(userId: String, name: String, email: String, phoneNumber: String): Boolean {
        return try {
            val userData = UserData(
                uid = userId,
                name = name,
                email = email,
                phone = phoneNumber,
                balance = 0.0,
                currency = "MWK",
                createdAt = Date()
            )

            val userMap = mapOf(
                "uid" to userId,
                "name" to name,
                "email" to email,
                "phone" to phoneNumber,
                "balance" to 0.0,
                "currency" to "MWK",
                "createdAt" to com.google.firebase.Timestamp(Date().time)
            )

            db.collection("users").document(userId)
                .set(userMap)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Save user data to Firestore
    suspend fun saveUserData(userId: String, userData: UserData): Boolean {
        return try {
            val userMap = mapOf(
                "uid" to userId,
                "name" to userData.name,
                "email" to userData.email,
                "phone" to userData.phone,
                "balance" to userData.balance,
                "currency" to userData.currency,
                "createdAt" to com.google.firebase.Timestamp(userData.createdAt.time)
            )

            db.collection("users").document(userId)
                .set(userMap, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Get user data from Firestore
    suspend fun getUserData(userId: String): UserData? {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                UserData(
                    uid = document.getString("uid") ?: userId,
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: "",
                    phone = document.getString("phone") ?: "",
                    balance = document.getDouble("balance") ?: 0.0,
                    currency = document.getString("currency") ?: "MWK",
                    createdAt = document.getTimestamp("createdAt")?.toDate() ?: Date()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Update user balance in Firestore
    suspend fun updateUserBalance(userId: String, newBalance: Double): Boolean {
        return try {
            val updates = mapOf(
                "balance" to newBalance,
                "lastUpdated" to com.google.firebase.Timestamp.now()
            )

            db.collection("users").document(userId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Add transaction to Firestore
    suspend fun addTransaction(userId: String, transaction: Transaction): Boolean {
        return try {
            val transactionMap = mapOf(
                "id" to transaction.id,
                "description" to transaction.description,
                "amount" to transaction.amount,
                "timestamp" to com.google.firebase.Timestamp(transaction.timestamp.time),
                "type" to transaction.type,
                "category" to transaction.category,
                "status" to transaction.status
            )

            db.collection("users").document(userId)
                .collection("transactions")
                .document(transaction.id)
                .set(transactionMap)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Get recent transactions from Firestore
    suspend fun getRecentTransactions(userId: String, limit: Int = 5): List<Transaction> {
        return try {
            val querySnapshot = db.collection("users").document(userId)
                .collection("transactions")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            querySnapshot.documents.map { document ->
                Transaction(
                    id = document.getString("id") ?: document.id,
                    description = document.getString("description") ?: "",
                    amount = document.getDouble("amount") ?: 0.0,
                    timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date(),
                    type = document.getString("type") ?: "unknown",
                    category = document.getString("category") ?: "",
                    status = document.getString("status") ?: "completed"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // BUDGET-RELATED METHODS

    // Save budget to Firestore
    suspend fun saveBudget(budget: Budget): Boolean {
        return try {
            val budgetMap = mapOf(
                "id" to budget.id,
                "category" to budget.category,
                "iconName" to budget.iconName,
                "spent" to budget.spent,
                "total" to budget.total,
                "color" to budget.color.toLong(), // Convert Int to Long for Firestore
                "createdAt" to com.google.firebase.Timestamp(budget.createdAt.time)
            )

            val userId = getCurrentUserId()
            if (userId != null) {
                db.collection("users").document(userId)
                    .collection("budgets")
                    .document(budget.id)
                    .set(budgetMap)
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Get user budgets from Firestore
    suspend fun getUserBudgets(userId: String): List<Budget> {
        return try {
            val querySnapshot = db.collection("users").document(userId)
                .collection("budgets")
                .get()
                .await()

            querySnapshot.documents.map { document ->
                Budget(
                    id = document.getString("id") ?: document.id,
                    category = document.getString("category") ?: "",
                    iconName = document.getString("iconName") ?: "",
                    spent = document.getDouble("spent") ?: 0.0,
                    total = document.getDouble("total") ?: 0.0,
                    color = getColorFromDocument(document),
                    createdAt = document.getTimestamp("createdAt")?.toDate() ?: Date()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Helper function to safely extract color from document
    private fun getColorFromDocument(document: com.google.firebase.firestore.DocumentSnapshot): Int {
        return try {
            // Try to get as Long first (Firestore might store numbers as Long)
            document.getLong("color")?.toInt() ?:
            // Fallback to Double
            document.getDouble("color")?.toInt() ?:
            // Final fallback
            0
        } catch (e: Exception) {
            0
        }
    }

    // Delete budget from Firestore
    suspend fun deleteBudget(budgetId: String): Boolean {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                db.collection("users").document(userId)
                    .collection("budgets")
                    .document(budgetId)
                    .delete()
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Update budget spending
    suspend fun updateBudgetSpending(budgetId: String, newSpentAmount: Double): Boolean {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val updates = mapOf(
                    "spent" to newSpentAmount
                )

                db.collection("users").document(userId)
                    .collection("budgets")
                    .document(budgetId)
                    .update(updates)
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // NEW METHODS FOR HOMESCREEN FUNCTIONALITY

    // Detect transaction category based on description and recipient
    fun detectTransactionCategory(description: String, recipient: String = ""): String {
        val desc = description.lowercase()

        return when {
            desc.contains("grocery") || desc.contains("food") || desc.contains("market") -> "Groceries"
            desc.contains("transport") || desc.contains("bus") || desc.contains("taxi") || desc.contains("fuel") -> "Transport"
            desc.contains("utility") || desc.contains("electric") || desc.contains("water") || desc.contains("bill") -> "Utilities"
            desc.contains("entertain") || desc.contains("movie") || desc.contains("game") -> "Entertainment"
            desc.contains("health") || desc.contains("medical") || desc.contains("hospital") -> "Healthcare"
            desc.contains("education") || desc.contains("school") || desc.contains("book") -> "Education"
            desc.contains("shopping") || desc.contains("cloth") || desc.contains("store") -> "Shopping"
            desc.contains("restaurant") || desc.contains("dining") || desc.contains("cafe") -> "Dining"
            desc.contains("send") || desc.contains("transfer") -> "Transfer"
            desc.contains("withdraw") -> "Withdrawal"
            desc.contains("deposit") -> "Deposit"
            else -> "Other"
        }
    }

    // Check if user can spend in a category based on budget constraints
    suspend fun canSpendInCategory(userId: String, category: String, amount: Double): Boolean {
        return try {
            val budgets = getUserBudgets(userId)
            val categoryBudget = budgets.find { it.category == category }

            categoryBudget?.let { budget ->
                // Check if the new spending would exceed the budget
                (budget.spent + amount) <= budget.total
            } ?: true // If no budget exists for this category, allow spending
        } catch (e: Exception) {
            e.printStackTrace()
            true // Allow spending if there's an error
        }
    }

    // Update budget spending for a transaction
    suspend fun updateBudgetSpendingForTransaction(userId: String, category: String, amount: Double): Boolean {
        return try {
            val budgets = getUserBudgets(userId)
            val categoryBudget = budgets.find { it.category == category }

            categoryBudget?.let { budget ->
                val newSpentAmount = budget.spent + amount
                updateBudgetSpending(budget.id, newSpentAmount)
            } ?: true // If no budget exists, nothing to update
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Generate deposit reference
    fun generateDepositReference(userId: String): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "DEP${userId.take(4)}${timestamp.toString().takeLast(6)}$random"
    }

    // Create pending deposit
    suspend fun createPendingDeposit(pendingDeposit: PendingDeposit): Boolean {
        return try {
            val depositMap = mapOf(
                "id" to pendingDeposit.id,
                "userId" to pendingDeposit.userId,
                "amount" to pendingDeposit.amount,
                "method" to pendingDeposit.method,
                "reference" to pendingDeposit.reference,
                "status" to pendingDeposit.status,
                "createdAt" to com.google.firebase.Timestamp(pendingDeposit.createdAt.time)
            )

            db.collection("pending_deposits")
                .document(pendingDeposit.id)
                .set(depositMap)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Complete deposit
    suspend fun completeDeposit(depositId: String): Boolean {
        return try {
            // Get the pending deposit
            val depositDoc = db.collection("pending_deposits").document(depositId).get().await()
            if (!depositDoc.exists()) return false

            val userId = depositDoc.getString("userId") ?: return false
            val amount = depositDoc.getDouble("amount") ?: 0.0

            // Update deposit status
            db.collection("pending_deposits").document(depositId)
                .update("status", "completed")
                .await()

            // Get current user balance
            val userData = getUserData(userId)
            val currentBalance = userData?.balance ?: 0.0

            // Update user balance
            val newBalance = currentBalance + amount
            val balanceUpdated = updateUserBalance(userId, newBalance)

            if (balanceUpdated) {
                // Add transaction record
                val transaction = Transaction(
                    description = "Deposit completed",
                    amount = amount,
                    timestamp = Date(),
                    type = "deposit",
                    category = "Deposit"
                )
                addTransaction(userId, transaction)
            }

            balanceUpdated
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Get monthly spending by category
    suspend fun getMonthlySpending(userId: String, month: Int, year: Int): Map<String, Double> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1, 0, 0, 0)
            val startOfMonth = calendar.time

            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.SECOND, -1)
            val endOfMonth = calendar.time

            val querySnapshot = db.collection("users").document(userId)
                .collection("transactions")
                .whereGreaterThanOrEqualTo("timestamp", com.google.firebase.Timestamp(startOfMonth.time))
                .whereLessThanOrEqualTo("timestamp", com.google.firebase.Timestamp(endOfMonth.time))
                .get()
                .await()

            val spendingByCategory = mutableMapOf<String, Double>()

            querySnapshot.documents.forEach { document ->
                val category = document.getString("category") ?: "Other"
                val amount = document.getDouble("amount") ?: 0.0
                val type = document.getString("type") ?: ""

                // Only count spending (withdrawals and sends), not deposits
                if (type == "withdrawal" || type == "send") {
                    spendingByCategory[category] = spendingByCategory.getOrDefault(category, 0.0) + amount
                }
            }

            spendingByCategory
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    // Get current user ID
    fun getCurrentUserId(): String? {
        // This should return the Firebase Auth current user ID
        return try {
            com.example.regen.services.AuthService.getCurrentUserId()
        } catch (e: Exception) {
            // Fallback for preview/testing
            "test_user_id"
        }
    }

    // Local storage functions
    fun saveUserDataLocally(context: Context, userId: String, userName: String, userEmail: String) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_id", userId)
            putString("user_name", userName)
            putString("user_email", userEmail)
            apply()
        }
    }

    fun getLocalUserName(context: Context): Flow<String?> = flow {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        emit(sharedPref.getString("user_name", null))
    }

    // Add the missing method for getting local user email
    fun getLocalUserEmail(context: Context): Flow<String?> = flow {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        emit(sharedPref.getString("user_email", null))
    }
}