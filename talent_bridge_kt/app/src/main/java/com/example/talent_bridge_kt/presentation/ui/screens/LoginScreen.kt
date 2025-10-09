package com.example.talent_bridge_kt.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.TitleGreen
import com.example.talent_bridge_kt.data.AuthManager
import com.example.talent_bridge_kt.domain.analytics.AnalyticsTracker
import com.example.talent_bridge_kt.data.analytics.FirebaseAnalyticsTracker

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
// En cualquier parte con contexto (Activity/Compose):
import com.google.firebase.analytics.ktx.analytics




@Composable
fun LoginScreen(modifier: Modifier = Modifier,  onCreateAccount: () -> Unit = {},
                onStudentFeed: () -> Unit = {}) {
    //States
    val tracker: AnalyticsTracker = remember { FirebaseAnalyticsTracker() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    fun isValidEmail(value: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
    fun isValidPassword(value: String) = value.length >= 6

    fun showToast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    fun doLogin() {
        if (!isValidEmail(email)) { showToast("Invalid email"); return }
        if (!isValidPassword(password)) { showToast("Password must have at least six characters"); return }

        isLoading = true
        AuthManager.login(
            email = email.trim(),
            password = password,
            onSuccess = {
                isLoading = false
                tracker.login("email")
                tracker.identify(userId = email.trim(), role = "student")
                onStudentFeed()
            },
            onError = { msg ->
                isLoading = false
                tracker.event(
                    name = "login_error",
                    params = mapOf(
                        "method" to "email",
                        "message" to msg.take(120)
                    )
                )
                showToast(
                    when {
                        msg.contains("password is invalid", ignoreCase = true) -> "Incorrect password"
                        msg.contains("no user record", ignoreCase = true)      -> "There is no account associated to this email"
                        msg.contains("badly formatted", ignoreCase = true)     -> "Invalid email"
                        else -> "Auth Error: $msg"
                    }
                )
            }
        )
    }

    Surface(color = CreamBackground, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Talent Bridge",
                    modifier = Modifier.size(250.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(60.dp))

                Text(text = "Email",
                    color = AccentYellow,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        errorContainerColor = Color.White,
                        focusedBorderColor = AccentYellow,
                        unfocusedBorderColor = AccentYellow,
                        focusedLabelColor = AccentYellow,
                        cursorColor = AccentYellow,
                    )
                )

                Spacer(Modifier.height(16.dp))

                Text(text= "Password",
                    color = AccentYellow,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        errorContainerColor = Color.White,
                        focusedBorderColor = AccentYellow,
                        unfocusedBorderColor = AccentYellow,
                        focusedLabelColor = AccentYellow,
                        cursorColor = AccentYellow,
                    )
                )

                Spacer(Modifier.height(28.dp))

                OutlinedButton(
                    onClick = { if (!isLoading) doLogin() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AccentYellow,
                        contentColor = Color.White
                    ),
                ) {
                    Text(if (isLoading) "Loading..." else "Next")
                }

                Spacer(Modifier.height(32.dp))


                Spacer(Modifier.height(16.dp))

                Text(text = "New to Talent Bridge? Sign In",
                    color = TitleGreen,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreateAccount() },
                    textAlign = TextAlign.Center


                )

            }
        }
    }
}

