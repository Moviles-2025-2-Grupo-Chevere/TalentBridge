package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@Composable
fun SomeElseProfileScreen(
    onBack: () -> Unit = {},
    onRequestPortfolio: () -> Unit = {},
    onRequestCv: () -> Unit = {},
    onBottomHome: () -> Unit = {},
    onBottomSearch: () -> Unit = {},
    onBottomMenu: () -> Unit = {},
    onBottomFav: () -> Unit = {},
) {
    // Datos demo
    val avatar = R.drawable.student // reemplaza por tu drawable de avatar
    val name = "JuanPabloGomez"
    val subtitle = "Estudiante de Ing. Sistemas y\nComp."
    val description = "Interesado en proyectos con paga con relación a la IA."
    val experienceItems = listOf("Monitor de BI")
    val chips = listOf("ML", "Dibujo", "AI", "BasesdeDatos", "PowerBi")

    val PurpleBorder = Color(0xFFB39DDB)

    Surface(color = CreamBackground, modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {


            TopBarCustom(
                height = 100.dp,
                onBack = onBack,
                onMenu=onBottomMenu
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = avatar),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(4.dp, PurpleBorder, CircleShape)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = name,
                            fontSize = 20.sp,
                            color = TitleGreen,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }


                SectionTitle("Description")
                Text(description, fontSize = 14.sp, color = Color.DarkGray)
                Text("Previous Experience:", fontSize = 14.sp, color = TitleGreen)
                experienceItems.forEach { Bullet(it) }

                ChipsGrid(
                    tags = chips,
                    itemsPerRow = 3,
                    horizontalGap = 8.dp,
                    verticalGap = 8.dp
                )

                // Contacto
                SectionTitle("Contact")

                ContactRow(
                    label = "Email:",
                    value = "juanpablo57@gmail.com",
                    buttonText = "Request portfolio",
                    onAction = onRequestPortfolio
                )

                ContactRow(
                    label = "LinkedIn:",
                    value = "juanpablogomez57",
                    buttonText = "Request CV",
                    onAction = onRequestCv
                )

                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                }
                Spacer(Modifier.height(8.dp))
            }
            BottomBarCustom(
                onHome = onBottomHome,
                onSearch = onBottomSearch,
                onMenu = onBottomMenu,
                onFav = onBottomFav
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
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHome) { Icon(Icons.Filled.Home, contentDescription = "Home", tint = TitleGreen) }
            IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = "Search", tint = TitleGreen) }
            IconButton(onClick = onMenu) { Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = TitleGreen) }
            IconButton(onClick = onFav) { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Fav", tint = TitleGreen) }
        }
    }
}


@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = AccentYellow,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 2.dp)) {
        Text("•", color = Color.DarkGray, modifier = Modifier.padding(end = 6.dp))
        Text(text, color = Color.DarkGray, fontSize = 14.sp)
    }
}

@Composable
private fun ChipsGrid(
    tags: List<String>,
    itemsPerRow: Int,
    horizontalGap: Dp,
    verticalGap: Dp
) {
    val rows = remember(tags, itemsPerRow) { tags.chunked(itemsPerRow) }
    Column(verticalArrangement = Arrangement.spacedBy(verticalGap)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalGap)
            ) {
                row.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, AccentYellow, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) { Text(tag, fontSize = 12.sp, color = Color.DarkGray) }
                }
                repeat(itemsPerRow - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ContactRow(
    label: String,
    value: String,
    buttonText: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TitleGreen, fontSize = 14.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = Color.DarkGray, fontSize = 14.sp)
        }
        OutlinedButton(
            onClick = onAction,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = LinkGreen
            ),
            border = BorderStroke(1.dp, LinkGreen)
        ) {
            Text(buttonText)
        }
    }
}