// services/BudgetService.kt
package com.example.regen.services

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Budget(
    val id: String = "",
    val userId: String = "",
    val category: String = "",
    val icon: String = "",
    val spent: Double = 0.0,
    val total: Double = 0.0,
    val color: String = "#FFC107",
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)

object BudgetService {
    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION_BUDGETS = "budgets"

    // Get all budgets for a user
    suspend fun getUserBudgets(userId: String): List<Budget> {
        return try {
            val query = db.collection(COLLECTION_BUDGETS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            query.documents.map { document ->
                Budget(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    category = document.getString("category") ?: "",
                    icon = document.getString("icon") ?: "shopping_cart",
                    spent = document.getDouble("spent") ?: 0.0,
                    total = document.getDouble("total") ?: 0.0,
                    color = document.getString("color") ?: "#FFC107",
                    createdAt = document.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Add new budget
    suspend fun addBudget(
        userId: String,
        category: String,
        total: Double,
        icon: String = "shopping_cart",
        color: String = "#FFC107"
    ): Result<String> {
        return try {
            val budgetData = hashMapOf(
                "userId" to userId,
                "category" to category,
                "icon" to icon,
                "spent" to 0.0,
                "total" to total,
                "color" to color,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val documentRef = db.collection(COLLECTION_BUDGETS).add(budgetData).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update budget
    suspend fun updateBudget(budgetId: String, updates: Map<String, Any>): Result<Boolean> {
        return try {
            db.collection(COLLECTION_BUDGETS).document(budgetId)
                .update(updates)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete budget
    suspend fun deleteBudget(budgetId: String): Result<Boolean> {
        return try {
            db.collection(COLLECTION_BUDGETS).document(budgetId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}