package com.example.talent_bridge_kt.presentation.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.TitleGreen
import com.example.talent_bridge_kt.data.firebase.model.FirestoreProject
import com.google.firebase.Timestamp
import java.util.UUID

data class ProjectDraft(
    val title: String,
    val description: String,
    val skills: List<String>,
    val imageUri: String?
)

@Composable
fun CreateProjectPopUp(
    onDismiss: () -> Unit,
    onSave: (ProjectDraft) -> Unit
) {

    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }

    val availableSkills = listOf(
        "Python",
        "Machine Learning",
        "Data Analysis",
        "Android / Kotlin",
        "UI/UX",
        "Backend",
        "Cloud",
        "Angular"
    )

    var skillsMenuExpanded by remember { mutableStateOf(false) }
    var selectedSkills by remember { mutableStateOf<List<String>>(emptyList()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val draft = ProjectDraft(
                        title = title.text.trim(),
                        description = description.text.trim(),
                        skills = selectedSkills.toList(),
                        imageUri = imageUri?.toString()
                    )
                    onSave(draft)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TitleGreen
                )
            ) {
                Text("Cancel")
            }
        },
        title = { Text("Create new project", color = TitleGreen) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = selectedSkills.joinToString(", "),
                        onValueChange = {},
                        label = { Text("Skills") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { skillsMenuExpanded = true },
                        enabled = false,
                        readOnly = true
                    )

                    DropdownMenu(
                        expanded = skillsMenuExpanded,
                        onDismissRequest = { skillsMenuExpanded = false }
                    ) {
                        availableSkills.forEach { skill ->
                            val isSelected = skill in selectedSkills
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isSelected) "✓ $skill" else skill
                                    )
                                },
                                onClick = {
                                    selectedSkills = if (isSelected) {
                                        selectedSkills - skill
                                    } else {
                                        selectedSkills + skill
                                    }
                                }
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (imageUri == null) "Add picture" else "Change picture",
                        color = TitleGreen
                    )
                }
                if (imageUri != null) {
                    Text(
                        text = "Imagen seleccionada: ${imageUri.toString()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )

}

