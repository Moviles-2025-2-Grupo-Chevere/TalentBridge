package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.LinkGreen
import com.example.talent_bridge_kt.ui.theme.TitleGreen

/* ======================= Pantalla ======================= */

@Composable
fun SavedProjectsScreen(
    onBack: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
    // bottom bar
    onHome: () -> Unit = {},
    onSearch: () -> Unit = {},
    onFav: () -> Unit = {},
) {
    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            TopBarCustom(height = 56.dp, onBack = onBack, onMenu = onOpenMenu)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

                // ---- Card 1 ----
                item {
                    SavedProjectCard(
                        time = "Iniciativa 1 • 5m",
                        title = "Buscamos Diseñadores",
                        subtitle = "Personas interesadas en el diseño gráfico.",
                        description = "Esperamos una disponibilidad de 2 horas semanales.",
                        tags = listOf("Diseño", "Dibujo", "2 Horas")
                    )
                }

                // ---- Card 2 ----
                item {
                    SavedProjectCard(
                        time = "Iniciativa 2 • 10m",
                        title = "Buscamos Diseñadores",
                        subtitle = "Personas interesadas en el diseño gráfico.",
                        description = "Esperamos una disponibilidad de 2 horas semanales.",
                        tags = listOf("Diseño", "Dibujo", "2 Horas"),
                        showSecondRowChips = true
                    )
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
}

/* =================== Componentes de UI =================== */

@Composable
private fun SavedProjectCard(
    time: String,
    title: String,
    subtitle: String,
    description: String,
    tags: List<String>,
    showSecondRowChips: Boolean = false
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
            Text(text = time, fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(Modifier.height(8.dp))
        Text(title, color = TitleGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, fontSize = 12.sp, color = Color.DarkGray)
        Spacer(Modifier.height(8.dp))
        Text(description, fontSize = 12.sp, color = Color.DarkGray)

        // Chips fila 1
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            tags.forEach { ChipTag(it) }
        }
        // Chips fila 2 opcional (para imitar tu segunda card)
        if (showSecondRowChips) {
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                ChipTag("Diseño"); ChipTag("Diseño")
            }
        }

        // Acciones
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Comments(0)",
                fontSize = 12.sp,
                color = LinkGreen
            )

            Text(
                "Save",
                fontSize = 12.sp,
                color = TitleGreen
            )
            Text(
                "Apply",
                fontSize = 12.sp,
                color = TitleGreen
            )


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
