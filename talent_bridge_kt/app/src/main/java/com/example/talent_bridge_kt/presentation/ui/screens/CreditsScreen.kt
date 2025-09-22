package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.talent_bridge_kt.ui.theme.TitleGreen

@Composable
fun CreditsScreen(
    onBack: () -> Unit = {}
) {
    Surface(color = CreamBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {


            TopBarLogoOnly(height = 100.dp)


            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero
                Spacer(Modifier.height(16.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "The\nTeam",
                    color = AccentYellow,
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )

                // Botón Home -> back
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TitleGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(24.dp))
                        .height(36.dp)
                ) {
                    Text("Home")
                }

                // Lista de miembros (izq/der alternado)
                Spacer(Modifier.height(16.dp))
                val team = remember {
                    listOf(
                        TeamMember("Daniel Triviño",     R.drawable.student_dani),
                        TeamMember("David Fuquen",      R.drawable.student_david),
                        TeamMember("Mariana Ortega",    R.drawable.student_mari),
                        TeamMember("Manuela Lizcano",    R.drawable.student_manu),
                        TeamMember("Juan Diego Lozano", R.drawable.student_judi),
                        TeamMember("María Paula Murrillo",  R.drawable.student_mp),
                    )
                }

                team.forEachIndexed { index, m ->
                    MemberRow(
                        member = m,
                        alignLeft = index % 2 == 0
                    )
                    Spacer(Modifier.height(20.dp))
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

private data class TeamMember(val name: String, val imageRes: Int)

/** Fila de miembro alternando izquierda/derecha según alignLeft. */
@Composable
private fun MemberRow(member: TeamMember, alignLeft: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = if (alignLeft) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (alignLeft) {
            Avatar(member)
            Spacer(Modifier.width(12.dp))
            Text(member.name, color = AccentYellow, fontSize = 14.sp)
        } else {
            Text(member.name, color = AccentYellow, fontSize = 14.sp)
            Spacer(Modifier.width(12.dp))
            Avatar(member)
        }
    }
}

@Composable
private fun Avatar(member: TeamMember) {
    Image(
        painter = painterResource(id = member.imageRes),
        contentDescription = member.name,
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .border(2.dp, Color(0xFFE3E3E3), CircleShape),
        contentScale = ContentScale.Crop
    )
}


@Composable
private fun TopBarLogoOnly(height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(2.dp)
            .background(CreamBackground)
            .padding(horizontal = 8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Talent Bridge",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .height(70.dp)
        )

    }
}