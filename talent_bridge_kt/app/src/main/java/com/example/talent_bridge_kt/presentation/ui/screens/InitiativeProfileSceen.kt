package com.example.talent_bridge_kt.presentation.ui.screens

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InitiativeProfileSceen(
    modifier: Modifier = Modifier,
    onEditNumber: () -> Unit = {},
    onAddOferta: () -> Unit = {},
    onAddProject: () -> Unit = {},
    onBack: () -> Unit = {},
) {

    var email by remember { mutableStateOf("omargalvez@gmail.com") }
    var linkedin by remember { mutableStateOf("omargalvez7") }
    var number by remember { mutableStateOf<String?>(null) }
    val tags = remember { listOf("Ingenieria de sistemas") }

    Surface(color = CreamBackground, modifier = modifier.fillMaxSize()) {


        Column(modifier = Modifier.fillMaxSize()) {

            TopBarCustom(
                height = 100.dp,
                onBack = onBack,
                onMenu = {  }
            )


            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.iniciativa),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE7F6)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Iniciativa AI",
                                fontSize = 18.sp,
                                color = TitleGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Contacto
                    item { SectionTitle("Contacto") }
                    item {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LabeledValue("Email:", email)
                            LabeledValue("LinkedIn:", linkedin)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Número:", color = TitleGreen, fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                if (number.isNullOrBlank()) {
                                    Text(
                                        "Agregar número",
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

                    // Descripción
                    item { SectionTitle("Descripción") }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Buscamos interesados en proyectos de AI.",
                                fontSize = 14.sp, color = Color.DarkGray
                            )
                            Text("Perfiles:", fontSize = 14.sp, color = TitleGreen)
                            Bullet("Aceptamos estudiantes de todos los semestres")
                        }
                    }

                    // Chips
                    item {
                        Column {
                            Text("Carrera", fontSize = 12.sp, color = Color.DarkGray)
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

                    // CV y Portafolio (arreglo de weight)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AddBox(
                                title = "Agregar Oferta",
                                modifier = Modifier.weight(1f),
                                onClick = onAddOferta
                            )
                        }
                    }

                    item { Spacer(Modifier.height(28.dp)) }
                    item {
                        Column(Modifier.padding(24.dp)) {
                            Text("Saved Projects")
                            TextButton(onClick = onBack) { Text("Volver") }
                        }
                    }
                }
            }

            BottomBarCustom(
                onHome = {  },
                onSearch = {  },
                onMenu = {  },
                onFav = {  }
            )
        }
    }
}

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
        // Back (izquierda)
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TitleGreen)
            }
        }
        // Logo
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Talent Bridge",
                modifier = Modifier.height(70.dp),
                contentScale = ContentScale.Fit
            )
        }
        // Menú
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            IconButton(onClick = onHome) { Icon(Icons.Filled.Home,  contentDescription = "Home",  tint = TitleGreen) }
            IconButton(onClick = onSearch) { Icon(Icons.Filled.Search,contentDescription = "Search",tint = TitleGreen) }
            IconButton(onClick = onMenu) { Icon(Icons.Filled.Menu,  contentDescription = "Menu",  tint = TitleGreen) }
            IconButton(onClick = onFav)  { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Fav", tint = TitleGreen) }
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
                contentDescription = "Agregar",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, color = TitleGreen, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            "Agregar link",
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
            Text("Agregar proyecto")
        }
    }
}