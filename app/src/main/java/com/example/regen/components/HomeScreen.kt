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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    when (currentScreen) {
        "main" -> MainHomeScreen(
            onWalletClick = onWalletClick,
            onBudgetClick = onBudgetClick,
            onSettingsClick = onSettingsClick,
            onNotificationsClick = onNotificationsClick,
            onSendClick = { currentScreen = "send" },
            onDepositClick = { currentScreen = "deposit" },
            onWithdrawClick = { currentScreen = "withdraw" },
            onReportsClick = { currentScreen = "reports" }
        )
        "send" -> HomeSendScreen(onBackClick = { currentScreen = "main" })
        "deposit" -> HomeDepositScreen(onBackClick = { currentScreen = "main" })
        "withdraw" -> HomeWithdrawScreen(onBackClick = { currentScreen = "main" })
        "reports" -> HomeReportsScreen(onBackClick = { currentScreen = "main" })
    }
}

@Composable
fun MainHomeScreen(
    onWalletClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onDepositClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onReportsClick: () -> Unit = {}
) {
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
                        text = "0.00 MWK",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MR JOHN LEVIS DOE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }

            // Main Action Buttons - Only Deposit, Withdraw, Send, Reports
            ActionButtonsGrid(
                onSendClick = onSendClick,
                onDepositClick = onDepositClick,
                onWithdrawClick = onWithdrawClick,
                onReportsClick = onReportsClick
            )

            // Volume Section
            Card(
                backgroundColor = YellowCard,
                shape = RoundedCornerShape(12.dp),
                elevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "300 mL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        VolumeIndicator(percentage = "25%", value = "0.00 mL")
                        VolumeIndicator(percentage = "24%", value = "0.00 mL")
                    }
                }
            }
        }
    }
}

// ---------- HOME SEND SCREEN ----------
@Composable
fun HomeSendScreen(onBackClick: () -> Unit = {}) {
    var personNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

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
                            // TODO: Implement send money logic
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = YellowPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = personNumber.isNotBlank() && amount.isNotBlank()
                    ) {
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

// ---------- HOME DEPOSIT SCREEN ----------
@Composable
fun HomeDepositScreen(onBackClick: () -> Unit = {}) {
    var amount by remember { mutableStateOf("") }

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
                            // TODO: Implement deposit logic
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = YellowPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = amount.isNotBlank()
                    ) {
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

// ---------- HOME WITHDRAW SCREEN ----------
@Composable
fun HomeWithdrawScreen(onBackClick: () -> Unit = {}) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("") }

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
                            // TODO: Implement withdraw logic
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = YellowPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = amount.isNotBlank() && selectedMethod.isNotBlank()
                    ) {
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

// ---------- HOME REPORTS SCREEN ----------
@Composable
fun HomeReportsScreen(onBackClick: () -> Unit = {}) {
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

// ---------- VOLUME INDICATOR ----------
@Composable
fun VolumeIndicator(percentage: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = percentage,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
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
fun HomeWithdrawMethodItem(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
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
    HomeSendScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeDepositScreen() {
    HomeDepositScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeWithdrawScreen() {
    HomeWithdrawScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeReportsScreen() {
    HomeReportsScreen()
}