package com.example.regen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable // ADD THIS IMPORT

// ---------- COLOR PALETTE ----------
private val YellowPrimary = Color(0xFFFFC107)
private val YellowCard = Color(0xFFFFEB3B)
private val LightGrayBackground = Color(0xFFF5F5F5)

// ---------- SETTINGS SCREEN ----------
@Composable
fun SettingsScreen(onBackClick: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf("main") }

    when (currentScreen) {
        "main" -> MainSettingsScreen(
            onBackClick = onBackClick,
            onAccountSettingsClick = { currentScreen = "account" },
            onNotificationsClick = { currentScreen = "notifications" },
            onSecurityClick = { currentScreen = "security" },
            onPrivacyClick = { currentScreen = "privacy" },
            onPaymentMethodsClick = { currentScreen = "payment" },
            onLanguageClick = { currentScreen = "language" },
            onHelpSupportClick = { currentScreen = "help" },
            onAboutClick = { currentScreen = "about" }
        )
        "account" -> AccountSettingsScreen(onBackClick = { currentScreen = "main" })
        "notifications" -> NotificationsSettingsScreen(onBackClick = { currentScreen = "main" })
        "security" -> SecuritySettingsScreen(onBackClick = { currentScreen = "main" })
        "privacy" -> PrivacySettingsScreen(onBackClick = { currentScreen = "main" })
        "payment" -> PaymentMethodsScreen(onBackClick = { currentScreen = "main" })
        "language" -> LanguageSettingsScreen(onBackClick = { currentScreen = "main" })
        "help" -> HelpSupportScreen(onBackClick = { currentScreen = "main" })
        "about" -> AboutScreen(onBackClick = { currentScreen = "main" })
    }
}

