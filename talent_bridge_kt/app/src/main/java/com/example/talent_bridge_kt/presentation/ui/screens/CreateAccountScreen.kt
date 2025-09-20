package com.example.talent_bridge_kt.presentation.ui.screens

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
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.TitleGreen
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.LinkGreen

@Composable
fun CreateAccountScreen(
    onBack: () -> Unit = {}
) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

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
                    text = "Crea Tu Cuenta",
                    color = AccentYellow,
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(36.dp))

                // Usuario
                FieldLabel("Usuario")
                PillTextField(
                    value = user,
                    onValueChange = { user = it }
                )

                Spacer(Modifier.height(18.dp))

                // Password
                FieldLabel("Contraseña")
                PillTextField(
                    value = password,
                    onValueChange = { password = it },
                    isPassword = true
                )

                Spacer(Modifier.height(18.dp))

                // Email
                FieldLabel("Email")
                PillTextField(
                    value = email,
                    onValueChange = { email = it }
                )

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .shadow(6.dp, RoundedCornerShape(28.dp))
                ) {
                    Text("Next", color = Color.White, fontSize = 18.sp)
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    "Other Sign In Options",
                    color = TitleGreen,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(16.dp))

                // Icono Gmail centrado
                Image(
                    painter = painterResource(R.drawable.gmail_logo), // usa tu recurso
                    contentDescription = "Sign in with Gmail",
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(28.dp))

                // Link inferior
                Text(
                    text = "Already have an account? Log In",
                    color = LinkGreen,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { onBack() },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Etiqueta encima de cada campo
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
