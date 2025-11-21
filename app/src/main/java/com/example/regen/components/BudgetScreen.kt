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
        color = this.color.toArgb(), // Convert Color to Int
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
        color = Color(this.color), // Convert Int to Color
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
        Icons.Default.AccountBalanceWallet -> "AccountBalanceWallet"
        Icons.Default.Savings -> "Savings"
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
        "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
        "Savings" -> Icons.Default.Savings
        else -> Icons.Default.Category
    }
}

// --- MAIN BUDGET SCREEN COMPOSABLE ---
@Composable
fun BudgetScreen(onBackClick: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf("main") }
    var budgets by remember { mutableStateOf<List<Budget>>(emptyList()) }
    var userBalance by remember { mutableStateOf(0.0) }
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }
    val userId = UserManager.getCurrentUserId()
    val coroutineScope = rememberCoroutineScope()

    // Load user data and budgets
    LaunchedEffect(userId) {
        if (userId != null) {
            loadUserData(userId, budgets, userBalance) { updatedBudgets, updatedBalance ->
                budgets = updatedBudgets
                userBalance = updatedBalance
            }
        }
    }

    // Refresh data when returning to main screen
    LaunchedEffect(currentScreen) {
        if (currentScreen == "main" && userId != null) {
            loadUserData(userId, budgets, userBalance) { updatedBudgets, updatedBalance ->
                budgets = updatedBudgets
                userBalance = updatedBalance
            }
        }
    }

    when (currentScreen) {
        "main" -> MainBudgetScreen(
            budgets = budgets,
            userBalance = userBalance,
            onBackClick = onBackClick,
            onAddBudgetClick = { currentScreen = "add" },
            onBudgetClick = { budget ->
                selectedBudget = budget
                currentScreen = "edit"
            },
            onDeleteBudget = { budget ->
                coroutineScope.launch {
                    val success = UserManager.deleteBudget(budget.id)
                    if (success) {
                        budgets = budgets.filter { it.id != budget.id }
                    }
                }
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
            budget = selectedBudget,
            onBackClick = { currentScreen = "main" },
            onBudgetUpdated = { updatedBudget ->
                coroutineScope.launch {
                    val success = UserManager.saveBudget(updatedBudget.toManagerBudget())
                    if (success) {
                        budgets = budgets.map { if (it.id == updatedBudget.id) updatedBudget else it }
                        currentScreen = "main"
                    }
                }
            },
            onBudgetDeleted = { budget ->
                coroutineScope.launch {
                    val success = UserManager.deleteBudget(budget.id)
                    if (success) {
                        budgets = budgets.filter { it.id != budget.id }
                        currentScreen = "main"
                    }
                }
            }
        )
    }
}

private suspend fun loadUserData(
    userId: String,
    currentBudgets: List<Budget>,
    currentBalance: Double,
    onDataLoaded: (List<Budget>, Double) -> Unit
) {
    // Load user balance
    val userData = UserManager.getUserData(userId)
    val updatedBalance = userData?.balance ?: currentBalance

    // Load budgets from Firestore
    val managerBudgets = UserManager.getUserBudgets(userId)
    val updatedBudgets = managerBudgets.map { it.toUIBudget() }

    onDataLoaded(updatedBudgets, updatedBalance)
}

