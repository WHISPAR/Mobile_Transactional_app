package com.example.regen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.regen.managers.UserManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ---------- COLOR PALETTE ----------
private val YellowPrimary = Color(0xFFFFC107)
private val YellowCard = Color(0xFFFFEB3B)
private val LightGrayBackground = Color(0xFFF5F5F5)

// ---------- MAIN SCREEN -----THE FRONT PAGE-----
@Composable
fun HomeScreen(
    onWalletClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf("main") }
    var userName by remember { mutableStateOf("Loading...") }
    var userBalance by remember { mutableStateOf(0.0) }
    var userId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Load user data
    LaunchedEffect(Unit) {
        // Get current user ID
        userId = UserManager.getCurrentUserId()

        if (userId != null) {
            // Try to get user data from Firestore
            val userData = UserManager.getUserData(userId!!)
            userData?.let {
                userName = it.name.ifEmpty { "User" }
                userBalance = it.balance

                // Save to local storage
                UserManager.saveUserDataLocally(context, userId!!, it.name, it.email)
            }

            // Fallback to local data if Firestore fails
            if (userName == "Loading...") {
                UserManager.getLocalUserName(context).collect { localName ->
                    localName?.let {
                        userName = it
                    }
                }
            }
        }
    }

    when (currentScreen) {
        "main" -> MainHomeScreen(
            userName = userName,
            userBalance = userBalance,
            onWalletClick = onWalletClick,
            onBudgetClick = onBudgetClick,
            onSettingsClick = onSettingsClick,
            onNotificationsClick = onNotificationsClick,
            onSendClick = { currentScreen = "send" },
            onDepositClick = {
                currentScreen = "deposit"
            },
            onWithdrawClick = {
                currentScreen = "withdraw"
            },
            onReportsClick = { currentScreen = "reports" }
        )
        "send" -> HomeSendScreen(
            userId = userId,
            currentBalance = userBalance,
            onBackClick = { currentScreen = "main" },
            onBalanceUpdate = { newBalance ->
                userBalance = newBalance
            }
        )
        "deposit" -> HomeDepositScreen(
            userId = userId,
            currentBalance = userBalance,
            onBackClick = { currentScreen = "main" },
            onBalanceUpdate = { newBalance ->
                userBalance = newBalance
            }
        )
        "withdraw" -> HomeWithdrawScreen(
            userId = userId,
            currentBalance = userBalance,
            onBackClick = { currentScreen = "main" },
            onBalanceUpdate = { newBalance ->
                userBalance = newBalance
            }
        )
        "reports" -> HomeReportsScreen(
            userId = userId,
            onBackClick = { currentScreen = "main" }
        )
    }
}

