package com.example.talent_bridge_kt.presentation.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.data.firebase.FirebaseProfileRepository
import com.example.talent_bridge_kt.data.repository.ResumeRepositorySimple
import com.example.talent_bridge_kt.domain.model.Project
import com.example.talent_bridge_kt.domain.model.ResumeLanguage
import com.example.talent_bridge_kt.domain.usecase.GetProfileUseCase
import com.example.talent_bridge_kt.domain.usecase.UpdateProfileUseCase
import com.example.talent_bridge_kt.domain.usecase.UploadAvatarUseCase
import com.example.talent_bridge_kt.presentation.ui.viewmodel.ProfileUiState
import com.example.talent_bridge_kt.presentation.ui.viewmodel.ProfileViewModel
import com.example.talent_bridge_kt.ui.theme.AccentYellow
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.LinkGreen
import com.example.talent_bridge_kt.ui.theme.TitleGreen
import com.example.talent_bridge_kt.presentation.ui.components.OfflineConnectionDialog
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import kotlin.math.roundToInt
import android.util.Log

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    onEditNumber: () -> Unit = {},
    onAddCv: () -> Unit = {},
    onAddPortfolio: () -> Unit = {},
    onAddProject: () -> Unit = {},
    onBack: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    // bottom bar navigation
    onHome: () -> Unit = {},
    onSearch: () -> Unit = {},
    onExploreStudents: () -> Unit = {},
    onFav: () -> Unit = {}
) {
    // --------- estado de edición y campos básicos ---------
    var email by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }
    var number by remember { mutableStateOf<String?>(null) }
    var bio by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // entrada para nuevo tag
    var newTag by remember { mutableStateOf("") }

    // dialog para nuevo proyecto
    var showProjectDialog by remember { mutableStateOf(false) }
    var pTitle by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    var pSkills by remember { mutableStateOf("") }

    // --------- VM de perfil (eventual connectivity) ---------
    val context = LocalContext.current
    val eventualRepo = remember { 
        com.example.talent_bridge_kt.data.repository.EventualConnectivityProfileRepository(context)
    }
    val vm = remember {
        ProfileViewModel(
            getProfile = GetProfileUseCase(eventualRepo),
            updateProfile = UpdateProfileUseCase(eventualRepo),
            uploadAvatar = UploadAvatarUseCase(eventualRepo),
            eventualRepo = eventualRepo
        )
    }
    val uiState by vm.uiState.collectAsState()
    

    val connectivityObserver = remember {
        com.example.talent_bridge_kt.core.conectivity.AndroidConnectivityObserver(context)
    }
    var isConnected by remember { mutableStateOf(false) }
    var showOfflineDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        connectivityObserver.isConnected.collect { connected ->
            val wasConnected = isConnected
            isConnected = connected
            
            // Solo mostrar el dialog cuando se pierde la conexión (transición de conectado a desconectado)
            if (wasConnected && !connected) {
                showOfflineDialog = true
            } else if (connected) {
                showOfflineDialog = false
            }
            
            if (connected) {
                vm.syncNow()
            }
        }
    }
    

    LaunchedEffect(Unit) { 
        vm.load() 
    }


    LaunchedEffect(isEditing) {
        if (!isEditing) {
            vm.refresh()
        }
    }


    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ProfileUiState.Ready -> {

                if (!isEditing) {
                    email = state.profile.email.ifEmpty { "" }
                    linkedin = state.profile.linkedin ?: ""
                    number = state.profile.phone
                    bio = state.profile.bio ?: ""
                }
            }
            is ProfileUiState.Loading -> {

            }
            is ProfileUiState.Error -> {
            }
        }
    }

    // --------- cámara ---------
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    fun createTempImageUri(): Uri {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("avatar_", ".jpg", imagesDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) cameraUri?.let { vm.onAvatarPicked(it) }
    }

    // --------- REPO de CV  ---------
    val offlineResumeRepo = remember {
        com.example.talent_bridge_kt.data.repository.OfflineFirstResumeRepository(
            context = context,
            storage = com.google.firebase.storage.FirebaseStorage.getInstance(),
            db = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance(),
            contentResolver = context.contentResolver
        )
    }

    // --------- Hoja (bottom sheet) de CV (multi + idioma por item) ---------
    var showCvSheet by remember { mutableStateOf(false) }
    val onAddCvInternal = remember<(Unit) -> Unit> { { showCvSheet = true } }

    // --------- Lista de CVs (sección visible en perfil) -------------
    data class ResumeDoc(
        val id: String,
        val fileName: String,
        val url: String,
        val language: String,
        val uploadedAt: com.google.firebase.Timestamp?,
        val storagePath: String? = null
    )

    var resumes by remember { mutableStateOf<List<ResumeDoc>>(emptyList()) }
    var editingResumeId by remember { mutableStateOf<String?>(null) }
    var editingFileName by remember { mutableStateOf("") }
    var editingLanguage by remember { mutableStateOf<ResumeLanguage?>(null) }
    

    val resumeScope = rememberCoroutineScope()


    DisposableEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        var reg: ListenerRegistration? = null
        if (uid != null) {
            reg = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("resumes")
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, _ ->
                    val list = snap?.documents?.map { d ->
                        ResumeDoc(
                            id = d.id,
                            fileName = d.getString("fileName") ?: "",
                            url = d.getString("url") ?: "",
                            language = d.getString("language") ?: "",
                            uploadedAt = d.getTimestamp("uploadedAt"),
                            storagePath = d.getString("storagePath")
                        )
                    } ?: emptyList()
                    resumes = list

                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        val resumesJson = org.json.JSONArray().apply {
                            list.forEach { resume ->
                                put(org.json.JSONObject().apply {
                                    put("id", resume.id)
                                    put("fileName", resume.fileName)
                                    put("url", resume.url)
                                    put("language", resume.language)
                                    put("uploadedAt", resume.uploadedAt?.toDate()?.time)
                                    resume.storagePath?.let { put("storagePath", it) }
                                })
                            }
                        }.toString()
                        offlineResumeRepo.editStore.saveLocalResumes(resumesJson)
                    }
                }
        }
        onDispose { reg?.remove() }
    }
    

    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val localJson = offlineResumeRepo.editStore.getLocalResumes()
            if (localJson != null && resumes.isEmpty()) {
                try {
                    val array = org.json.JSONArray(localJson)
                    val localResumes = (0 until array.length()).map { i ->
                        val obj = array.getJSONObject(i)
                        ResumeDoc(
                            id = obj.getString("id"),
                            fileName = obj.getString("fileName"),
                            url = obj.optString("url", ""),
                            language = obj.getString("language"),
                            uploadedAt = obj.optLong("uploadedAt", -1).takeIf { it > 0 }
                                ?.let { com.google.firebase.Timestamp(java.util.Date(it)) },
                            storagePath = obj.optString("storagePath").takeIf { obj.has("storagePath") }
                        )
                    }
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        resumes = localResumes
                    }
                } catch (e: Exception) {

                }
            }
        }
    }
    

    LaunchedEffect(isConnected) {
        if (isConnected) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                offlineResumeRepo.syncPendingEditsAwait()
                offlineResumeRepo.syncPendingDeletesAwait()
            }
        }
    }

    Surface(color = CreamBackground, modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            TopBarCustom(
                height = 64.dp,
                onBack = onBack,
                onMenu = { },
                onDrawer = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ------------------ Header ------------------
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val avatarModel: Any = when (val s = uiState) {
                            is ProfileUiState.Ready -> {
                                val url = s.profile.avatarUrl.orEmpty()
                                if (url.isNotBlank()) url else R.drawable.student1
                            }
                            else -> R.drawable.student1
                        }

                        AsyncImage(
                            model = avatarModel,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEDE7F6))
                                .clickable {
                                    val tmp = createTempImageUri()
                                    cameraUri = tmp
                                    takePicture.launch(tmp)
                                },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = when (val s = uiState) {
                                is ProfileUiState.Ready -> s.profile.name.ifBlank { "" }
                                else -> ""
                            },
                            fontSize = 18.sp,
                            color = TitleGreen,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isEditing = !isEditing }) {
                                Icon(
                                    if (isEditing) Icons.Filled.Close else Icons.Filled.Edit,
                                    contentDescription = null, tint = TitleGreen
                                )
                            }
                            if (isEditing) Text("Editing", color = TitleGreen, fontSize = 14.sp)
                        }
                    }
                }

                // ------------------ Contact ------------------
                item { SectionTitle("Contact") }
                item {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!isEditing) {
                            LabeledValue("Email:", email)
                            LabeledValue("LinkedIn:", linkedin)
                        } else {
                            OutlinedTextField(
                                value = email, onValueChange = { email = it },
                                label = { Text("Email") }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = linkedin, onValueChange = { linkedin = it },
                                label = { Text("LinkedIn (usuario o URL)") }, singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Number:", color = TitleGreen, fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            if (!isEditing) {
                                if (number.isNullOrBlank()) {
                                    Text(
                                        "Add number",
                                        color = LinkGreen,
                                        fontSize = 14.sp,
                                        modifier = Modifier.clickable { onEditNumber() })
                                } else {
                                    Text(number!!, fontSize = 14.sp, color = Color.DarkGray)
                                }
                            } else {
                                OutlinedTextField(
                                    value = number.orEmpty(), onValueChange = { number = it },
                                    label = { Text("Número") }, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // ------------------ Description ------------------
                item { SectionTitle("Description") }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!isEditing) {
                            Text(bio, fontSize = 14.sp, color = Color.DarkGray)
                        } else {
                            OutlinedTextField(
                                value = bio, onValueChange = { bio = it },
                                label = { Text("Descripción / Bio") },
                                modifier = Modifier.fillMaxWidth(), minLines = 3
                            )
                        }
                        Text("Previous experience:", fontSize = 14.sp, color = TitleGreen)
                        Bullet("Monitoria IP")
                    }
                }

                // ------------------ Career (tags) ------------------
                item {
                    val tags = (uiState as? ProfileUiState.Ready)?.profile?.tags ?: emptyList()
                    Column {
                        Text("Career", fontSize = 12.sp, color = Color.DarkGray)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tags.forEach { tag ->
                                if (!isEditing) {
                                    TagPill(tag)
                                } else {
                                    DeletableTagPill(tag) { vm.removeTag(tag) }
                                }
                            }
                        }
                        if (isEditing) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newTag, onValueChange = { newTag = it },
                                    label = { Text("Nuevo tag") }, singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = {
                                    val t = newTag.trim()
                                    if (t.isNotEmpty()) vm.addTag(t)
                                    newTag = ""
                                }) { Text("Add") }
                            }
                        }
                    }
                }

                // ------------------ Acciones CV/Portafolio ------------------
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AddBox(
                            title = "Add CV",
                            modifier = Modifier.weight(1f),
                            onClick = { onAddCvInternal(Unit) }
                        )
                        AddBox(
                            title = "Add Portafolio",
                            modifier = Modifier.weight(1f),
                            onClick = onAddPortfolio
                        )
                    }
                }

                // ------------------ My CVs (lista desde Firestore) ---------
                item { SectionTitle("My CVs") }
                item {
                    if (resumes.isEmpty()) {
                        Text("Aún no has subido CVs.", color = Color.DarkGray, fontSize = 13.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            resumes.forEach { cv ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                if (editingResumeId == cv.id) {
                                                    // Edit mode
                                                    OutlinedTextField(
                                                        value = editingFileName,
                                                        onValueChange = { editingFileName = it },
                                                        label = { Text("File Name") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    var showLangDropdown by remember { mutableStateOf(false) }
                                                    Box {
                                                        OutlinedButton(onClick = { showLangDropdown = true }) {
                                                            Text("Language: ${editingLanguage?.name ?: cv.language}")
                                                        }
                                                        DropdownMenu(
                                                            expanded = showLangDropdown,
                                                            onDismissRequest = { showLangDropdown = false }
                                                        ) {
                                                            ResumeLanguage.values().forEach { lang ->
                                                                DropdownMenuItem(
                                                                    text = { Text(lang.name) },
                                                                    onClick = {
                                                                        editingLanguage = lang
                                                                        showLangDropdown = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        OutlinedButton(onClick = {
                                                            editingResumeId = null
                                                            editingFileName = ""
                                                            editingLanguage = null
                                                        }) {
                                                            Text("Cancel")
                                                        }
                                                        Button(onClick = {
                                                            resumeScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                editingFileName.takeIf { it.isNotBlank() }?.let {
                                                                    offlineResumeRepo.updateFileName(cv.id, it)
                                                                }
                                                                editingLanguage?.let {
                                                                    offlineResumeRepo.updateLanguage(cv.id, it)
                                                                }
                                                                
                                                                // Reload from local storage to get updated data
                                                                val localJson = offlineResumeRepo.editStore.getLocalResumes()
                                                                if (localJson != null) {
                                                                    try {
                                                                        val array = org.json.JSONArray(localJson)
                                                                        val updatedResumes = (0 until array.length()).map { i ->
                                                                            val obj = array.getJSONObject(i)
                                                                            ResumeDoc(
                                                                                id = obj.getString("id"),
                                                                                fileName = obj.getString("fileName"),
                                                                                url = obj.optString("url", ""),
                                                                                language = obj.getString("language"),
                                                                                uploadedAt = obj.optLong("uploadedAt", -1).takeIf { it > 0 }
                                                                                    ?.let { com.google.firebase.Timestamp(java.util.Date(it)) },
                                                                                storagePath = obj.optString("storagePath").takeIf { obj.has("storagePath") }
                                                                            )
                                                                        }
                                                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                            resumes = updatedResumes
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        // Fallback: update local state directly
                                                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                            val updatedResumes = resumes.map { r ->
                                                                                if (r.id == cv.id) {
                                                                                    r.copy(
                                                                                        fileName = editingFileName.takeIf { it.isNotBlank() } ?: r.fileName,
                                                                                        language = editingLanguage?.name ?: r.language
                                                                                    )
                                                                                } else r
                                                                            }
                                                                            resumes = updatedResumes
                                                                        }
                                                                    }
                                                                } else {
                                                                    // Fallback: update local state directly
                                                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                        val updatedResumes = resumes.map { r ->
                                                                            if (r.id == cv.id) {
                                                                                r.copy(
                                                                                    fileName = editingFileName.takeIf { it.isNotBlank() } ?: r.fileName,
                                                                                    language = editingLanguage?.name ?: r.language
                                                                                )
                                                                            } else r
                                                                        }
                                                                        resumes = updatedResumes
                                                                    }
                                                                }
                                                                
                                                                withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                    editingResumeId = null
                                                                    editingFileName = ""
                                                                    editingLanguage = null
                                                                }
                                                            }
                                                        }) {
                                                            Text("Save")
                                                        }
                                                    }
                                                } else {
                                                    // Display mode
                                                    Text(cv.fileName, fontWeight = FontWeight.SemiBold, color = TitleGreen)
                                                    Spacer(Modifier.height(2.dp))
                                                    Text("Idioma: ${cv.language}", fontSize = 12.sp, color = Color.DarkGray)
                                                }
                                            }
                                            if (editingResumeId != cv.id) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    if (isEditing) {
                                                        IconButton(onClick = {
                                                            editingResumeId = cv.id
                                                            editingFileName = cv.fileName
                                                            editingLanguage = ResumeLanguage.values().find { it.name == cv.language }
                                                        }) {
                                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = TitleGreen)
                                                        }
                                                    }
                                                    // Trash icon - always visible for easy deletion
                                                    IconButton(onClick = {
                                                        resumeScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                            offlineResumeRepo.deleteResume(cv.id, cv.storagePath)
                                                            
                                                            // Reload from local storage to reflect deletion
                                                            val localJson = offlineResumeRepo.editStore.getLocalResumes()
                                                            if (localJson != null) {
                                                                try {
                                                                    val array = org.json.JSONArray(localJson)
                                                                    val updatedResumes = (0 until array.length()).map { i ->
                                                                        val obj = array.getJSONObject(i)
                                                                        ResumeDoc(
                                                                            id = obj.getString("id"),
                                                                            fileName = obj.getString("fileName"),
                                                                            url = obj.optString("url", ""),
                                                                            language = obj.getString("language"),
                                                                            uploadedAt = obj.optLong("uploadedAt", -1).takeIf { it > 0 }
                                                                                ?.let { com.google.firebase.Timestamp(java.util.Date(it)) },
                                                                            storagePath = obj.optString("storagePath").takeIf { obj.has("storagePath") }
                                                                        )
                                                                    }
                                                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                        resumes = updatedResumes
                                                                    }
                                                                } catch (e: Exception) {
                                                                    // Fallback: remove from local state directly
                                                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                        resumes = resumes.filter { it.id != cv.id }
                                                                    }
                                                                }
                                                            } else {
                                                                // Fallback: remove from local state directly
                                                                withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                    resumes = resumes.filter { it.id != cv.id }
                                                                }
                                                            }
                                                        }
                                                    }) {
                                                        Icon(
                                                            Icons.Filled.Delete,
                                                            contentDescription = "Delete CV",
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                    val ctx = LocalContext.current
                                                    TextButton(onClick = {
                                                        if (cv.url.isNotBlank()) {
                                                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(cv.url))
                                                            ctx.startActivity(i)
                                                        }
                                                    }) {
                                                        Icon(Icons.Filled.OpenInNew, contentDescription = null)
                                                        Spacer(Modifier.width(6.dp))
                                                        Text("Ver")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                // ------------------ My Projects ------------------
                item { SectionTitle("My Projects") }
                item {
                    val projects = (uiState as? ProfileUiState.Ready)?.profile?.projects ?: emptyList()
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (projects.isEmpty()) {
                            Text("No tienes proyectos activos.", fontSize = 13.sp, color = Color.DarkGray)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                projects.forEach { p ->
                                    ProjectCard(
                                        title = p.title,
                                        description = p.description,
                                        skills = p.skills,
                                        canDelete = isEditing,
                                        onDelete = { vm.removeProject(p.id) }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        if (isEditing) {
                            Button(onClick = { showProjectDialog = true }) { Text("Add proyecto") }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AddBox(
                            title = "Add Project",
                            modifier = Modifier.weight(1f),
                            onClick = onAddProject
                        )
                    }
                }

                // ------------------ Guardar/Cancelar ------------------
                if (isEditing) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    val cur = (uiState as? ProfileUiState.Ready)?.profile ?: return@Button
                                    vm.update(
                                        cur.copy(
                                            email = email,
                                            linkedin = linkedin.ifBlank { null },
                                            phone = number,
                                            bio = bio
                                        )
                                    )
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Done, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Save")
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(28.dp)) }
                item {
                    Column(Modifier.padding(24.dp)) {
                        Text("Saved Projects")
                        TextButton(onClick = onBack) { Text("Back") }
                    }
                }
            }

            BottomBarCustom(
                onHome = onHome,
                onSearch = onSearch,
                onMenu = onExploreStudents,
                onFav = onFav
            )
        }
    }

    // ====== Hoja/BottomSheet para subir múltiples CVs con idioma ======
    if (showCvSheet) {
        CvMultiUploadSheet(
            repo = offlineResumeRepo.baseRepo,
            onDismiss = { showCvSheet = false }
        )
    }

    // --------- Dialogo de conexión offline para Student Profile ---------
    if (showOfflineDialog) {
        OfflineConnectionDialog(
            onDismiss = { showOfflineDialog = false }
        )
    }
    
    // --------- Dialogo de proyectos (sin cambios) ---------
    if (showProjectDialog) {
        AlertDialog(
            onDismissRequest = { showProjectDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val skills = pSkills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (pTitle.isNotBlank() && pDesc.isNotBlank()) {
                        val newProject = Project(
                            id = java.util.UUID.randomUUID().toString(),
                            title = pTitle.trim(),
                            subtitle = null,
                            description = pDesc.trim(),
                            skills = skills,
                            imgUrl = null,
                            createdAt = com.google.firebase.Timestamp.now(),
                            createdById = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                        )
                        vm.addProject(newProject)
                        pTitle = ""; pDesc = ""; pSkills = ""; showProjectDialog = false
                    }
                }) { Text("Add") }
            }
            ,
            dismissButton = { TextButton(onClick = { showProjectDialog = false }) { Text("Cancel") } },
            title = { Text("Nuevo proyecto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pTitle, onValueChange = { pTitle = it }, label = { Text("Título") }, singleLine = true)
                    OutlinedTextField(value = pDesc, onValueChange = { pDesc = it }, label = { Text("Descripción") }, minLines = 3)
                    OutlinedTextField(value = pSkills, onValueChange = { pSkills = it }, label = { Text("Skills (coma separadas)") }, singleLine = true)
                }
            }
        )
    }
}