@Composable
fun MainBudgetScreen(
    budgets: List<Budget>,
    userBalance: Double = 0.0,
    onBackClick: () -> Unit = {},
    onAddBudgetClick: () -> Unit = {},
    onBudgetClick: (Budget) -> Unit = {},
    onDeleteBudget: (Budget) -> Unit = {}
) {
    val totalBudget = budgets.sumOf { it.total }
    val totalSpent = budgets.sumOf { it.spent }
    val remainingBudget = totalBudget - totalSpent
    val budgetUtilization = if (totalBudget > 0) (totalSpent / totalBudget) * 100 else 0.0

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
                    userBalance = userBalance,
                    budgetUtilization = budgetUtilization
                )
            }

            // --- Budget Utilization Warning ---
            if (budgetUtilization > 80 && budgets.isNotEmpty()) {
                item {
                    BudgetWarningCard(
                        utilization = budgetUtilization,
                        isOverBudget = remainingBudget < 0
                    )
                }
            }

            // --- Section Header: Categories ---
            if (budgets.isNotEmpty()) {
                item {
                    Text(
                        text = "BUDGET CATEGORIES",
                        style = MaterialTheme.typography.overline,
                        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
                        color = DarkerGrayText
                    )
                }
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
                        onClick = { onBudgetClick(budget) },
                        onDelete = { onDeleteBudget(budget) }
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
    userBalance: Double,
    budgetUtilization: Double
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

            // Progress Bar for Overall Budget
            if (totalBudget > 0) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Budget Utilization", style = MaterialTheme.typography.caption, color = DarkerGrayText)
                        Text(
                            "${budgetUtilization.toInt()}%",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                budgetUtilization > 90 -> ErrorRed
                                budgetUtilization > 75 -> WarningOrange
                                else -> SuccessGreen
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = (budgetUtilization / 100).toFloat().coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            budgetUtilization > 90 -> ErrorRed
                            budgetUtilization > 75 -> WarningOrange
                            else -> SuccessGreen
                        },
                        backgroundColor = LightGrayBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

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
fun BudgetWarningCard(utilization: Double, isOverBudget: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = if (isOverBudget) ErrorRed.copy(alpha = 0.1f) else WarningOrange.copy(alpha = 0.1f),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isOverBudget) Icons.Default.Warning else Icons.Default.Info,
                contentDescription = "Warning",
                tint = if (isOverBudget) ErrorRed else WarningOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOverBudget) "Budget Exceeded!" else "Budget Alert",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverBudget) ErrorRed else WarningOrange
                )
                Text(
                    text = if (isOverBudget)
                        "You have exceeded your total budget. Consider reviewing your spending."
                    else
                        "You've used ${utilization.toInt()}% of your budget. Consider slowing down.",
                    style = MaterialTheme.typography.caption,
                    color = DarkerGrayText
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
fun BudgetItemCard(budget: Budget, onClick: () -> Unit, onDelete: () -> Unit) {
    val progress = (budget.spent / budget.total).toFloat().coerceIn(0f, 1f)
    val amountText = "MWK ${"%,.2f".format(budget.spent)} / ${"%,.2f".format(budget.total)}"
    val isOverBudget = budget.spent > budget.total
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Budget") },
            text = { Text("Are you sure you want to delete the ${budget.category} budget? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

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

                // Percentage and Delete Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) ErrorRed else DarkerGrayText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Budget",
                            tint = ErrorRed.copy(alpha = 0.6f)
                        )
                    }
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
                color = when {
                    isOverBudget -> ErrorRed
                    progress > 0.8 -> WarningOrange
                    else -> budget.color
                },
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
                text = "Create your first budget to start tracking your expenses and manage your spending effectively",
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
        BudgetCategory("Dining", Icons.Default.Restaurant, Color(0xFF795548)),
        BudgetCategory("Deposit", Icons.Default.AccountBalanceWallet, Color(0xFF009688)),
        BudgetCategory("Savings", Icons.Default.Savings, Color(0xFF3F51B5))
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
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = YellowPrimary,
                            focusedLabelColor = YellowPrimary
                        )
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
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = YellowPrimary,
                            focusedLabelColor = YellowPrimary
                        )
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
                            } else if (amount.toDouble() > userBalance) {
                                errorMessage = "Budget amount cannot exceed your available balance"
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
fun EditBudgetScreen(
    budget: Budget?,
    onBackClick: () -> Unit = {},
    onBudgetUpdated: (Budget) -> Unit = {},
    onBudgetDeleted: (Budget) -> Unit = {}
) {
    var category by remember { mutableStateOf(budget?.category ?: "") }
    var amount by remember { mutableStateOf(budget?.total?.toString() ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (budget == null) {
        onBackClick()
        return
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Budget") },
            text = { Text("Are you sure you want to delete the ${budget.category} budget? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBudgetDeleted(budget)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

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
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Budget", tint = ErrorRed)
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
                        text = "Edit Budget",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Current Spending
                    Text(
                        text = "Current Spending: MWK ${"%,.2f".format(budget.spent)}",
                        fontSize = 14.sp,
                        color = DarkerGrayText,
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

                    // Category Input
                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                            errorMessage = null
                        },
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = YellowPrimary,
                            focusedLabelColor = YellowPrimary
                        )
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
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = YellowPrimary,
                            focusedLabelColor = YellowPrimary
                        )
                    )

                    // Update Button
                    Button(
                        onClick = {
                            if (category.isBlank()) {
                                errorMessage = "Please enter a category"
                            } else if (amount.isBlank() || amount.toDoubleOrNull() == null) {
                                errorMessage = "Please enter a valid amount"
                            } else if (amount.toDouble() <= 0) {
                                errorMessage = "Amount must be greater than 0"
                            } else if (amount.toDouble() < budget.spent) {
                                errorMessage = "New budget amount cannot be less than current spending"
                            } else {
                                isLoading = true
                                val updatedBudget = budget.copy(
                                    category = category,
                                    total = amount.toDouble()
                                )
                                onBudgetUpdated(updatedBudget)
                                isLoading = false
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
                                text = "Update Budget",
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

@Preview(showBackground = true)
@Composable
fun PreviewBudgetItemCard() {
    val sampleBudget = Budget(
        category = "Groceries",
        icon = Icons.Default.ShoppingCart,
        spent = 750.0,
        total = 1000.0,
        color = Color(0xFF4CAF50)
    )
    BudgetItemCard(
        budget = sampleBudget,
        onClick = {},
        onDelete = {}
    )
}