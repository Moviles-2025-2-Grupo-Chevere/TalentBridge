package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.talent_bridge_kt.ui.theme.CreamBackground

@Composable
fun NavegationScreen(
    onBack: () -> Unit = {},
    onInitiativeProfile: () -> Unit = {},
    onLeaderFeed: () -> Unit = {},
    onSavedProjects: () -> Unit = {},
    onSearch: () -> Unit = {},
    onStudentProfile: () -> Unit = {},
    onSomeoneElseProfile: () -> Unit = {},
    onCredits: () -> Unit ={},
    onStudentFeed: () -> Unit ={}

) {
    Surface(color = CreamBackground, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(24.dp)) {

            Text("Navegation")
            TextButton(onClick = onBack) { Text("Volver") }

            Text(text = "Initiative Profile",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInitiativeProfile() })
            Spacer(Modifier.height(16.dp))

            Text(text = "Leader Feed",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLeaderFeed() })
            Spacer(Modifier.height(16.dp))

            Text(text = "Saved Projects",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSavedProjects() })
            Spacer(Modifier.height(16.dp))

            Text(text = "Search",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearch() })
            Spacer(Modifier.height(16.dp))

            Text(text = "Student Profile",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStudentProfile() })
            Spacer(Modifier.height(16.dp))

            Text(text = "Someone Else Screen",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSomeoneElseProfile() })
            Spacer(Modifier.height(16.dp))

            Text(text = "Credits Screen",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCredits() })
            Spacer(Modifier.height(16.dp))

            Text(text = "Student Feed",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStudentFeed() })
            Spacer(Modifier.height(16.dp))

        }

    }
}
