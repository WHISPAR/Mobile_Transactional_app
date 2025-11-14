package com.example.regen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
private val SuccessGreen = Color(0xFF4CAF50)
private val ErrorRed = Color(0xFFF44336)

// ---------- WALLET SCREEN ----------
@Composable
fun WalletScreen(onBackClick: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf("main") }
    var userBalance by remember { mutableStateOf(0.0) }
    var userId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Load user data
    LaunchedEffect(Unit) {
        userId = UserManager.getCurrentUserId()
        if (userId != null) {
            val userData = UserManager.getUserData(userId!!)
            userData?.let {
                userBalance = it.balance
            }
        }
    }

    when (currentScreen) {
        "main" -> MainWalletScreen(
            userBalance = userBalance,
            onBackClick = onBackClick,
            onSendClick = { currentScreen = "send" },
            onReceiveClick = { currentScreen = "receive" },
            onDepositClick = { currentScreen = "deposit" },
            onBalanceUpdate = { newBalance ->
                userBalance = newBalance
            }
        )
        "send" -> WalletSendScreen(
            userId = userId,
            currentBalance = userBalance,
            onBackClick = { currentScreen = "main" },
            onBalanceUpdate = { newBalance ->
                userBalance = newBalance
            }
        )
        "receive" -> WalletReceiveScreen(
            onBackClick = { currentScreen = "main" }
        )
        "deposit" -> WalletDepositScreen(
            userId = userId,
            currentBalance = userBalance,
            onBackClick = { currentScreen = "main" },
            onBalanceUpdate = { newBalance ->
                userBalance = newBalance
            }
        )
    }
}

