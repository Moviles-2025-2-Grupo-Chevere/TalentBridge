package com.example.talent_bridge_kt.presentation.ui.screens

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import com.example.talent_bridge_kt.domain.model.Project
import com.example.talent_bridge_kt.presentation.ui.viewmodel.ProjectsViewModel
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.LinkGreen
import com.example.talent_bridge_kt.ui.theme.TitleGreen

/* ======================= Factory para el ViewModel ======================= */

private fun savedVmFactory(app: Application) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectsViewModel(app) as T
        }
    }

/* ======================= Pantalla ======================= */

@Composable
fun SavedProjectsScreen(
    onBack: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    // bottom bar
    onHome: () -> Unit = {},
    onSearch: () -> Unit = {},
    onFav: () -> Unit = {},
) {
    // Obtener VM con Application (igual que en tu feed)
    val context = LocalContext.current
    val vm: ProjectsViewModel = viewModel(
        factory = savedVmFactory(context.applicationContext as Application)
    )

    // Observa los guardados desde Room
    val savedList by vm.savedProjects.collectAsState()
    val nonEmptySaved = remember(savedList) { savedList.isNotEmpty() }

    // Estado de aplicaciones existentes
    val appliedIds by vm.appliedProjectIds.collectAsState()

    // Eventos informativos (cola offline / sync ok)
    val appEvent by vm.applicationEvent.collectAsState()
    var showInfo by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("") }
    LaunchedEffect(appEvent) {
        appEvent?.let {
            infoMessage = it
            showInfo = true
        }
    }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            TopBarCustom(
                height = 56.dp,
                onBack = onBack,
                onMenu = onOpenMenu,
                onDrawer = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Saved Projects",
                        color = AccentYellow,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }

                if (!nonEmptySaved) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("You haven’t saved any projects yet.", color = Color.Gray)
                        }
                    }
                } else {
                    items(savedList, key = { it.id }) { e ->
                        // Mapear skills (CSV) a lista para chips
                        val tags = remember(e.skills) {
                            e.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        }

                        val asDomain = e.toDomain()
                        val isApplied = appliedIds.contains(e.id)

                        SavedProjectCard(
                            time = e.createdAt ?: "—",
                            title = e.title,
                            subtitle = e.subtitle ?: "",
                            description = e.description,
                            tags = tags,
                            applied = isApplied,
                            onApplyToggle = {
                                vm.toggleApplication(
                                    asDomain,
                                    onApplied = { /* opcional */ },
                                    onUnapplied = { /* opcional */ },
                                    onError = { /* opcional */ },
                                    onQueuedOffline = { /* Info llega por appEvent */ }
                                )
                            },
                            onRemove = { vm.toggleFavorite(asDomain) } // desmarca favorito
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }

            BottomBarCustom(
                onHome = onHome,
                onSearch = onSearch,
                onMenu = onOpenMenu,
                onFav = onFav
            )
        }
    }

    if (showInfo) {
        InfoDialog(message = infoMessage, onOk = { showInfo = false; infoMessage = "" })
    }
}

/* =================== Componentes de UI =================== */

@Composable
private fun InfoDialog(message: String, onOk: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 240.dp, max = 320.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFF0F6A7A), RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                message,
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onOk,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0F6A7A)
                )
            ) { Text("OK", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun SavedProjectCard(
    time: String,
    title: String,
    subtitle: String,
    description: String,
    tags: List<String>,
    applied: Boolean,
    onApplyToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // mini header con ícono de iniciativa y tiempo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.iniciativa),
                contentDescription = "iniciativa",
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Text(text = "iniciativa • $time", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(Modifier.height(8.dp))
        Text(title, color = TitleGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        if (subtitle.isNotBlank()) {
            Text(subtitle, fontSize = 12.sp, color = Color.DarkGray)
        }
        Spacer(Modifier.height(8.dp))
        Text(description, fontSize = 12.sp, color = Color.DarkGray)

        // Chips (skills)
        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { ChipTag(it) }
            }
        }

        // Acciones
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Apply / Applied toggle
            OutlinedButton(
                onClick = onApplyToggle,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (applied) TitleGreen.copy(alpha = 0.08f) else Color.White,
                    contentColor = TitleGreen
                )
            ) { Text(if (applied) "Applied" else "Apply", fontSize = 12.sp) }

            // Remove (unsave)
            OutlinedButton(
                onClick = onRemove,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = TitleGreen.copy(alpha = 0.08f),
                    contentColor = TitleGreen
                )
            ) { Text("Remove", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun ChipTag(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, AccentYellow, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color.DarkGray)
    }
}

/* =============== Top / Bottom bars (custom) =============== */

@Composable
private fun TopBarCustom(
    height: Dp,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onDrawer: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(2.dp)
            .background(CreamBackground)
            .padding(horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Talent Bridge",
                modifier = Modifier.height(80.dp),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TitleGreen)
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onMenu) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = TitleGreen)
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDrawer) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Open drawer", tint = TitleGreen)
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
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHome)  { Icon(Icons.Filled.Home,  contentDescription = "Home",  tint = TitleGreen) }
            IconButton(onClick = onSearch){ Icon(Icons.Filled.Search,contentDescription = "Search",tint = TitleGreen) }
            IconButton(onClick = onMenu)  { Icon(Icons.Filled.Menu,  contentDescription = "Menu",  tint = TitleGreen) }
            IconButton(onClick = onFav)   { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Fav", tint = TitleGreen) }
        }
    }
}

/* =================== Helpers / Mappers =================== */

private fun ProjectEntity.toDomain(): Project = Project(
    id = id,
    title = title,
    subtitle = subtitle,
    description = description,
    skills = if (skills.isBlank()) emptyList() else skills.split(",").map { it.trim() },
    imgUrl = imgUrl,
    createdAt = null,
    createdById = createdById
)
