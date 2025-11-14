package com.example.regen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.regen.managers.UserManager
import kotlinx.coroutines.launch
import java.util.*

// --- COLOR PALETTE ---
private val YellowPrimary = Color(0xFFFFC107)
private val LightGrayBackground = Color(0xFFF5F5F5)
private val DarkerGrayText = Color(0xFF757575)
private val SuccessGreen = Color(0xFF4CAF50)
private val WarningOrange = Color(0xFFFF9800)
private val ErrorRed = Color(0xFFF44336)

// --- Data Classes ---
data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val icon: ImageVector,
    val spent: Double = 0.0,
    val total: Double,
    val color: Color,
    val createdAt: Date = Date()
)

data class BudgetCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

// Extension functions for conversion
fun Budget.toManagerBudget(): UserManager.Budget {
    return UserManager.Budget(
        id = this.id,
        category = this.category,
        iconName = getIconName(this.icon),
        spent = this.spent,
        total = this.total,
        color = this.color.toArgb(),
        createdAt = this.createdAt
    )
}

fun UserManager.Budget.toUIBudget(): Budget {
    return Budget(
        id = this.id,
        category = this.category,
        icon = getIconFromName(this.iconName),
        spent = this.spent,
        total = this.total,
        color = Color(this.color),
        createdAt = this.createdAt
    )
}

// Helper functions for icon conversion
private fun getIconName(icon: ImageVector): String {
    return when (icon) {
        Icons.Default.ShoppingCart -> "ShoppingCart"
        Icons.Default.Commute -> "Commute"
        Icons.Default.Movie -> "Movie"
        Icons.Default.Lightbulb -> "Lightbulb"
        Icons.Default.LocalHospital -> "LocalHospital"
        Icons.Default.School -> "School"
        Icons.Default.ShoppingBasket -> "ShoppingBasket"
        Icons.Default.Restaurant -> "Restaurant"
        else -> "Category"
    }
}

private fun getIconFromName(iconName: String): ImageVector {
    return when (iconName) {
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Commute" -> Icons.Default.Commute
        "Movie" -> Icons.Default.Movie
        "Lightbulb" -> Icons.Default.Lightbulb
        "LocalHospital" -> Icons.Default.LocalHospital
        "School" -> Icons.Default.School
        "ShoppingBasket" -> Icons.Default.ShoppingBasket
        "Restaurant" -> Icons.Default.Restaurant
        else -> Icons.Default.Category
    }
}

// --- MAIN BUDGET SCREEN COMPOSABLE ---
@Composable
fun BudgetScreen(onBackClick: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf("main") }
    var budgets by remember { mutableStateOf<List<Budget>>(emptyList()) }
    var userBalance by remember { mutableStateOf(0.0) }
    val userId = UserManager.getCurrentUserId()
    val coroutineScope = rememberCoroutineScope()

    // Load user data and budgets
    LaunchedEffect(userId) {
        if (userId != null) {
            // Load user balance
            val userData = UserManager.getUserData(userId)
            userData?.let {
                userBalance = it.balance
            }

            // Load budgets from storage
            val managerBudgets = UserManager.getUserBudgets(userId)
            budgets = managerBudgets.map { it.toUIBudget() }
        }
    }

    when (currentScreen) {
        "main" -> MainBudgetScreen(
            budgets = budgets,
            userBalance = userBalance,
            onBackClick = onBackClick,
            onAddBudgetClick = { currentScreen = "add" },
            onBudgetClick = { budget ->
                // Navigate to budget detail or edit
                currentScreen = "edit"
            }
        )
        "add" -> AddBudgetScreen(
            userBalance = userBalance,
            onBackClick = { currentScreen = "main" },
            onBudgetCreated = { newBudget ->
                coroutineScope.launch {
                    val success = UserManager.saveBudget(newBudget.toManagerBudget())
                    if (success) {
                        budgets = budgets + newBudget
                        currentScreen = "main"
                    }
                }
            }
        )
        "edit" -> EditBudgetScreen(
            onBackClick = { currentScreen = "main" }
        )
    }
}

@Composable
fun MainBudgetScreen(
    budgets: List<Budget>,
    userBalance: Double = 0.0,
    onBackClick: () -> Unit = {},
    onAddBudgetClick: () -> Unit = {},
    onBudgetClick: (Budget) -> Unit = {}
) {
    val totalBudget = budgets.sumOf { it.total }
    val totalSpent = budgets.sumOf { it.spent }
    val remainingBudget = totalBudget - totalSpent

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
                    IconButton(onClick = onAddBudgetClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Budget")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBudgetClick,
                backgroundColor = YellowPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
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
                OverallBudgetSummaryCard(
                    totalSpent = totalSpent,
                    totalBudget = totalBudget,
                    remainingBudget = remainingBudget,
                    userBalance = userBalance
                )
            }

            // --- Section Header: Categories ---
            item {
                Text(
                    text = "BUDGET CATEGORIES",
                    style = MaterialTheme.typography.overline,
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
                    color = DarkerGrayText
                )
            }

            // --- List of Individual Budget Items ---
            if (budgets.isEmpty()) {
                item {
                    EmptyBudgetsState(onAddBudgetClick = onAddBudgetClick)
                }
            } else {
                items(budgets) { budget ->
                    BudgetItemCard(
                        budget = budget,
                        onClick = { onBudgetClick(budget) }
                    )
                }
            }
        }
    }
}