private data class CvItemUi(
    val uri: Uri,
    val language: ResumeLanguage = ResumeLanguage.ES,
    val progress: Int = 0,
    val done: Boolean = false,
    val error: String? = null,
    val sha256: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CvMultiUploadSheet(
    repo: ResumeRepositorySimple,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    var cvItems by remember { mutableStateOf<List<CvItemUi>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var globalError by remember { mutableStateOf<String?>(null) }
    var uploadedCount by remember { mutableStateOf(0) }


    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            try {
                val allowed = uris.filter { uri ->
                    try {
                        val mime = context.contentResolver.getType(uri).orEmpty()
                        mime == "application/pdf" ||
                                mime == "application/msword" ||
                                mime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    } catch (e: Exception) {
                        Log.e("CvUpload", "Error checking file type for URI: $uri", e)
                        false
                    }
                }
                val current = cvItems.map { it.uri }.toSet()
                val merged = cvItems + allowed.filterNot { it in current }.map { CvItemUi(uri = it) }
                cvItems = merged
                globalError = null
            } catch (e: Exception) {
                Log.e("CvUpload", "Error processing selected files", e)
                globalError = "Error al procesar archivos: ${e.message}"
            }
        }
    )
    fun launchPicker() {
        try {

            picker.launch("*/*")
        } catch (e: Exception) {
            Log.e("CvUpload", "Error launching file picker", e)
            globalError = "Error al abrir el selector de archivos: ${e.message}"
        }
    }
    
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Cargar Hojas de Vida",
                fontWeight = FontWeight.SemiBold,
                color = TitleGreen,
                fontSize = 18.sp
            )

            // Botones centrados y full width
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = { launchPicker() },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AttachFile, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Añadir archivos")
                }

                OutlinedButton(
                    onClick = {
                        cvItems = emptyList()
                        uploadedCount = 0
                        globalError = null
                    },
                    enabled = !isUploading && cvItems.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Limpiar lista")
                }


                Button(
                    onClick = {
                        if (cvItems.isEmpty() || isUploading) return@Button
                        isUploading = true
                        globalError = null
                        uploadedCount = 0

                        scope.launch(Main) {
                            try {
                               
                                val withHashes: List<CvItemUi> = supervisorScope {
                                    cvItems.mapIndexed { index, item ->
                                        async(IO) {
                                            try {
                                                val inputStream = context.contentResolver.openInputStream(item.uri)
                                                val hash = computeSha256(inputStream)
                                                index to item.copy(sha256 = hash)
                                            } catch (e: Exception) {
                                                index to item.copy(sha256 = null)
                                            }
                                        }
                                    }.awaitAll()
                                        .sortedBy { it.first }
                                        .map { it.second }
                                }
                                cvItems = withHashes

                             
                                val results = withContext(IO) {
                                    try {
                                        repo.uploadAll(
                                            items = withHashes.map { it.uri to it.language },
                                            onProgress = { prog ->
                                                scope.launch(Main) {
                                                    cvItems = cvItems.map { current ->
                                                        if (current.uri == prog.uri) {
                                                            current.copy(progress = prog.percent.coerceIn(0, 100))
                                                        } else current
                                                    }
                                                }
                                            }
                                        )
                                    } catch (e: Exception) {
                                        throw e
                                    }
                                }

                                uploadedCount = results.size
                                cvItems = cvItems.map { it.copy(progress = 100, done = true) }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                Log.e("CvUpload", "Error uploading CVs", e)
                                globalError = e.message ?: "Error desconocido: ${e.javaClass.simpleName}"
                            } finally {
                                isUploading = false
                            }
                        }
                    },
                    enabled = cvItems.isNotEmpty() && !isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isUploading) "Subiendo..." else "Subir todo (${cvItems.size})")
                }
            }

            if (cvItems.isEmpty()) {
                Text(
                    "Selecciona uno o varios (PDF/DOC/DOCX). Puedes tocar “Añadir archivos” varias veces para acumular.",
                    color = Color.DarkGray, fontSize = 13.sp
                )
            }

            if (globalError != null) {
                Text("Error: $globalError", color = MaterialTheme.colorScheme.error)
            }
            if (uploadedCount > 0) {
                Text("Listo: $uploadedCount CV(s) subido(s).", fontWeight = FontWeight.Medium, color = TitleGreen)
            }

            if (cvItems.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    items(cvItems.size) { idx ->
                        val it = cvItems[idx]
                        CvRow(
                            item = it,
                            onLangChange = { lang ->
                                cvItems = cvItems.mapIndexed { i, cur -> if (i == idx) cur.copy(language = lang) else cur }
                            }
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CvRow(
    item: CvItemUi,
    onLangChange: (ResumeLanguage) -> Unit
) {
    Card {
        Column(Modifier.padding(12.dp)) {
            Text(item.uri.lastPathSegment ?: item.uri.toString(), fontWeight = FontWeight.Medium)
            item.sha256?.let {
                Spacer(Modifier.height(4.dp))
                Text("SHA-256: ${it.take(16)}...", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            LangSelectorCv(selected = item.language, onChange = onLangChange)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = item.progress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (item.done) "Completado" else "Progreso: ${item.progress}%",
                fontSize = 12.sp
            )
            item.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun LangSelectorCv(
    selected: ResumeLanguage,
    onChange: (ResumeLanguage) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val options = listOf(ResumeLanguage.ES, ResumeLanguage.EN, ResumeLanguage.FR, ResumeLanguage.OTHER)
    Box {
        OutlinedButton(onClick = { open = true }) { Text("Idioma: ${selected.name}") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.name) },
                    onClick = { onChange(lang); open = false }
                )
            }
        }
    }
}

/* =================== HELPERS EXISTENTES (TOP-LEVEL) =================== */

@Composable
private fun TagPill(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, AccentYellow, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) { Text(text, fontSize = 12.sp, color = Color.DarkGray) }
}

@Composable
private fun DeletableTagPill(text: String, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, AccentYellow, RoundedCornerShape(16.dp))
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color.DarkGray)
        IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectCard(
    title: String,
    description: String,
    skills: List<String>,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.SemiBold, color = TitleGreen, fontSize = 16.sp, modifier = Modifier.weight(1f))
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "delete", tint = Color(0xFFB00020))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(description, color = Color.DarkGray, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                skills.forEach { TagPill(it) }
            }
        }
    }
}

