package com.example.regen.services

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Budget(
    val id: String,
    val userId: String,
    val category: String,
    val icon: String,
    val spent: Double,
    val total: Double,
    val color: String,
    val createdAt: com.google.firebase.Timestamp
)

object BudgetService {
    private val db = FirebaseFirestore.getInstance()

    // Get all budgets for a user
    suspend fun getUserBudgets(userId: String): List<Budget> {
        return try {
            val query = db.collection("budgets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            query.documents.map { document ->
                Budget(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    category = document.getString("category") ?: "",
                    icon = document.getString("icon") ?: "",
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
    suspend fun addBudget(budget: Budget): Result<Boolean> {
        return try {
            val budgetData = hashMapOf(
                "userId" to budget.userId,
                "category" to budget.category,
                "icon" to budget.icon,
                "spent" to budget.spent,
                "total" to budget.total,
                "color" to budget.color,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            db.collection("budgets").add(budgetData).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update budget
    suspend fun updateBudget(budgetId: String, updates: Map<String, Any>): Result<Boolean> {
        return try {
            db.collection("budgets").document(budgetId)
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
            db.collection("budgets").document(budgetId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}