@Composable
fun MainHomeScreen(
    userName: String = "User",
    userBalance: Double = 0.0,
    onWalletClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onDepositClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onReportsClick: () -> Unit = {}
) {
    var recentTransactions by remember { mutableStateOf<List<UserManager.Transaction>>(emptyList()) }
    val userId = UserManager.getCurrentUserId()

    // Load recent transactions
    LaunchedEffect(userId) {
        if (userId != null) {
            recentTransactions = UserManager.getRecentTransactions(userId, 3) // Get last 3 transactions
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                backgroundColor = YellowPrimary,
                contentColor = Color.Black,
                elevation = 4.dp,
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = { /* Already on home */ },
                onWalletClick = onWalletClick,
                onBudgetClick = onBudgetClick,
                onSettingsClick = onSettingsClick
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

            // Balance Section
            Card(
                backgroundColor = YellowCard,
                shape = RoundedCornerShape(12.dp),
                elevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Balance",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                    Text(
                        text = "MWK ${String.format("%.2f", userBalance)}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = userName.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }

            // Main Action Buttons
            ActionButtonsGrid(
                onSendClick = onSendClick,
                onDepositClick = onDepositClick,
                onWithdrawClick = onWithdrawClick,
                onReportsClick = onReportsClick
            )

            // Recent Transactions Section
            Card(
                backgroundColor = YellowCard,
                shape = RoundedCornerShape(12.dp),
                elevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header with title and view all button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        TextButton(
                            onClick = onReportsClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "View All",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Transactions List
                    if (recentTransactions.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Receipt,
                                contentDescription = "No transactions",
                                tint = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No recent transactions",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            recentTransactions.forEachIndexed { index, transaction ->
                                TransactionItem(transaction = transaction)
                                if (index < recentTransactions.size - 1) {
                                    Divider(
                                        color = Color.Black.copy(alpha = 0.1f),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- TRANSACTION ITEM COMPOSABLE ----------
@Composable
fun TransactionItem(transaction: UserManager.Transaction) {
    val isIncome = transaction.type == "deposit"
    val amountColor = if (isIncome) Color(0xFF388E3C) else Color(0xFFD32F2F) // Green for income, red for expense
    val amountPrefix = if (isIncome) "+" else "-"
    val icon = when (transaction.type) {
        "deposit" -> Icons.Filled.AccountBalanceWallet
        "withdrawal" -> Icons.Filled.Money
        "send" -> Icons.Filled.Send
        else -> Icons.Filled.Receipt
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Card(
            backgroundColor = YellowPrimary,
            shape = RoundedCornerShape(8.dp),
            elevation = 2.dp,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    icon,
                    contentDescription = transaction.type,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Transaction Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatTransactionDate(transaction.timestamp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amount
        Text(
            text = "$amountPrefix MWK ${String.format("%.2f", transaction.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

// ---------- DATE FORMATTER ----------
private fun formatTransactionDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(date)
}

// ---------- HOME SEND SCREEN ----------
@Composable
fun HomeSendScreen(
    userId: String?,
    currentBalance: Double,
    onBackClick: () -> Unit = {},
    onBalanceUpdate: (Double) -> Unit = {}
) {
    var personNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Send Money") },
                backgroundColor = YellowPrimary,
                contentColor = Color.Black,
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Send Money",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Current Balance
                    Text(
                        text = "Current Balance: MWK ${String.format("%.2f", currentBalance)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Person Number Input
                    OutlinedTextField(
                        value = personNumber,
                        onValueChange = { personNumber = it },
                        label = { Text("Person Number") },
                        placeholder = { Text("Enter recipient's number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    // Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
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

                    // Send Button
                    Button(
                        onClick = {
                            if (userId != null) {
                                isLoading = true
                                coroutineScope.launch {
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                                    if (amountValue > 0 && amountValue <= currentBalance) {
                                        // Update balance
                                        val newBalance = currentBalance - amountValue
                                        val success = UserManager.updateUserBalance(userId, newBalance)

                                        if (success) {
                                            // Add transaction
                                            val transaction = UserManager.Transaction(
                                                description = "Send money to $personNumber",
                                                amount = amountValue,
                                                timestamp = java.util.Date(),
                                                type = "send"
                                            )
                                            UserManager.addTransaction(userId, transaction)

                                            onBalanceUpdate(newBalance)
                                            onBackClick()
                                        }
                                    }
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
                        enabled = personNumber.isNotBlank() && amount.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Confirm & Send",
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

// ---------- HOME DEPOSIT SCREEN ----------
@Composable
fun HomeDepositScreen(
    userId: String?,
    currentBalance: Double,
    onBackClick: () -> Unit = {},
    onBalanceUpdate: (Double) -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Deposit Money") },
                backgroundColor = YellowPrimary,
                contentColor = Color.Black,
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Add Funds to Your Wallet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Current Balance
                    Text(
                        text = "Current Balance: MWK ${String.format("%.2f", currentBalance)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Deposit Amount") },
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

                    // Deposit Methods
                    Text(
                        text = "Deposit Methods",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    HomeDepositMethodItem("Mobile Money", "Add funds via Airtel Money or TNM Mpamba")
                    HomeDepositMethodItem("Bank Transfer", "Transfer from your bank account")
                    HomeDepositMethodItem("Cash Agent", "Visit nearby agent to deposit cash")

                    Spacer(modifier = Modifier.height(24.dp))

                    // Deposit Button
                    Button(
                        onClick = {
                            if (userId != null) {
                                isLoading = true
                                coroutineScope.launch {
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                                    if (amountValue > 0) {
                                        // Update balance
                                        val newBalance = currentBalance + amountValue
                                        val success = UserManager.updateUserBalance(userId, newBalance)

                                        if (success) {
                                            // Add transaction
                                            val transaction = UserManager.Transaction(
                                                description = "Mobile money deposit",
                                                amount = amountValue,
                                                timestamp = java.util.Date(),
                                                type = "deposit"
                                            )
                                            UserManager.addTransaction(userId, transaction)

                                            onBalanceUpdate(newBalance)
                                            onBackClick()
                                        }
                                    }
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
                        enabled = amount.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Proceed to Deposit",
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

// ---------- HOME WITHDRAW SCREEN ----------
@Composable
fun HomeWithdrawScreen(
    userId: String?,
    currentBalance: Double,
    onBackClick: () -> Unit = {},
    onBalanceUpdate: (Double) -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Withdraw Money") },
                backgroundColor = YellowPrimary,
                contentColor = Color.Black,
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Withdraw Funds",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Current Balance
                    Text(
                        text = "Current Balance: MWK ${String.format("%.2f", currentBalance)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Withdrawal Amount") },
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

                    // Withdrawal Methods
                    Text(
                        text = "Withdrawal Methods",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    HomeWithdrawMethodItem(
                        "Mobile Money",
                        "Withdraw to Airtel Money or TNM Mpamba",
                        selected = selectedMethod == "mobile",
                        onClick = { selectedMethod = "mobile" }
                    )
                    HomeWithdrawMethodItem(
                        "Bank Transfer",
                        "Transfer to your bank account",
                        selected = selectedMethod == "bank",
                        onClick = { selectedMethod = "bank" }
                    )
                    HomeWithdrawMethodItem(
                        "Cash Agent",
                        "Withdraw cash from nearby agent",
                        selected = selectedMethod == "agent",
                        onClick = { selectedMethod = "agent" }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Withdraw Button
                    Button(
                        onClick = {
                            if (userId != null) {
                                isLoading = true
                                coroutineScope.launch {
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                                    if (amountValue > 0 && amountValue <= currentBalance) {
                                        // Update balance
                                        val newBalance = currentBalance - amountValue
                                        val success = UserManager.updateUserBalance(userId, newBalance)

                                        if (success) {
                                            // Add transaction
                                            val transaction = UserManager.Transaction(
                                                description = "Withdrawal via $selectedMethod",
                                                amount = amountValue,
                                                timestamp = java.util.Date(),
                                                type = "withdrawal"
                                            )
                                            UserManager.addTransaction(userId, transaction)

                                            onBalanceUpdate(newBalance)
                                            onBackClick()
                                        }
                                    }
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
                        enabled = amount.isNotBlank() && selectedMethod.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Confirm Withdrawal",
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

// ---------- HOME REPORTS SCREEN ----------
@Composable
fun HomeReportsScreen(
    userId: String?,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics") },
                backgroundColor = YellowPrimary,
                contentColor = Color.Black,
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Financial Reports",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    HomeReportItem("Transaction History", "View all your transactions")
                    HomeReportItem("Spending Analytics", "Analyze your spending patterns")
                    HomeReportItem("Income Report", "View your income sources")
                    HomeReportItem("Budget Performance", "Track your budget goals")
                    HomeReportItem("Monthly Statement", "Download monthly statements")
                }
            }
        }
    }
}

// ---------- ACTION BUTTONS GRID ----------
@Composable
fun ActionButtonsGrid(
    onSendClick: () -> Unit = {},
    onDepositClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onReportsClick: () -> Unit = {}
) {
    // First row: Deposit and Withdraw
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GridButton(
            text = "Deposit",
            icon = Icons.Filled.AccountBalanceWallet,
            onClick = onDepositClick
        )
        GridButton(
            text = "Withdraw",
            icon = Icons.Filled.Money,
            onClick = onWithdrawClick
        )
    }

    // Second row: Send and Reports
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GridButton(
            text = "Send",
            icon = Icons.Filled.Send,
            onClick = onSendClick
        )
        GridButton(
            text = "Reports",
            icon = Icons.Filled.Assessment,
            onClick = onReportsClick
        )
    }
}

// ---------- BOTTOM NAVIGATION BAR ----------
@Composable
fun BottomNavigationBar(
    onHomeClick: () -> Unit = {},
    onWalletClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        "Home" to Icons.Filled.Home,
        "Wallet" to Icons.Filled.AccountBalanceWallet,
        "Budget" to Icons.Filled.PieChart,
        "Settings" to Icons.Filled.Settings
    )

    BottomNavigation(
        backgroundColor = YellowPrimary,
        contentColor = Color.Black,
        elevation = 8.dp
    ) {
        items.forEachIndexed { index, (title, icon) ->
            BottomNavigationItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    when (title) {
                        "Home" -> onHomeClick()
                        "Wallet" -> onWalletClick()
                        "Budget" -> onBudgetClick()
                        "Settings" -> onSettingsClick()
                    }
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Black.copy(alpha = 0.6f)
            )
        }
    }
}

// ---------- GRID BUTTON ----------
@Composable
fun RowScope.GridButton(text: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = YellowPrimary,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .weight(1f)
            .height(80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------- HOME DEPOSIT METHOD ITEM ----------
@Composable
fun HomeDepositMethodItem(title: String, description: String) {
    Card(
        backgroundColor = LightGrayBackground,
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            }
            RadioButton(
                selected = false,
                onClick = { /* TODO: Handle selection */ },
                colors = RadioButtonDefaults.colors(
                    selectedColor = YellowPrimary
                )
            )
        }
    }
}

// ---------- HOME WITHDRAW METHOD ITEM ----------
@Composable
fun HomeWithdrawMethodItem(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        backgroundColor = LightGrayBackground,
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = YellowPrimary
                )
            )
        }
    }
}

// ---------- HOME REPORT ITEM ----------
@Composable
fun HomeReportItem(title: String, description: String) {
    Card(
        backgroundColor = LightGrayBackground,
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "View",
                tint = YellowPrimary
            )
        }
    }
}

// ---------- PREVIEW ----------
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeSendScreen() {
    HomeSendScreen(userId = "test", currentBalance = 1000.0)
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeDepositScreen() {
    HomeDepositScreen(userId = "test", currentBalance = 1000.0)
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeWithdrawScreen() {
    HomeWithdrawScreen(userId = "test", currentBalance = 1000.0)
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeReportsScreen() {
    HomeReportsScreen(userId = "test")
}

@Preview(showBackground = true)
@Composable
fun PreviewTransactionItem() {
    val transaction = UserManager.Transaction(
        description = "Mobile money deposit",
        amount = 1500.0,
        timestamp = Date(),
        type = "deposit"
    )
    TransactionItem(transaction = transaction)
}