package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.presentation.ui.viewmodel.ProjectsViewModel
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.TitleGreen
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.lazy.items
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
private fun projectsVmFactory(app: Application) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return com.example.talent_bridge_kt.presentation.ui.viewmodel.ProjectsViewModel(app) as T
        }
    }
@Composable
fun StudentFeedScreen(
    onBack: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    // Bottom bar
    onHome: () -> Unit = {},
    onSearch: () -> Unit = {},
    onFav: () -> Unit = {},
    // Navegación desde el pop-up (opcional)
    onGoToApplications: () -> Unit = {},
    onSomeOneElseProfile: () -> Unit = {},
    onExploreStudents: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: ProjectsViewModel = viewModel(
        factory = projectsVmFactory(context.applicationContext as Application)
    )
    val projects by vm.projects.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val savedList by vm.savedProjects.collectAsState()
    val savedIds = remember(savedList) { savedList.map { it.id }.toSet() }


    var showSubmitted by remember { mutableStateOf(false) }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            TopBarCustom(
                height = 56.dp,
                onBack = onBack,
                onMenu = onExploreStudents,
                onDrawer = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Explore Projects",
                        color = AccentYellow,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }

                // Estado: Loading
                if (loading) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Estado: Error
                if (error != null) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Failed to load: $error", color = Color.Red)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = { vm.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                // Estado: Lista OK
                if (!loading && error == null) {
                    items(projects, key = { it.id }) { p ->
                        val isSaved = savedIds.contains(p.id)

                        ProjectCardSimple(
                            time = p.createdAt?.let { prettySince(it.toDate().time) } ?: "—",
                            title = p.title,
                            subtitle = p.subtitle ?: "",
                            description = p.description,
                            tags = p.skills,
                            imageRes = null, // Si luego guardas URL, cámbialo por AsyncImage con p.imgUrl
                            saved = isSaved,                        // 👈 NUEVO
                            onSaveClick = { vm.toggleFavorite(p) }, // 👈 NUEVO
                            onApplyClick = { showSubmitted = true },
                            onSomeOneElseProfile = onSomeOneElseProfile
                        )
                    }

                    if (projects.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No projects yet.")
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }

            BottomBarCustom(
                onHome = onHome,
                onSearch = onSearch,
                onProfile = onProfile,
                onFav = onFav
            )
        }
    }

    if (showSubmitted) {
        ApplicationSubmittedDialog(
            onMyApplications = {
                showSubmitted = false
                onGoToApplications()
            },
            onKeepExploring = { showSubmitted = false }
        )
    }
}

/* ============================ Helpers ============================ */

// Muestra "5m", "2h", "3d" desde el millis indicado
private fun prettySince(thenMs: Long): String {
    val diff = System.currentTimeMillis() - thenMs
    val min = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hrs = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        min < 1 -> "now"
        min < 60 -> "${min}m"
        hrs < 24 -> "${hrs}h"
        else -> "${days}d"
    }
}

/* ============================ (tus componentes existentes) ============================ */
/* ProjectCardSimple, ApplicationSubmittedDialog, TopBarCustom, BottomBarCustom
   — déjalos como los tienes. No necesitan cambios para pintar datos del repo. */

/* ============================ COMPONENTES ============================ */

@Composable
private fun ProjectCardSimple(
    time: String,
    title: String,
    subtitle: String,
    description: String,
    tags: List<String>,
    imageRes: Int?,
    saved: Boolean,
    onSaveClick: () -> Unit,
    onApplyClick: () -> Unit,
    onSomeOneElseProfile: () -> Unit

) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // mini-header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.iniciativa),
                contentDescription = "iniciativa",
                modifier = Modifier.size(28.dp) .clickable { onSomeOneElseProfile() },
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Text(text = "iniciativa • $time", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(Modifier.height(8.dp))
        Text(title, color = TitleGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, fontSize = 12.sp, color = Color.DarkGray)
        Spacer(Modifier.height(8.dp))
        Text(description, fontSize = 12.sp, color = Color.DarkGray)

        if (imageRes != null) {
            Spacer(Modifier.height(10.dp))
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // Chips
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally) // centra las chips y mantiene espacio
        ) {
            tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .border(1.dp, AccentYellow, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = tag, fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }


        // Acciones
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---- SAVE / SAVED ----
            OutlinedButton(
                onClick = onSaveClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (saved) TitleGreen.copy(alpha = 0.08f) else Color.White,
                    contentColor = TitleGreen
                )
            ) { Text(if (saved) "Saved" else "Save", fontSize = 12.sp) }


            OutlinedButton(
                onClick = { /* guardar */ },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TitleGreen
                )
            ) { Text("Save", fontSize = 12.sp) }

            OutlinedButton(
                onClick = onApplyClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TitleGreen
                )
            ) { Text("Apply", fontSize = 12.sp) }

        }
    }
}

/* ------------------------- Pop-up/Diálogo ------------------------- */

@Composable
private fun ApplicationSubmittedDialog(
    onMyApplications: () -> Unit,
    onKeepExploring: () -> Unit
) {
    // Scrim oscuro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        // Tarjeta centrada
        Column(
            modifier = Modifier
                .widthIn(min = 240.dp, max = 320.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFF0F6A7A), RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Application\nSubmitted!",
                color = Color.White,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Your application has been\nsubmitted successfully.",
                color = Color(0xFFEAF7FA),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onMyApplications,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0F6A7A)
                    )
                ) { Text("My applications", fontSize = 12.sp) }

                Button(
                    onClick = onKeepExploring,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0F6A7A)
                    )
                ) { Text("Keep Exploring", fontSize = 12.sp) }
            }
        }
    }
}

/* -------------------------- Top / Bottom bars -------------------------- */

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
    onProfile: () -> Unit,
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
            IconButton(onClick = onProfile)  { Icon(Icons.Filled.Person,  contentDescription = "Profile",  tint = TitleGreen) }
            IconButton(onClick = onFav)   { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Fav", tint = TitleGreen) }
        }
    }
}