package com.example.regen.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Phone // Add this import
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.regen.managers.UserManager
import com.example.regen.services.AuthService
import kotlinx.coroutines.launch

// Color palette from BudgetScreen
private val YellowPrimary = Color(0xFFFFC107)
private val LightGrayBackground = Color(0xFFF5F5F5)
private val DarkerGrayText = Color(0xFF757575)
private val ErrorRed = Color(0xFFD32F2F)

// Default admin credentials
private const val DEFAULT_ADMIN_EMAIL = "admin@regen.com"
private const val DEFAULT_ADMIN_PASSWORD = "admin123"

@Composable
fun SigninScreen(
    onSignInSuccess: (userName: String) -> Unit,
    onNavigateToSignUp: () -> Unit = {}
) {
    var isSignIn by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Function to handle authentication
    fun handleAuthentication() {
        // Validate inputs
        if (!isSignIn) {
            if (password != confirmPassword) {
                errorMessage = "Passwords do not match"
                return
            }
            if (password.length < 6) {
                errorMessage = "Password must be at least 6 characters"
                return
            }
        }

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                // Check for default admin login first
                if (isSignIn && email == DEFAULT_ADMIN_EMAIL && password == DEFAULT_ADMIN_PASSWORD) {
                    // Save admin data locally
                    UserManager.saveUserDataLocally(context, "admin_user", "Admin User", email)
                    onSignInSuccess("Admin User")
                    return@launch
                }

                // Regular authentication flow
                val result = if (isSignIn) {
                    AuthService.signIn(email, password)
                } else {
                    AuthService.signUp(name, email, password, phoneNumber)
                }

                if (result.isSuccess) {
                    val userId = AuthService.getCurrentUserId()
                    if (userId != null) {
                        if (isSignIn) {
                            // For sign in, get user data from Firestore
                            val userData = UserManager.getUserData(userId)
                            if (userData != null) {
                                val userName = userData.name.ifEmpty { email.substringBefore("@") }
                                // Save user data locally
                                UserManager.saveUserDataLocally(context, userId, userName, email)
                                onSignInSuccess(userName)
                            } else {
                                // If no user data in Firestore, create it
                                val success = UserManager.createUserInFirestore(
                                    userId = userId,
                                    name = email.substringBefore("@"),
                                    email = email,
                                    phoneNumber = ""
                                )
                                if (success) {
                                    UserManager.saveUserDataLocally(context, userId, email.substringBefore("@"), email)
                                    onSignInSuccess(email.substringBefore("@"))
                                } else {
                                    errorMessage = "Failed to create user profile"
                                }
                            }
                        } else {
                            // For sign up, user data is created in AuthService.signUp
                            // Just verify and proceed
                            val userData = UserManager.getUserData(userId)
                            val userName = userData?.name ?: name
                            UserManager.saveUserDataLocally(context, userId, userName, email)
                            onSignInSuccess(userName)
                        }
                    } else {
                        errorMessage = "User authentication failed"
                    }
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightGrayBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = if (isSignIn) "Welcome Back" else "Create Account",
                style = MaterialTheme.typography.h4,
                fontSize = 28.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = if (isSignIn) "Sign in to continue" else "Sign up to get started",
                style = MaterialTheme.typography.body1,
                color = DarkerGrayText,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Error message
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = ErrorRed,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Name Field (only for sign up)
            if (!isSignIn) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Full Name", color = DarkerGrayText) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Name", tint = DarkerGrayText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.Black,
                        cursorColor = YellowPrimary,
                        focusedBorderColor = YellowPrimary,
                        focusedLabelColor = YellowPrimary
                    )
                )
            }

            // Phone Number Field (only for sign up)
            if (!isSignIn) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        errorMessage = null
                    },
                    label = { Text("Phone Number", color = DarkerGrayText) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = "Phone", tint = DarkerGrayText) // Now this will work
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.Black,
                        cursorColor = YellowPrimary,
                        focusedBorderColor = YellowPrimary,
                        focusedLabelColor = YellowPrimary
                    )
                )
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email Address", color = DarkerGrayText) },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email", tint = DarkerGrayText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    cursorColor = YellowPrimary,
                    focusedBorderColor = YellowPrimary,
                    focusedLabelColor = YellowPrimary
                )
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password", color = DarkerGrayText) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password", tint = DarkerGrayText)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = DarkerGrayText
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    cursorColor = YellowPrimary,
                    focusedBorderColor = YellowPrimary,
                    focusedLabelColor = YellowPrimary
                )
            )

            // Confirm Password Field (only for sign up)
            if (!isSignIn) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm Password", color = DarkerGrayText) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Confirm Password", tint = DarkerGrayText)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = DarkerGrayText
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.Black,
                        cursorColor = YellowPrimary,
                        focusedBorderColor = YellowPrimary,
                        focusedLabelColor = YellowPrimary
                    )
                )
            }

            // Sign In/Sign Up Button
            Button(
                onClick = { handleAuthentication() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && if (isSignIn) {
                    email.isNotEmpty() && password.isNotEmpty()
                } else {
                    name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = YellowPrimary,
                    contentColor = Color.Black,
                    disabledBackgroundColor = YellowPrimary.copy(alpha = 0.5f),
                    disabledContentColor = Color.Black.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignIn) "Sign In" else "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle between Sign In and Sign Up
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignIn) "Don't have an account?" else "Already have an account?",
                    color = DarkerGrayText,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = {
                        isSignIn = !isSignIn
                        errorMessage = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = YellowPrimary
                    )
                ) {
                    Text(
                        text = if (isSignIn) "Sign Up" else "Sign In",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Forgot Password (only for sign in)
            if (isSignIn) {
                TextButton(
                    onClick = {
                        // Handle forgot password
                        coroutineScope.launch {
                            if (email.isNotBlank()) {
                                val result = AuthService.resetPassword(email)
                                if (result.isSuccess) {
                                    errorMessage = "Password reset email sent to $email"
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to send reset email"
                                }
                            } else {
                                errorMessage = "Please enter your email address first"
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = DarkerGrayText
                    )
                ) {
                    Text(
                        "Forgot Password?",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}