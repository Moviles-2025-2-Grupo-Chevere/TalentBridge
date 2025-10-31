package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.domain.model.User
import com.example.talent_bridge_kt.presentation.ui.screens.SearchViewModel
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.TitleGreen
import com.example.talent_bridge_kt.presentation.ui.components.OfflineConnectionDialog
import com.example.talent_bridge_kt.core.conectivity.AndroidConnectivityObserver
import androidx.compose.ui.platform.LocalContext


@Composable
fun SearchScreen(
    vm: SearchViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {},
    onSearch: () -> Unit = {},
    onFav: () -> Unit = {},
    onProfile: () -> Unit = {},
    onStudentClick: (String) -> Unit = {}


) {
    var query by remember { mutableStateOf("") }
    val recents = listOf("Daniel Triviño", "ROBOCOL", "Proyectos Inteligencia Artificial")
    val state = vm.uiState
    

    val context = LocalContext.current
    val connectivityObserver = remember { AndroidConnectivityObserver(context) }
    var isConnected by remember { mutableStateOf(true) }
    var showOfflineDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(connectivityObserver) {
        connectivityObserver.isConnected.collect { connected ->
            val wasConnected = isConnected
            isConnected = connected
            // Only show dialog when transitioning from connected to disconnected
            if (wasConnected && !connected) {
                showOfflineDialog = true
            } else if (connected) {
                showOfflineDialog = false
            }
        }
    }

    // [SEARCH-CACHE] Show offline dialog when connection is lost
    if (showOfflineDialog) {
        OfflineConnectionDialog(
            onDismiss = { showOfflineDialog = false }
        )
    }
    
    Surface(color = CreamBackground, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp) ,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "TalentBridge logo",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.DarkGray)
                }
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.DarkGray)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Search",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = AccentYellow,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        vm.onSkillsInput(it)
                    },
                    shape = RoundedCornerShape(50),
                    singleLine = true,
                    placeholder = { Text("Escribe habilidades separadas por coma…") },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = AccentYellow,
                        unfocusedBorderColor = AccentYellow,
                        cursorColor = AccentYellow
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { }) {
                    FilterIcon(size = 28.dp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(12.dp))


            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { vm.search("any") },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                    modifier = Modifier.height(46.dp).width(140.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) { Text("Apply", color = Color.White, fontSize = 16.sp) }

                Spacer(Modifier.width(8.dp))


                OutlinedButton(
                    onClick = { vm.loadAll() },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(46.dp).width(120.dp)
                ) { Text("View All") }


            }

            if (state.isLoading) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            

            if (state.termProgress.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.termProgress.forEach { progress ->
                        TermProgressRow(progress = progress)
                    }
                }
            }
            
            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            if (state.results.isNotEmpty()) {
                Text(
                    "Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentYellow,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.results) { user -> ResultUserRow(u = user, onClick = { onStudentClick(user.id)}) }
                }
            }
            Spacer(Modifier.height(150.dp))
            BottomBarCustom(
                onHome = onHome,
                onSearch = onSearch,
                onProfile = onProfile,
                onFav = onFav
            )
        }
    }
}



@Composable
private fun ResultUserRow(u: User, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = u.photoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(u.displayName, fontWeight = FontWeight.SemiBold)
            if (u.email.isNotEmpty())
                Text(u.email, style = MaterialTheme.typography.bodySmall)
            if (u.skills.isNotEmpty())
                Text(u.skills.take(6).joinToString(" · "), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RecentItemRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ClockIcon(size = 22.dp, color = Color.Gray, stroke = 2f)
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDE7F6)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 16.sp, color = Color.DarkGray)
    }
}

@Composable
private fun ClockIcon(size: Dp, color: Color, stroke: Float = 2f) {
    val density = LocalDensity.current
    val strokePx = with(density) { stroke.dp.toPx() }
    Canvas(modifier = Modifier.size(size)) {
        val radius = size.toPx() / 2f
        val cx = size.toPx() / 2f
        val cy = size.toPx() / 2f
        drawCircle(
            color = color,
            radius = radius - strokePx / 2f,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
        drawLine(
            color,
            start = androidx.compose.ui.geometry.Offset(cx, cy),
            end = androidx.compose.ui.geometry.Offset(cx + radius * 0.35f, cy),
            strokeWidth = strokePx, cap = StrokeCap.Round
        )
        drawLine(
            color,
            start = androidx.compose.ui.geometry.Offset(cx, cy),
            end = androidx.compose.ui.geometry.Offset(cx, cy - radius * 0.5f),
            strokeWidth = strokePx, cap = StrokeCap.Round
        )
    }
}

@Composable
private fun FilterIcon(
    size: Dp = 24.dp,
    color: Color = Color.Gray,
    strokeWidth: Float = 4f,
    knobRadius: Float = 6f
) {
    val density = LocalDensity.current
    val stroke = with(density) { strokeWidth.dp.toPx() }
    val knob = with(density) { knobRadius.dp.toPx() }
    Canvas(modifier = Modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()
        val rowH = h / 3f

        fun line(y: Float, xKnob: Float) {
            drawLine(
                color,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(w, y),
                strokeWidth = stroke, cap = StrokeCap.Round
            )
            drawCircle(color, radius = knob, center = androidx.compose.ui.geometry.Offset(xKnob, y))
            drawCircle(Color.White, radius = knob / 2f, center = androidx.compose.ui.geometry.Offset(xKnob, y))
        }

        line(y = rowH * 0.5f, xKnob = w * 0.7f)
        line(y = rowH * 1.5f, xKnob = w * 0.3f)
        line(y = rowH * 2.5f, xKnob = w * 0.5f)
    }
}



@Composable
private fun TermProgressRow(
    progress: com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status icon
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (progress.status) {
                com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress.Status.PENDING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = Color.Gray
                    )
                }
                com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress.Status.SEARCHING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = AccentYellow
                    )
                }
                com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress.Status.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        modifier = Modifier.size(16.dp),
                        tint = TitleGreen
                    )
                }
                com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress.Status.FAILED -> {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Failed",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        

        Text(
            text = "\"${progress.term}\"",
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
        

        Text(
            text = when (progress.status) {
                com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress.Status.COMPLETED -> 
                    "${progress.resultsCount} results"
                com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress.Status.SEARCHING -> 
                    "Searching..."
                com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress.Status.FAILED -> 
                    "Error"
                else -> ""
            },
            fontSize = 11.sp,
            color = when (progress.status) {
                com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress.Status.COMPLETED -> TitleGreen
                com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository.TermSearchProgress.Status.FAILED -> MaterialTheme.colorScheme.error
                else -> Color.DarkGray
            }
        )
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
