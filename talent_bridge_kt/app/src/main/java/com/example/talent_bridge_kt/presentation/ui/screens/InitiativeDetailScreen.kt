package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.FlowRow
import com.example.talent_bridge_kt.R

// Si no tienes estos colores en tu tema, reemplaza por Color(...) directamente.
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.LinkGreen
import com.example.talent_bridge_kt.ui.theme.TitleGreen

/* ======================= Pantalla Detalle ======================= */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InitiativeDetailScreen(
    onBack: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
    onHome: () -> Unit = {},
    onSearch: () -> Unit = {},
    onFav: () -> Unit = {},
) {
    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            // Top bar local a este archivo
            TopBarLocal(height = 56.dp, onBack = onBack, onMenu = onOpenMenu)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                /* ---- Header con avatar + info ---- */
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.iniciativa),
                            contentDescription = "avatar",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFEDEDED), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Iniciativa 1",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TitleGreen
                            )
                            Text("Multidisciplinaria",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("usuario123@gmail.com", fontSize = 12.sp, color = Color.Gray)
                            Text("usuario123", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                /* ---- Title: Description (centrado) ---- */
                item {
                    Text(
                        text = "Description",
                        color = AccentYellow,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                /* ---- Párrafos ---- */
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Iniciativa de IA", fontSize = 14.sp, color = Color.DarkGray)
                        Text(
                            "Aceptamos estudiantes de todos los semestre",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                /* ---- Chips centradas (wrap en varias filas) ---- */
                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tags = listOf("Diseño", "Dibujo", "2 Horas", "Diseño", "Diseño")
                        tags.forEach { ChipTagLocal(it) }
                    }
                }

                /* ---- Title: Offers Available (alineado a la izquierda) ---- */
                item {
                    Text(
                        text = "Offers Available",
                        color = AccentYellow,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                /* ---- Card de oferta (local) ---- */
                item {
                    OfferCardLocal(
                        time = "Iniciativa 1 • 5m",
                        title = "Buscamos Diseñadores",
                        subtitle = "Personas interesadas en el diseño gráfico.",
                        description = "Esperamos una disponibilidad de 2 horas semanales.",
                        tags = listOf("Diseño", "Dibujo", "2 Horas")
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }
            }

            // Bottom bar local a este archivo
            BottomBarLocal(
                onHome = onHome,
                onSearch = onSearch,
                onMenu = onOpenMenu,
                onFav = onFav
            )
        }
    }
}

/* =================== Componentes locales =================== */

@Composable
private fun TopBarLocal(
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
private fun BottomBarLocal(
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
private fun ChipTagLocal(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, AccentYellow, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color.DarkGray)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfferCardLocal(
    time: String,
    title: String,
    subtitle: String,
    description: String,
    tags: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // encabezado mini
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

        // chips centradas
        Spacer(Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { ChipTagLocal(it) }
        }

        // acciones
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Comentarios(0)", fontSize = 12.sp, color = LinkGreen)
            Text("Guardar", fontSize = 12.sp, color = TitleGreen)
            Text("Aplicar", fontSize = 12.sp, color = TitleGreen)
        }
    }
}


