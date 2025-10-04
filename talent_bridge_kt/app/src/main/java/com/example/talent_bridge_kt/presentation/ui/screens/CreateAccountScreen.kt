package com.example.talent_bridge_kt.presentation.ui.screens

import SignUpPopUp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.TitleGreen
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.LinkGreen
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.talent_bridge_kt.data.AuthManager

@Composable
fun CreateAccountScreen(
    onBack: () -> Unit = {}
) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    fun isValidEmail(v: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(v).matches()
    fun isValidPassword(v: String) = v.length >= 6
    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()


    Surface(color = CreamBackground, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(120.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = "Sign Up",
                    color = AccentYellow,
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(36.dp))

                FieldLabel("User")
                PillTextField(
                    value = user,
                    onValueChange = { user = it }
                )

                Spacer(Modifier.height(18.dp))

                FieldLabel("Password")
                PillTextField(
                    value = password,
                    onValueChange = { password = it },
                    isPassword = true
                )

                Spacer(Modifier.height(18.dp))

                FieldLabel("Email")
                PillTextField(
                    value = email,
                    onValueChange = { email = it }
                )

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (!isValidEmail(email)) { toast("Invalid Email"); return@Button }
                        if (!isValidPassword(password)) { toast("Password must have at least six characters"); return@Button }

                        isLoading = true
                        AuthManager.register(
                            email = email.trim(),
                            password = password,
                            onSuccess = {
                                isLoading = false
                                showDialog = true

                            },
                            onError = { msg ->
                                isLoading = false
                                toast("Unsuccessful account creation: $msg")
                            }
                        )
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .shadow(6.dp, RoundedCornerShape(28.dp))
                ) {
                    Text(if (isLoading) "Creating..." else "Next", color = Color.White, fontSize = 18.sp)
                }

                Spacer(Modifier.height(28.dp))


                Spacer(Modifier.height(16.dp))


                Text(
                    text = "Already have an account? Log In",
                    color = LinkGreen,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { onBack() },
                    textAlign = TextAlign.Center
                )
                // ==== Pop-up overlay ====
                SignUpPopUp(
                    show = showDialog,
                    onDismiss = { showDialog = false },
                    onLoginClick = {
                        showDialog = false
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = AccentYellow,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp)
    )
}

@Composable
private fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    val shape = RoundedCornerShape(28.dp)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape)
            .background(Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedBorderColor = AccentYellow,
            unfocusedBorderColor = AccentYellow,
            cursorColor = AccentYellow
        ),
        visualTransformation = if (isPassword)
            PasswordVisualTransformation() else VisualTransformation.None
    )
}