@Composable
private fun TopBarCustom(height: Dp, onBack: () -> Unit, onMenu: () -> Unit, onDrawer: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(2.dp)
            .background(CreamBackground)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo), contentDescription = "Talent Bridge",
                modifier = Modifier.height(90.dp), contentScale = ContentScale.Fit
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TitleGreen) }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onMenu) { Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = TitleGreen) }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDrawer) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Open drawer", tint = TitleGreen)
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun BottomBarCustom(onHome: () -> Unit, onSearch: () -> Unit, onMenu: () -> Unit, onFav: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(2.dp)
            .background(CreamBackground)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHome)  { Icon(Icons.Filled.Home,  contentDescription = "Home",  tint = TitleGreen) }
            IconButton(onClick = onSearch){ Icon(Icons.Filled.Search,contentDescription = "Search",tint = TitleGreen) }
            IconButton(onClick = onMenu)  { Icon(Icons.Filled.Group,  contentDescription = "Explore Students",  tint = TitleGreen) }
            IconButton(onClick = onFav)   { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Fav", tint = TitleGreen) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, color = AccentYellow, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
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
private fun AddBox(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Gray, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, color = TitleGreen, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text("Add link", color = LinkGreen, fontSize = 12.sp, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable { onClick() })
    }
}



private fun computeSha256(input: InputStream?): String? {
    if (input == null) return null
    return input.use { stream ->
        val md = MessageDigest.getInstance("SHA-256")
        val buf = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = stream.read(buf)
            if (read <= 0) break
            md.update(buf, 0, read)
        }
        md.digest().joinToString("") { b -> "%02x".format(b) }
    }
}