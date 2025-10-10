package com.example.talent_bridge_kt.presentation.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.data.firebase.FirebaseProfileRepository
import com.example.talent_bridge_kt.domain.model.Project
import com.example.talent_bridge_kt.domain.usecase.GetProfileUseCase
import com.example.talent_bridge_kt.domain.usecase.UpdateProfileUseCase
import com.example.talent_bridge_kt.domain.usecase.UploadAvatarUseCase
import com.example.talent_bridge_kt.presentation.ui.viewmodel.ProfileUiState
import com.example.talent_bridge_kt.presentation.ui.viewmodel.ProfileViewModel
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.LinkGreen
import com.example.talent_bridge_kt.ui.theme.TitleGreen
import androidx.core.content.FileProvider
import androidx.compose.foundation.text.KeyboardOptions
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    onEditNumber: () -> Unit = {},
    onAddCv: () -> Unit = {},
    onAddPortfolio: () -> Unit = {},
    onAddProject: () -> Unit = {},
    onBack: () -> Unit = {},
    onOpenDrawer: () -> Unit = {}

) {
    // --------- estado de edición y campos básicos ---------
    var email by remember { mutableStateOf("lucianaperez@gmail.com") }
    var linkedin by remember { mutableStateOf("lucianap23") }
    var number by remember { mutableStateOf<String?>(null) }
    var bio by remember { mutableStateOf("Interesado en proyectos con paga con relación a la IA.") }
    var isEditing by remember { mutableStateOf(false) }

    // entrada para nuevo tag
    var newTag by remember { mutableStateOf("") }

    // dialog para nuevo proyecto
    var showProjectDialog by remember { mutableStateOf(false) }
    var pTitle by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    var pSkills by remember { mutableStateOf("") }

    val repo = remember { FirebaseProfileRepository() }

    val vm = remember {
        ProfileViewModel(
            getProfile = GetProfileUseCase(repo),
            updateProfile = UpdateProfileUseCase(repo),
            uploadAvatar = UploadAvatarUseCase(repo)
        )
    }
    val uiState by vm.uiState.collectAsState()
    LaunchedEffect(Unit) {
        vm.load()
    }

    LaunchedEffect(uiState) {
        if (isEditing) return@LaunchedEffect
        val p = (uiState as? ProfileUiState.Ready)?.profile ?: return@LaunchedEffect
        email = p.email
        linkedin = p.linkedin.orEmpty()
        number = p.phone
        bio = p.bio ?: bio
    }


    // --------- cámara ---------
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    fun createTempImageUri(): Uri {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("avatar_", ".jpg", imagesDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) cameraUri?.let { vm.onAvatarPicked(it) }
    }

    Surface(color = CreamBackground, modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            TopBarCustom(
                height = 64.dp,
                onBack = onBack,
                onMenu = { },
                onDrawer = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ------------------ Header ------------------
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        val avatarModel: Any = when (val s = uiState) {
                            is ProfileUiState.Ready -> {
                                val url = s.profile.avatarUrl.orEmpty()
                                if (url.isNotBlank()) url else R.drawable.student1
                            }
                            else -> R.drawable.student1
                        }

                        AsyncImage(
                            model = avatarModel,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEDE7F6))
                                .clickable {
                                    val tmp = createTempImageUri()
                                    cameraUri = tmp
                                    takePicture.launch(tmp)
                                },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = when (val s = uiState) {
                                is ProfileUiState.Ready -> s.profile.name.ifBlank { "" }
                                else -> ""
                            },
                            fontSize = 18.sp, color = TitleGreen, fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isEditing = !isEditing }) {
                                Icon(
                                    if (isEditing) Icons.Filled.Close else Icons.Filled.Edit,
                                    contentDescription = null, tint = TitleGreen
                                )
                            }
                            if (isEditing) Text("Editing", color = TitleGreen, fontSize = 14.sp)
                        }
                    }
                }

                // ------------------ Contact ------------------
                item { SectionTitle("Contact") }
                item {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!isEditing) {
                            LabeledValue("Email:", email)
                            LabeledValue("LinkedIn:", linkedin)
                        } else {
                            OutlinedTextField(
                                value = email, onValueChange = { email = it },
                                label = { Text("Email") }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = linkedin, onValueChange = { linkedin = it },
                                label = { Text("LinkedIn (usuario o URL)") }, singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Number:", color = TitleGreen, fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            if (!isEditing) {
                                if (number.isNullOrBlank()) {
                                    Text("Add number", color = LinkGreen, fontSize = 14.sp,
                                        modifier = Modifier.clickable { onEditNumber() })
                                } else {
                                    Text(number!!, fontSize = 14.sp, color = Color.DarkGray)
                                }
                            } else {
                                OutlinedTextField(
                                    value = number.orEmpty(), onValueChange = { number = it },
                                    label = { Text("Número") }, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // ------------------ Description ------------------
                item { SectionTitle("Description") }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!isEditing) {
                            Text(bio, fontSize = 14.sp, color = Color.DarkGray)
                        } else {
                            OutlinedTextField(
                                value = bio, onValueChange = { bio = it },
                                label = { Text("Descripción / Bio") },
                                modifier = Modifier.fillMaxWidth(), minLines = 3
                            )
                        }
                        Text("Previous experience:", fontSize = 14.sp, color = TitleGreen)
                        Bullet("Monitoria IP")
                    }
                }

                // ------------------ Career (tags) ------------------
                item {
                    val tags = (uiState as? ProfileUiState.Ready)?.profile?.tags ?: emptyList()
                    Column {
                        Text("Career", fontSize = 12.sp, color = Color.DarkGray)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tags.forEach { tag ->
                                if (!isEditing) {
                                    TagPill(tag)
                                } else {
                                    DeletableTagPill(tag) { vm.removeTag(tag) }
                                }
                            }
                        }
                        if (isEditing) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newTag, onValueChange = { newTag = it },
                                    label = { Text("Nuevo tag") }, singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = {
                                    val t = newTag.trim()
                                    if (t.isNotEmpty()) vm.addTag(t)
                                    newTag = ""
                                }) { Text("Add") }
                            }
                        }
                    }
                }

                // ------------------ Acciones CV/Portafolio ------------------
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AddBox(title = "Add CV", modifier = Modifier.weight(1f), onClick = onAddCv)
                        AddBox(title = "Add Portafolio", modifier = Modifier.weight(1f), onClick = onAddPortfolio)
                    }
                }

                // ------------------ My Projects ------------------
                item { SectionTitle("My Projects") }
                item {
                    val projects = (uiState as? ProfileUiState.Ready)?.profile?.projects ?: emptyList()
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                        if (projects.isEmpty()) {
                            Text("No tienes proyectos activos.", fontSize = 13.sp, color = Color.DarkGray)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                projects.forEach { p ->
                                    ProjectCard(
                                        title = p.title,
                                        description = p.description,
                                        skills = p.skills,
                                        canDelete = isEditing,
                                        onDelete = { vm.removeProject(p.id) }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        if (isEditing) {
                            Button(onClick = { showProjectDialog = true }) { Text("Add proyecto") }
                        }
                    }
                }

                // ------------------ Guardar/Cancelar ------------------
                if (isEditing) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    val cur = (uiState as? ProfileUiState.Ready)?.profile ?: return@Button
                                    vm.update(
                                        cur.copy(
                                            email = email,
                                            linkedin = linkedin.ifBlank { null },
                                            phone = number,
                                            bio = bio
                                        )
                                    )
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Done, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Save")
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(28.dp)) }
                item {
                    Column(Modifier.padding(24.dp)) {
                        Text("Saved Projects")
                        TextButton(onClick = onBack) { Text("Back") }
                    }
                }
            }

            BottomBarCustom(
                onHome = { }, onSearch = { }, onMenu = { }, onFav = { }
            )
        }
    }

    if (showProjectDialog) {
        AlertDialog(
            onDismissRequest = { showProjectDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val skills = pSkills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (pTitle.isNotBlank() && pDesc.isNotBlank()) {
                        vm.addProject(Project(title = pTitle.trim(), description = pDesc.trim(), skills = skills))
                        pTitle = ""; pDesc = ""; pSkills = ""; showProjectDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showProjectDialog = false }) { Text("Cancel") } },
            title = { Text("Nuevo proyecto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pTitle, onValueChange = { pTitle = it }, label = { Text("Título") }, singleLine = true)
                    OutlinedTextField(value = pDesc, onValueChange = { pDesc = it }, label = { Text("Descripción") }, minLines = 3)
                    OutlinedTextField(value = pSkills, onValueChange = { pSkills = it }, label = { Text("Skills (coma separadas)") }, singleLine = true)
                }
            }
        )
    }
}


