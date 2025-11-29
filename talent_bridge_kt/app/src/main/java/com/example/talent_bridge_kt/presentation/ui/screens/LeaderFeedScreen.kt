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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.domain.model.StudentListItem
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.LinkGreen
import com.example.talent_bridge_kt.ui.theme.TitleGreen
import kotlinx.coroutines.sync.Mutex
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.example.talent_bridge_kt.data.AnalyticsManager
import com.example.talent_bridge_kt.presentation.ui.viewmodel.StudentsViewModel
import com.google.firebase.auth.FirebaseAuth

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
    onInitiativeProfile: () -> Unit = {},
    onStudentClick: (uid: String) -> Unit = {}
) {

    val context = LocalContext.current
    val vm: StudentsViewModel = viewModel(
        factory = studentsVmFactory(context.applicationContext as Application)
    )

    val students by vm.students.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var filterExpanded by remember { mutableStateOf(false) }
    var selectedSkills by remember { mutableStateOf<Set<String>>(emptySet()) }

    val allSkills = remember(students) {
        students
            .flatMap { it.skillsOrTopics }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    val filteredStudents = remember(students, selectedSkills) {
        if (selectedSkills.isEmpty()) {
            students
        } else {
            students.filter { item ->
                item.skillsOrTopics.any { it in selectedSkills }
            }
        }
    }


    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            TopBarCustom(height = 100.dp, onBack = onBack, onMenu = onOpenMenu, onDrawer = onOpenDrawer)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                OutlinedButton(
                    onClick = { filterExpanded = !filterExpanded },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = TitleGreen
                    )
                ) {
                    Text(
                        text = if (selectedSkills.isEmpty()) "Filter by skills" else "Skills (${selectedSkills.size})",
                        fontSize = 12.sp
                    )
                }

                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false }
                ) {
                    if (allSkills.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No skills available") },
                            onClick = { }
                        )
                    } else {
                        allSkills.forEach { skill ->
                            val checked = selectedSkills.contains(skill)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(skill)
                                    }
                                },
                                onClick = {
                                    selectedSkills = if (checked) {
                                        selectedSkills - skill
                                    } else {
                                        selectedSkills + skill
                                    }
                                }
                            )
                        }

                        if (selectedSkills.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Clear filters") },
                                onClick = { selectedSkills = emptySet() }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (loading) {
                    item { Text("Loading…", color = Color.Gray) }
                }
                if (error != null) {
                    item { Text(error ?: "", color = Color.Red) }
                }

                items(filteredStudents, key = { it.uid }) { s ->
                    StudentCard(
                        item = s,
                        onClick = { onStudentClick(s.uid) }
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
                onInitiativeProfile = onInitiativeProfile,
                onFav = onFav
            )
        }
    }
}

/* =================== Componentes de UI =================== */

@Composable
private fun StudentCard(
    item: StudentListItem,
    onClick: () -> Unit
) {
    val name = item.displayName.ifBlank { "Student" }
    val career = item.headline.orEmpty()
    val interest = item.bio?.ifBlank { "Interested in projects" } ?: "Interested in projects"
    val tags = if (item.skillsOrTopics.isEmpty()) emptyList() else item.skillsOrTopics.take(5)
    val context = androidx.compose.ui.platform.LocalContext.current
    val contactRepo = remember {
        com.example.talent_bridge_kt.data.contacts.ContactRequestRepository()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            .padding(12.dp)
            .clickable {
                AnalyticsManager.logStudentProfileClick(
                    studentId = item.uid,
                    studentName = item.displayName,
                    hasAvatar = !item.avatarUrl.isNullOrBlank(),
                    sourceScreen = "LeaderFeed"
                )
                onClick()
            }
    ) {
        // Header con avatar + nombre
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.avatarUrl ?: R.drawable.student1,
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


        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                onClick = onClick,
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
                onClick = {
                    val auth = FirebaseAuth.getInstance()
                    val currentUser = auth.currentUser

                    val fromUid = currentUser?.uid ?: "unknown"
                    val fromName = currentUser?.displayName ?: "Anonymous"
                    val fromEmail = currentUser?.email ?: "No email"

                    val toUid = item.uid
                    val toName = item.displayName
                    val toEmail = item.email

                    contactRepo.sendContactRequest(
                        fromUid = fromUid,
                        toUid = toUid,
                        fromName = fromName,
                        toName = toName,
                        fromEmail = fromEmail,
                        toEmail = toEmail
                    ) { success, error ->
                        android.widget.Toast.makeText(
                            context,
                            if (success) "Contact request sent" else "Error: $error",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                          },
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
    onInitiativeProfile: () -> Unit,
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
            IconButton(onClick = onInitiativeProfile)  { Icon(Icons.Filled.Person,  contentDescription = "Profile",  tint = TitleGreen) }
            IconButton(onClick = onFav)   { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Fav", tint = TitleGreen) }
        }
    }
}


