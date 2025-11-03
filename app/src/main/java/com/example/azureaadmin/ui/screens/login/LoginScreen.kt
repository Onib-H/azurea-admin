    package com.example.azureaadmin.ui.screens.login

    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Email
    import androidx.compose.material.icons.filled.Lock
    import androidx.compose.material.icons.filled.Visibility
    import androidx.compose.material.icons.filled.VisibilityOff
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.SpanStyle
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.text.buildAnnotatedString
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.text.input.VisualTransformation
    import androidx.compose.ui.text.withStyle
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.example.azureaadmin.R
    import com.example.azureaadmin.data.local.DataStoreManager
    import com.example.azureaadmin.data.repository.AdminRepository


    @Composable
    fun LoginScreen(
        onLoginSuccess: () -> Unit
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        // Setup ViewModel manually here (replace with Hilt if you use DI)
        val context = LocalContext.current
        val repository = AdminRepository(context)
        val dataStoreManager = remember { DataStoreManager(context) }
        val viewModel = remember { AdminLoginViewModel(repository, dataStoreManager) }
        val loginState by viewModel.loginState.collectAsState()




        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = buildAnnotatedString {
                        append("Welcome to ")
                        withStyle(
                            SpanStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF6A1B9A), Color(0xFF6A1B9A))
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Azurea")
                        }
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.Gray) },
                    placeholder = { Text("email@gmail.com", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Email Icon",
                            tint = Color.DarkGray
                        )
                    },
                    textStyle = TextStyle(color = Color.Black)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    label = { Text("Password", color = Color.Gray) },
                    placeholder = { Text("Enter your password", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Password Icon",
                            tint = Color.DarkGray
                        )
                    },
                    trailingIcon = {
                        val visibilityIcon = if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = visibilityIcon,
                                contentDescription = description,
                                tint = Color.Black
                            )
                        }
                    },
                    textStyle = TextStyle(color = Color.Black)
                )

                // ✅ Show error just below the password input
                if (loginState is LoginState.Error) {
                    Text(
                        text = (loginState as LoginState.Error).message,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Forgot password?",
                    color = Color(0xFF6A1B9A),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Login button
                // Button with spinner when loading
                Button(
                    onClick = { viewModel.login(email, password) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = loginState !is LoginState.Loading
                ) {
                    when (loginState) {
                        is LoginState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Logging in...", fontSize = 16.sp, color = Color.White)
                            }
                        }
                        else -> {
                            Text("Login", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }

// Handle state changes outside the button
                when (loginState) {
                    is LoginState.Success -> {
                        // ✅ Navigation happens here
                        LaunchedEffect(Unit) { onLoginSuccess() }
                    }
                    else -> {}
                }


                Spacer(modifier = Modifier.height(16.dp))

                Text("OR", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                // Google login button
                Button(
                    onClick = { /* Handle Google login */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Image(
                        painter = painterResource(R.drawable.google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login with Google", fontSize = 16.sp, color = Color.Black)
                }
            }
        }
    }