@Composable
private fun TagPill(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, AccentYellow, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) { Text(text, fontSize = 12.sp, color = Color.DarkGray) }
}

@Composable
private fun DeletableTagPill(text: String, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, AccentYellow, RoundedCornerShape(16.dp))
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color.DarkGray)
        IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectCard(
    title: String,
    description: String,
    skills: List<String>,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.SemiBold, color = TitleGreen, fontSize = 16.sp, modifier = Modifier.weight(1f))
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "delete", tint = Color(0xFFB00020))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(description, color = Color.DarkGray, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                skills.forEach { TagPill(it) }
            }
        }
    }
}


@Composable
private fun TopBarCustom(height: Dp, onBack: () -> Unit, onMenu: () -> Unit, onDrawer: () -> Unit) { /* igual a tu versión */
    Box(
        modifier = Modifier.fillMaxWidth().height(height).shadow(2.dp).background(CreamBackground).padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Talent Bridge",
                modifier = Modifier.height(90.dp), contentScale = ContentScale.Fit)
        }
        Row(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TitleGreen) }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onMenu) { Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = TitleGreen) }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDrawer) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Open drawer", tint = TitleGreen)
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun BottomBarCustom(onHome: () -> Unit, onSearch: () -> Unit, onMenu: () -> Unit, onFav: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(64.dp).shadow(2.dp).background(CreamBackground).padding(horizontal = 8.dp)
    ) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onHome)  { Icon(Icons.Filled.Home,  contentDescription = "Home",  tint = TitleGreen) }
            IconButton(onClick = onSearch){ Icon(Icons.Filled.Search,contentDescription = "Search",tint = TitleGreen) }
            IconButton(onClick = onMenu)  { Icon(Icons.Filled.Menu,  contentDescription = "Menu",  tint = TitleGreen) }
            IconButton(onClick = onFav)   { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Fav", tint = TitleGreen) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, color = AccentYellow, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TitleGreen, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Text(value, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("•", modifier = Modifier.padding(end = 6.dp), color = Color.DarkGray)
        Text(text, color = Color.DarkGray, fontSize = 14.sp)
    }
}

@Composable
private fun AddBox(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth().height(110.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Gray, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, color = TitleGreen, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text("Add link", color = LinkGreen, fontSize = 12.sp, modifier = Modifier.clickable { onClick() })
    }
}