@Composable
fun MainWalletScreen(
    userBalance: Double = 0.0,
    onBackClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onReceiveClick: () -> Unit = {},
    onDepositClick: () -> Unit = {},
    onBalanceUpdate: (Double) -> Unit = {}
) {
    var recentTransactions by remember { mutableStateOf<List<UserManager.Transaction>>(emptyList()) }
    val userId = UserManager.getCurrentUserId()

    // Load recent transactions
    LaunchedEffect(userId) {
        if (userId != null) {
            recentTransactions = UserManager.getRecentTransactions(userId, 5)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Wallet") },
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

            // Wallet Balance Section
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
                        text = "Total Balance",
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
                        text = "Available Funds",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }

            // Quick Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WalletActionButton("Send", Icons.AutoMirrored.Filled.Send, onClick = onSendClick)
                WalletActionButton("Receive", Icons.Filled.RequestQuote, onClick = onReceiveClick)
                WalletActionButton("Deposit", Icons.Filled.Add, onClick = onDepositClick)
            }

            // Transaction History
            Card(
                backgroundColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (recentTransactions.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Receipt,
                                contentDescription = "No transactions",
                                tint = Color.Gray,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            recentTransactions.forEachIndexed { index, transaction ->
                                WalletTransactionItem(transaction = transaction)
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

// ---------- WALLET SEND SCREEN ----------
@Composable
fun WalletSendScreen(
    userId: String?,
    currentBalance: Double,
    onBackClick: () -> Unit = {},
    onBalanceUpdate: (Double) -> Unit = {}
) {
    var personNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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

                    // Error Message
                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = ErrorRed,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Person Number Input
                    OutlinedTextField(
                        value = personNumber,
                        onValueChange = {
                            personNumber = it
                            errorMessage = null
                        },
                        label = { Text("Recipient Number") },
                        placeholder = { Text("265 XXX XXX XXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                                errorMessage = null

                                coroutineScope.launch {
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0

                                    // Validation
                                    if (personNumber.length < 9) {
                                        errorMessage = "Please enter a valid phone number"
                                    } else if (amountValue <= 0) {
                                        errorMessage = "Please enter a valid amount"
                                    } else if (amountValue > currentBalance) {
                                        errorMessage = "Insufficient balance"
                                    } else {
                                        // Update balance
                                        val newBalance = currentBalance - amountValue
                                        val success = UserManager.updateUserBalance(userId, newBalance)

                                        if (success) {
                                            // Add transaction
                                            val transaction = UserManager.Transaction(
                                                description = "Send to $personNumber",
                                                amount = amountValue,
                                                timestamp = Date(),
                                                type = "send"
                                            )
                                            UserManager.addTransaction(userId, transaction)

                                            onBalanceUpdate(newBalance)
                                            onBackClick()
                                        } else {
                                            errorMessage = "Transaction failed. Please try again."
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

// ---------- WALLET RECEIVE SCREEN ----------
@Composable
fun WalletReceiveScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var userPhoneNumber by remember { mutableStateOf("265 991 034 749") }

    // Load user phone number
    LaunchedEffect(Unit) {
        // In a real app, you would get this from user profile or authentication
        userPhoneNumber = "265 991 034 749" // Placeholder
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Receive Money") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                backgroundColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.QrCode,
                        contentDescription = "Receive Money",
                        modifier = Modifier.size(80.dp),
                        tint = YellowPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your Personal Number",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = userPhoneNumber,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Button(
                        onClick = {
                            // TODO: Implement share functionality
                            // Share phone number via SMS, WhatsApp, etc.
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = YellowPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Number")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Share this number with others to receive money instantly",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ---------- WALLET DEPOSIT SCREEN ----------
@Composable
fun WalletDepositScreen(
    userId: String?,
    currentBalance: Double,
    onBackClick: () -> Unit = {},
    onBalanceUpdate: (Double) -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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

                    // Error Message
                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = ErrorRed,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            errorMessage = null
                        },
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

                    WalletDepositMethodItem(
                        "Mobile Money",
                        "Add funds via Airtel Money or TNM Mpamba",
                        selected = selectedMethod == "mobile",
                        onClick = { selectedMethod = "mobile" }
                    )
                    WalletDepositMethodItem(
                        "Bank Transfer",
                        "Transfer from your bank account",
                        selected = selectedMethod == "bank",
                        onClick = { selectedMethod = "bank" }
                    )
                    WalletDepositMethodItem(
                        "Cash Agent",
                        "Visit nearby agent to deposit cash",
                        selected = selectedMethod == "agent",
                        onClick = { selectedMethod = "agent" }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Deposit Button
                    Button(
                        onClick = {
                            if (userId != null) {
                                isLoading = true
                                errorMessage = null

                                coroutineScope.launch {
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0

                                    // Validation
                                    if (amountValue <= 0) {
                                        errorMessage = "Please enter a valid amount"
                                    } else if (selectedMethod.isEmpty()) {
                                        errorMessage = "Please select a deposit method"
                                    } else {
                                        // Update balance
                                        val newBalance = currentBalance + amountValue
                                        val success = UserManager.updateUserBalance(userId, newBalance)

                                        if (success) {
                                            // Add transaction
                                            val transaction = UserManager.Transaction(
                                                description = "Deposit via $selectedMethod",
                                                amount = amountValue,
                                                timestamp = Date(),
                                                type = "deposit"
                                            )
                                            UserManager.addTransaction(userId, transaction)

                                            onBalanceUpdate(newBalance)
                                            onBackClick()
                                        } else {
                                            errorMessage = "Deposit failed. Please try again."
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

@Composable
fun WalletDepositMethodItem(
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

@Composable
fun WalletTransactionItem(transaction: UserManager.Transaction) {
    val isIncome = transaction.type == "deposit"
    val amountColor = if (isIncome) SuccessGreen else ErrorRed
    val amountPrefix = if (isIncome) "+" else "-"
    val icon = when (transaction.type) {
        "deposit" -> Icons.Filled.AccountBalanceWallet
        "withdrawal" -> Icons.Filled.Money
        "send" -> Icons.AutoMirrored.Filled.Send
        else -> Icons.Filled.Receipt
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                color = Color.Black
            )
            Text(
                text = formatWalletTransactionDate(transaction.timestamp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
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

@Composable
fun RowScope.WalletActionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = YellowPrimary,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .weight(1f)
            .height(60.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatWalletTransactionDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(date)
}

@Preview(showBackground = true)
@Composable
fun PreviewWalletScreen() {
    WalletScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewWalletSendScreen() {
    WalletSendScreen(userId = "test", currentBalance = 1000.0)
}

@Preview(showBackground = true)
@Composable
fun PreviewWalletReceiveScreen() {
    WalletReceiveScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewWalletDepositScreen() {
    WalletDepositScreen(userId = "test", currentBalance = 1000.0)
}