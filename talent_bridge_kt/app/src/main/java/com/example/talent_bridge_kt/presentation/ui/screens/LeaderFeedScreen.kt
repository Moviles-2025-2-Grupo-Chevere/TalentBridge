@file:JvmName("LeaderFeedScreenKt")

package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.sync.Mutex

/* ======================= Pantalla ======================= */

@Composable
fun LeaderFeedScreen(
    onBack: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    // bottom bar
    onHome: () -> Unit = {},
    onSearch: () -> Unit = {},
    onFav: () -> Unit = {},
) {
    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            TopBarCustom(height = 100.dp, onBack = onBack, onMenu = onOpenMenu, onDrawer = onOpenDrawer)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Explore Students",
                        color = AccentYellow,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)

                    )
                }


                item {
                    StudentCardSimple(
                        avatarRes = R.drawable.leader, // usa tu recurso
                        name = "Daniel Triviño",
                        career = "Estudiante de Ing. Sistemas y Comp.",
                        interest = "Interesado en proyectos con paga",
                        experienceTitle = "Experiencia previa:",
                        experienceBullets = listOf("Multiservi"),
                        tags = listOf("Angular", "Diseño", "Python")
                    )
                }
                item {
                    StudentCardSimple(
                        avatarRes = R.drawable.iniciativa,
                        name = "María Paula Murillo",
                        career = "Diseño Industrial",
                        interest = "Interesado en conseguir experiencia",
                        experienceTitle = "Experiencia previa:",
                        experienceBullets = listOf("Monitoria de investigación en IAU"),
                        tags = listOf("Diseño", "Dibujo", "Foto", "CAD", "Pintura")
                    )
                }
                item {
                    StudentCardSimple(
                        avatarRes = R.drawable.leader,
                        name = "Juan Diego Lozano",
                        career = "Ingeniería Industrial",
                        interest = "Interesado en proyectos pagos",
                        experienceTitle = "Experiencia previa:",
                        experienceBullets = emptyList(),
                        tags = listOf("Excel", "Fintech", "Word")
                    )
                }
                item {
                    StudentCardSimple(
                        avatarRes = R.drawable.logo,
                        name = "Ingeniería de Sistemas y Computación",
                        career = "",
                        interest = "Interesados en proyectos pagos",
                        experienceTitle = "",
                        experienceBullets = listOf("Esperamos una disponibilidad de 2 horas semanales."),
                        tags = listOf("Diseño", "Dibujo", "2 Horas")
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }


                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.height(28.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
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
private fun StudentCardSimple(
    avatarRes: Int,
    name: String,
    career: String,
    interest: String,
    experienceTitle: String,
    experienceBullets: List<String>,
    tags: List<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Header con avatar + nombre
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = name,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(name, color = TitleGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (career.isNotBlank()) {
                    Text(career, color = Color.DarkGray, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(interest, color = Color.DarkGray, fontSize = 12.sp)

        if (experienceTitle.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(experienceTitle, color = TitleGreen, fontSize = 12.sp)
        }
        experienceBullets.forEach {
            Row(verticalAlignment = Alignment.Top) {
                Text("•", color = Color.DarkGray, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
                Text(it, color = Color.DarkGray, fontSize = 12.sp)
            }
        }

        // Chips
        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), // que ocupe todo el ancho
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
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

        }

        // Acciones
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { /* comentarios */ },
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
                onClick = { /* contactar */ },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TitleGreen
                )
            ) { Text("Contact", fontSize = 12.sp) }


        }
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
            Spacer(Modifier.width(4.dp))
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


