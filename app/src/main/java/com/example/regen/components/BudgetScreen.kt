package com.example.regen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
// --- COLOR PALETTE (You can move this to a central Theme file later) ---
private val YellowPrimary = Color(0xFFFFC107)
private val LightGrayBackground = Color(0xFFF5F5F5)
private val DarkerGrayText = Color(0xFF757575)

// --- Data Classes for dummy data (replace with your actual data models) ---
data class Budget(
    val category: String,
    val icon: ImageVector,
    val spent: Double,
    val total: Double,
    val color: Color
)

// --- MAIN BUDGET SCREEN COMPOSABLE ---
@Composable
fun BudgetScreen(onBackClick: () -> Unit = {}) {
    // Dummy data for previewing the UI
    val budgets = listOf(
        Budget("Groceries", Icons.Default.ShoppingCart, 250.75, 500.0, Color(0xFF4CAF50)),
        Budget("Transport", Icons.Default.Commute, 120.50, 150.0, Color(0xFF2196F3)),
        Budget("Entertainment", Icons.Default.Movie, 85.0, 200.0, Color(0xFFE91E63)),
        Budget("Utilities", Icons.Default.Lightbulb, 180.25, 180.0, Color(0xFFFF9800))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Budgets") },
                backgroundColor = YellowPrimary,
                contentColor = Color.Black,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Add new budget action */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Budget")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightGrayBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Header Card: Overall Summary ---
            item {
                OverallBudgetSummaryCard(totalSpent = 350.50, totalBudget = 1030.0)
            }

            // --- Section Header: Categories ---
            item {
                Text(
                    text = "CATEGORIES",
                    style = MaterialTheme.typography.overline,
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
                    color = DarkerGrayText
                )
            }

            // --- List of Individual Budget Items ---
            items(budgets) { budget ->
                BudgetItemCard(budget = budget)
            }
        }
    }
}

// --- SUPPORTING COMPOSABLES ---

@Composable
fun OverallBudgetSummaryCard(totalSpent: Double, totalBudget: Double) {
    val remaining = totalBudget - totalSpent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Monthly Summary",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryItem(label = "Spent", value = "MWK ${"%,.2f".format(totalSpent)}")
                SummaryItem(label = "Remaining", value = "MWK ${"%,.2f".format(remaining)}", color = if (remaining >= 0) Color(0xFF4CAF50) else Color.Red)
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color = Color.Black) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.caption, color = DarkerGrayText)
        Text(text = value, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold, color = color)
    }
}


@Composable
fun BudgetItemCard(budget: Budget) {
    val progress = (budget.spent / budget.total).toFloat().coerceIn(0f, 1f)
    val amountText = "MWK ${"%,.2f".format(budget.spent)} / ${"%,.2f".format(budget.total)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(budget.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(budget.icon, contentDescription = budget.category, tint = budget.color)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Category and Amount
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = budget.category, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    Text(text = amountText, style = MaterialTheme.typography.caption, color = DarkerGrayText)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = budget.color,
                backgroundColor = budget.color.copy(alpha = 0.2f)
            )
        }
    }
}


// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun PreviewBudgetScreen() {
    BudgetScreen()
}