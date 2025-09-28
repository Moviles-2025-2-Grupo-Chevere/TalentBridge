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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.LinkGreen
import com.example.talent_bridge_kt.ui.theme.TitleGreen

/* =========================================================================
 * ExploreProjectsScreen – con header, cards, bottom bar y pop-up de “Aplicar”
 * ========================================================================= */
@Composable
fun StudentFeedScreen(
    onBack: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
    // Bottom bar
    onHome: () -> Unit = {},
    onSearch: () -> Unit = {},
    onFav: () -> Unit = {},
    // Navegación desde el pop-up (opcional)
    onGoToApplications: () -> Unit = {}
) {
    var showSubmitted by remember { mutableStateOf(false) }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            // Encabezado fijo (logo + back + menú)
            TopBarCustom(height = 56.dp, onBack = onBack, onMenu = onOpenMenu)

            // Contenido
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

                // Card 1
                item {
                    ProjectCardSimple(
                        time = "1 · 5m",
                        title = "Buscamos Diseñadores",
                        subtitle = "Personas interesadas en el diseño gráfico.",
                        description = "Esperamos una disponibilidad de 2 horas semanales.",
                        tags = listOf("Diseño", "Dibujo", "2 Horas"),
                        imageRes = R.drawable.robocol,
                        onApplyClick = { showSubmitted = true }
                    )
                }

                // Card 2
                item {
                    ProjectCardSimple(
                        time = "2 · 10m",
                        title = "Buscamos Diseñadores",
                        subtitle = "Personas interesadas en el diseño gráfico.",
                        description = "Esperamos una disponibilidad de 2 horas semanales.",
                        tags = listOf("Diseño", "Dibujo", "2 Horas"),
                        imageRes = R.drawable.relaja,
                        onApplyClick = { showSubmitted = true }
                    )
                }

                // Card 3 (sin imagen)
                item {
                    ProjectCardSimple(
                        time = "hoy",
                        title = "Concurso Villains",
                        subtitle = "Afiche para feria 2025",
                        description = "Esperamos una disponibilidad de 2 horas semanales.",
                        tags = listOf("Diseño", "Dibujo", "2 Horas"),
                        imageRes = null,
                        onApplyClick = { showSubmitted = true }
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }
            }

            // Menú inferior fijo
            BottomBarCustom(
                onHome = onHome,
                onSearch = onSearch,
                onMenu = onOpenMenu,
                onFav = onFav
            )
        }
    }

    // Pop-up “Application Submitted!”
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

/* ============================ COMPONENTES ============================ */

@Composable
private fun ProjectCardSimple(
    time: String,
    title: String,
    subtitle: String,
    description: String,
    tags: List<String>,
    imageRes: Int?,                   // puede ser null
    onApplyClick: () -> Unit
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
                modifier = Modifier.size(28.dp),
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
            OutlinedButton(
                onClick = { /* guardar */ },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TitleGreen
                )
            ) { Text("Comments (0)", fontSize = 12.sp) }

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
    onMenu: () -> Unit
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