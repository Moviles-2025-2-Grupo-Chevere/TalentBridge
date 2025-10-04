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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@Composable
fun LoginScreen(modifier: Modifier = Modifier,  onCreateAccount: () -> Unit = {},
                onStudentFeed: () -> Unit = {}) {
    //States
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = Firebase.auth

    fun isValidEmail(value: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
    fun isValidPassword(value: String) = value.length >= 6

    fun showToast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    fun doLogin() {
        if (!isValidEmail(email)) { showToast("Email inválido"); return }
        if (!isValidPassword(password)) { showToast("La contraseña debe tener al menos 6 caracteres"); return }

        isLoading = true
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onStudentFeed()
                } else {
                    val m = task.exception?.message?.lowercase().orEmpty()
                    val msg = when {
                        "password is invalid" in m -> "Contraseña incorrecta"
                        "no user record" in m      -> "No existe una cuenta con ese email"
                        "badly formatted" in m     -> "Email con formato inválido"
                        else -> "Error de autenticación: ${task.exception?.message ?: "desconocido"}"
                    }
                    showToast(msg)
                }
            }
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

                Text(text = "User",
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
                    Text(if (isLoading) "Ingresando..." else "Next")
                }

                Spacer(Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = R.drawable.gmail_logo),
                    contentDescription = "Gmail Logo",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )

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

