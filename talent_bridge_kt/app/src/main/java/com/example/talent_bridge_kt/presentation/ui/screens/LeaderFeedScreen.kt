package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.talent_bridge_kt.ui.theme.CreamBackground

@Composable
fun LeaderFeedScreen(
    onBack: () -> Unit = {}

) {
    Surface(color = CreamBackground, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(24.dp)) {
            Text("Saved Projects")
            TextButton(onClick = onBack) { Text("Volver") }

        }
    }
}
