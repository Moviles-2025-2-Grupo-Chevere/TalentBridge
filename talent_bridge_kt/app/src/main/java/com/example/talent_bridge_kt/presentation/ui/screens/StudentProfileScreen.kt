package com.example.talent_bridge_kt.presentation.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.LinkGreen
import com.example.talent_bridge_kt.ui.theme.TitleGreen

// VM + casos de uso + fake repo (ajusta paquetes si difieren en tu proyecto)
import com.example.talent_bridge_kt.data.fake.FakeProfileRepository
import com.example.talent_bridge_kt.domain.usecase.GetProfileUseCase
import com.example.talent_bridge_kt.domain.usecase.UpdateProfileUseCase
import com.example.talent_bridge_kt.domain.usecase.UploadAvatarUseCase
import com.example.talent_bridge_kt.presentation.ui.viewmodel.ProfileUiState
import com.example.talent_bridge_kt.presentation.ui.viewmodel.ProfileViewModel

import androidx.core.content.FileProvider
import androidx.compose.ui.text.style.TextOverflow
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    onEditNumber: () -> Unit = {},
    onAddCv: () -> Unit = {},
    onAddPortfolio: () -> Unit = {},
    onAddProject: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // --------- TUS ESTADOS ORIGINALES ---------
    var email by remember { mutableStateOf("lucianaperez@gmail.com") }
    var linkedin by remember { mutableStateOf("lucianap23") }
    var number by remember { mutableStateOf<String?>(null) }
    val localTags = remember { listOf("Diseño", "UI/UX", "AI") }

    // --------- ViewModel (wiring simple sin Hilt) ---------
    val repo = remember { FakeProfileRepository() }
    val vm = remember {
        ProfileViewModel(
            getProfile = GetProfileUseCase(repo),
            updateProfile = UpdateProfileUseCase(repo),
            uploadAvatar = UploadAvatarUseCase(repo)
        )
    }
    val uiState by vm.uiState.collectAsState()

    // --------- Cámara con TakePicture + FileProvider ---------
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createTempImageUri(): Uri {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("avatar_", ".jpg", imagesDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // Evita smart-cast error usando let
        if (success) cameraUri?.let { vm.onAvatarPicked(it) }
    }

    Surface(color = CreamBackground, modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBarCustom(
                height = 64.dp,
                onBack = onBack,
                onMenu = { }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header (igual layout; Image -> AsyncImage + click para abrir cámara)
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val avatarModel: Any = when (val s = uiState) {
                            is ProfileUiState.Ready -> s.profile.avatarUrl ?: R.drawable.student1
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
                                    takePicture.launch(tmp) // pasamos Uri no nulo
                                },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = when (val s = uiState) {
                                is ProfileUiState.Ready -> s.profile.name.ifBlank { "LucianaPerez" }
                                else -> "LucianaPerez"
                            },
                            fontSize = 18.sp,
                            color = TitleGreen,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Contact
                item { SectionTitle("Contact") }
                item {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LabeledValue("Email:", email)
                        LabeledValue("LinkedIn:", linkedin)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Number:", color = TitleGreen, fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            if (number.isNullOrBlank()) {
                                Text(
                                    "Add number",
                                    color = LinkGreen,
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { onEditNumber() }
                                )
                            } else {
                                Text(number!!, fontSize = 14.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }

                // Description
                item { SectionTitle("Description") }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            when (val s = uiState) {
                                is ProfileUiState.Ready -> s.profile.bio ?: "Interesado en proyectos con paga con relación a la IA."
                                else -> "Interesado en proyectos con paga con relación a la IA."
                            },
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Text("Previous experience:", fontSize = 14.sp, color = TitleGreen)
                        Bullet("Monitoria IP")
                    }
                }

                // Tags
                item {
                    val tags = when (val s = uiState) {
                        is ProfileUiState.Ready -> if (s.profile.tags.isNotEmpty()) s.profile.tags else localTags
                        else -> localTags
                    }
                    Column {
                        Text("Career", fontSize = 12.sp, color = Color.DarkGray)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, AccentYellow, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(tag, fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }

                // Acciones
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AddBox(
                            title = "Add CV",
                            modifier = Modifier.weight(1f),
                            onClick = onAddCv
                        )
                        AddBox(
                            title = "Add Portafolio",
                            modifier = Modifier.weight(1f),
                            onClick = onAddPortfolio
                        )
                    }
                }

                // Mis Proyectos
                item { SectionTitle("My Projects") }
                item { EmptyProjectsCard(onAddProject) }

                item { Spacer(Modifier.height(28.dp)) }
                item {
                    Column(Modifier.padding(24.dp)) {
                        Text("Saved Projects")
                        TextButton(onClick = onBack) { Text("Back") }
                    }
                }
            }

            BottomBarCustom(
                onHome = {  },
                onSearch = { },
                onMenu = {  },
                onFav = {  }
            )
        }
    }
}

// ===================================================================================
// Helpers en el MISMO ARCHIVO (así no chocan los “private in file” de otros archivos)
// ===================================================================================

@Composable
private fun TopBarCustom(
    height: Dp,
    onBack: () -> Unit,
    onMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(2.dp)
            .background(CreamBackground)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Talent Bridge",
                modifier = Modifier.height(90.dp),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TitleGreen)
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onMenu) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = TitleGreen)
            }
        }
    }
}

@Composable
private fun BottomBarCustom(
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onMenu: () -> Unit,
    onFav: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(2.dp)
            .background(CreamBackground)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHome)  { Icon(Icons.Filled.Home,  contentDescription = "Home",  tint = TitleGreen) }
            IconButton(onClick = onSearch){ Icon(Icons.Filled.Search,contentDescription = "Search",tint = TitleGreen) }
            IconButton(onClick = onMenu)  { Icon(Icons.Filled.Menu,  contentDescription = "Menu",  tint = TitleGreen) }
            IconButton(onClick = onFav)   { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Fav", tint = TitleGreen) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = AccentYellow,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp)
    )
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
private fun AddBox(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, color = TitleGreen, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            "Add link",
            color = LinkGreen,
            fontSize = 12.sp,
            modifier = Modifier.clickable { onClick() }
        )
    }
}

@Composable
private fun EmptyProjectsCard(onAddProject: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No tienes proyectos activos.", fontSize = 13.sp, color = Color.DarkGray)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onAddProject,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = AccentYellow,
                contentColor = Color.White
            ),
        ) {
            Text("Add proyecto")
        }
    }
}