@Composable
fun MainSettingsScreen(
    onBackClick: () -> Unit = {},
    onAccountSettingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onPaymentMethodsClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        ) {

            // Profile Section
            Card(
                backgroundColor = YellowCard,
                shape = RoundedCornerShape(12.dp),
                elevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(50.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "MR JOHN LEVIS DOE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "john.doe@example.com",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                        Text(
                            text = "+265 123 456 789",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Settings Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                SettingsOption("Account Settings", Icons.Filled.Person, onClick = onAccountSettingsClick)
                SettingsOption("Notifications", Icons.Filled.Notifications, onClick = onNotificationsClick)
                SettingsOption("Security", Icons.Filled.Security, onClick = onSecurityClick)
                SettingsOption("Privacy", Icons.Filled.PrivacyTip, onClick = onPrivacyClick)
                SettingsOption("Payment Methods", Icons.Filled.Payment, onClick = onPaymentMethodsClick)
                SettingsOption("Language", Icons.Filled.Language, onClick = onLanguageClick)
                SettingsOption("Help & Support", Icons.AutoMirrored.Filled.Help, onClick = onHelpSupportClick)
                SettingsOption("About", Icons.Filled.Info, onClick = onAboutClick)
            }

            // Logout Button
            Button(
                onClick = { /* TODO: Add logout action */ },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------- ACCOUNT SETTINGS SCREEN ----------
@Composable
fun AccountSettingsScreen(onBackClick: () -> Unit = {}) {
    var fullName by remember { mutableStateOf("MR JOHN LEVIS DOE") }
    var email by remember { mutableStateOf("john.doe@example.com") }
    var phone by remember { mutableStateOf("+265 123 456 789") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = { onBackClick() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = YellowPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------- NOTIFICATIONS SETTINGS SCREEN ----------
@Composable
fun NotificationsSettingsScreen(onBackClick: () -> Unit = {}) {
    var pushNotifications by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(false) }
    var smsNotifications by remember { mutableStateOf(true) }
    var transactionAlerts by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Notification Preferences",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    NotificationOption("Push Notifications", "Receive app notifications", pushNotifications) { pushNotifications = it }
                    NotificationOption("Email Notifications", "Get updates via email", emailNotifications) { emailNotifications = it }
                    NotificationOption("SMS Notifications", "Receive text messages", smsNotifications) { smsNotifications = it }
                    NotificationOption("Transaction Alerts", "Alerts for all transactions", transactionAlerts) { transactionAlerts = it }
                }
            }
        }
    }
}

// ---------- SECURITY SETTINGS SCREEN ----------
@Composable
fun SecuritySettingsScreen(onBackClick: () -> Unit = {}) {
    var biometricEnabled by remember { mutableStateOf(true) }
    var twoFactorAuth by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Security") },
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Security Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    NotificationOption("Biometric Login", "Use fingerprint or face ID", biometricEnabled) { biometricEnabled = it }
                    NotificationOption("Two-Factor Authentication", "Extra security for your account", twoFactorAuth) { twoFactorAuth = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { /* TODO: Change password */ },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = YellowPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Change Password", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { /* TODO: View login activity */ },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = LightGrayBackground,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("View Login Activity", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------- PRIVACY SETTINGS SCREEN ----------
@Composable
fun PrivacySettingsScreen(onBackClick: () -> Unit = {}) {
    var dataSharing by remember { mutableStateOf(false) }
    var personalizedAds by remember { mutableStateOf(false) }
    var locationTracking by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Privacy") },
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Privacy Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    NotificationOption("Data Sharing", "Share anonymous usage data", dataSharing) { dataSharing = it }
                    NotificationOption("Personalized Ads", "Show relevant advertisements", personalizedAds) { personalizedAds = it }
                    NotificationOption("Location Tracking", "Use location for services", locationTracking) { locationTracking = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { /* TODO: Download data */ },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = YellowPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Download My Data", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------- PAYMENT METHODS SCREEN ----------
@Composable
fun PaymentMethodsScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods") },
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Your Payment Methods",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    PaymentMethodItem("Mobile Money", "Airtel Money •••• 1234", Icons.Filled.PhoneAndroid)
                    PaymentMethodItem("Bank Account", "Standard Bank •••• 5678", Icons.Filled.AccountBalance)
                    PaymentMethodItem("Credit Card", "Visa •••• 9012", Icons.Filled.CreditCard)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { /* TODO: Add payment method */ },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = YellowPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Add Payment Method", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------- LANGUAGE SETTINGS SCREEN ----------
@Composable
fun LanguageSettingsScreen(onBackClick: () -> Unit = {}) {
    var selectedLanguage by remember { mutableStateOf("English") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Language") },
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Select Language",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LanguageOption("English", selected = selectedLanguage == "English") { selectedLanguage = "English" }
                    LanguageOption("Chichewa", selected = selectedLanguage == "Chichewa") { selectedLanguage = "Chichewa" }
                    LanguageOption("French", selected = selectedLanguage == "French") { selectedLanguage = "French" }
                    LanguageOption("Portuguese", selected = selectedLanguage == "Portuguese") { selectedLanguage = "Portuguese" }
                }
            }
        }
    }
}

// ---------- HELP & SUPPORT SCREEN ----------
@Composable
fun HelpSupportScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Help & Support") },
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Get Help",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    HelpOption("FAQs", "Frequently asked questions")
                    HelpOption("Contact Support", "Get in touch with our team")
                    HelpOption("Report a Problem", "Tell us about any issues")
                    HelpOption("User Guide", "How to use the app")

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Contact Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text("Email: support@regen.com", fontSize = 14.sp, color = Color.Gray)
                    Text("Phone: +265 800 123 456", fontSize = 14.sp, color = Color.Gray)
                    Text("Hours: 24/7", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

// ---------- ABOUT SCREEN ----------
@Composable
fun AboutScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = "App Icon",
                        modifier = Modifier.size(80.dp),
                        tint = YellowPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "REGEN Wallet",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "Version 1.0.0",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    AboutItem("Terms of Service")
                    AboutItem("Privacy Policy")
                    AboutItem("Licenses")
                    AboutItem("Rate Our App")

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "© 2024 REGEN Wallet. All rights reserved.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ---------- COMPONENTS ----------
@Composable
fun SettingsOption(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        backgroundColor = Color.White,
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = onClick), // FIXED: Added clickable import
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = text,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun NotificationOption(title: String, description: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = YellowPrimary,
                checkedTrackColor = YellowPrimary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun PaymentMethodItem(name: String, details: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        backgroundColor = LightGrayBackground,
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
            Icon(
                icon,
                contentDescription = name,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = details,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "Options",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun LanguageOption(language: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        backgroundColor = if (selected) YellowPrimary.copy(alpha = 0.2f) else LightGrayBackground,
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick) // FIXED: Added clickable import
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = language,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = YellowPrimary
                )
            }
        }
    }
}

@Composable
fun HelpOption(title: String, description: String) {
    Card(
        backgroundColor = LightGrayBackground,
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* TODO: Navigate to help section */ } // FIXED: Added clickable import
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun AboutItem(title: String) {
    Card(
        backgroundColor = LightGrayBackground,
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* TODO: Handle click */ } // FIXED: Added clickable import
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewAccountSettingsScreen() {
    AccountSettingsScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewNotificationsSettingsScreen() {
    NotificationsSettingsScreen()
}