@Composable
fun OverallBudgetSummaryCard(
    totalSpent: Double,
    totalBudget: Double,
    remainingBudget: Double,
    userBalance: Double
) {
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
                text = "Financial Overview",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Wallet Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Wallet Balance", style = MaterialTheme.typography.body2, color = DarkerGrayText)
                Text(
                    "MWK ${"%,.2f".format(userBalance)}",
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    color = if (userBalance > 0) SuccessGreen else Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryItem(
                    label = "Total Budget",
                    value = "MWK ${"%,.2f".format(totalBudget)}",
                    color = YellowPrimary
                )
                SummaryItem(
                    label = "Spent",
                    value = "MWK ${"%,.2f".format(totalSpent)}",
                    color = if (totalSpent > totalBudget) ErrorRed else Color.Black
                )
                SummaryItem(
                    label = "Remaining",
                    value = "MWK ${"%,.2f".format(remainingBudget)}",
                    color = if (remainingBudget >= 0) SuccessGreen else ErrorRed
                )
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color = Color.Black) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = DarkerGrayText,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BudgetItemCard(budget: Budget, onClick: () -> Unit) {
    val progress = (budget.spent / budget.total).toFloat().coerceIn(0f, 1f)
    val amountText = "MWK ${"%,.2f".format(budget.spent)} / ${"%,.2f".format(budget.total)}"
    val isOverBudget = budget.spent > budget.total

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    Text(
                        text = budget.category,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.caption,
                        color = if (isOverBudget) ErrorRed else DarkerGrayText
                    )
                }

                // Percentage
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverBudget) ErrorRed else DarkerGrayText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isOverBudget) ErrorRed else budget.color,
                backgroundColor = budget.color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun EmptyBudgetsState(onAddBudgetClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.PieChart,
                contentDescription = "No Budgets",
                modifier = Modifier.size(60.dp),
                tint = DarkerGrayText.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Budgets Created",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = DarkerGrayText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your first budget to start tracking your expenses",
                style = MaterialTheme.typography.body2,
                color = DarkerGrayText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddBudgetClick,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = YellowPrimary,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Budget")
            }
        }
    }
}

@Composable
fun AddBudgetScreen(
    userBalance: Double = 0.0,
    onBackClick: () -> Unit = {},
    onBudgetCreated: (Budget) -> Unit = {}
) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryType by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val budgetCategories = listOf(
        BudgetCategory("Groceries", Icons.Default.ShoppingCart, Color(0xFF4CAF50)),
        BudgetCategory("Transport", Icons.Default.Commute, Color(0xFF2196F3)),
        BudgetCategory("Entertainment", Icons.Default.Movie, Color(0xFFE91E63)),
        BudgetCategory("Utilities", Icons.Default.Lightbulb, Color(0xFFFF9800)),
        BudgetCategory("Healthcare", Icons.Default.LocalHospital, Color(0xFF9C27B0)),
        BudgetCategory("Education", Icons.Default.School, Color(0xFF607D8B)),
        BudgetCategory("Shopping", Icons.Default.ShoppingBasket, Color(0xFFFF5722)),
        BudgetCategory("Dining", Icons.Default.Restaurant, Color(0xFF795548))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Budget") },
                backgroundColor = YellowPrimary,
                contentColor = Color.Black,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightGrayBackground)
                .padding(16.dp)
        ) {
            Card(
                backgroundColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Create New Budget",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Available Balance
                    Text(
                        text = "Available Balance: MWK ${"%,.2f".format(userBalance)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Error Message
                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = ErrorRed,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Category Selection
                    Text(
                        text = "Select Category",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(budgetCategories) { budgetCategory ->
                            CategoryChip(
                                category = budgetCategory,
                                selected = selectedCategoryType == budgetCategory.name,
                                onClick = {
                                    selectedCategoryType = budgetCategory.name
                                    category = budgetCategory.name
                                    errorMessage = null
                                }
                            )
                        }
                    }

                    // Custom Category Input
                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                            selectedCategoryType = ""
                            errorMessage = null
                        },
                        label = { Text("Custom Category") },
                        placeholder = { Text("Or enter custom category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    // Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            errorMessage = null
                        },
                        label = { Text("Budget Amount") },
                        placeholder = { Text("Enter amount in MWK") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        singleLine = true,
                        leadingIcon = {
                            Text("MWK", modifier = Modifier.padding(end = 8.dp))
                        }
                    )

                    // Create Button
                    Button(
                        onClick = {
                            if (category.isBlank()) {
                                errorMessage = "Please select or enter a category"
                            } else if (amount.isBlank() || amount.toDoubleOrNull() == null) {
                                errorMessage = "Please enter a valid amount"
                            } else if (amount.toDouble() <= 0) {
                                errorMessage = "Amount must be greater than 0"
                            } else {
                                isLoading = true
                                coroutineScope.launch {
                                    val selectedCategory = budgetCategories.find { it.name == category }
                                    val newBudget = Budget(
                                        category = category,
                                        icon = selectedCategory?.icon ?: Icons.Default.Category,
                                        total = amount.toDouble(),
                                        color = selectedCategory?.color ?: YellowPrimary
                                    )

                                    onBudgetCreated(newBudget)
                                    isLoading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = YellowPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Budget",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(category: BudgetCategory, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = if (selected) 4.dp else 1.dp,
        backgroundColor = if (selected) category.color.copy(alpha = 0.2f) else LightGrayBackground
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                category.icon,
                contentDescription = category.name,
                tint = if (selected) category.color else DarkerGrayText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.caption,
                color = if (selected) category.color else DarkerGrayText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EditBudgetScreen(onBackClick: () -> Unit = {}) {
    // Implementation for editing existing budgets
    // Similar to AddBudgetScreen but with pre-filled data
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Budget") },
                backgroundColor = YellowPrimary,
                contentColor = Color.Black,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightGrayBackground),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Edit Budget Screen - Coming Soon")
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun PreviewBudgetScreen() {
    BudgetScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewAddBudgetScreen() {
    AddBudgetScreen(userBalance = 1500.0)
}