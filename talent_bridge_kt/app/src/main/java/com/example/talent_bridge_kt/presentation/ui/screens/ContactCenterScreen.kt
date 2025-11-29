package com.example.talent_bridge_kt.presentation.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import com.example.talent_bridge_kt.R
import com.example.talent_bridge_kt.domain.model.ContactRequest
import com.example.talent_bridge_kt.presentation.ui.viewmodel.ContactCenterViewModel
import com.example.talent_bridge_kt.ui.theme.CreamBackground
import com.example.talent_bridge_kt.ui.theme.TitleGreen

private fun contactCenterVmFactory(app: Application) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactCenterViewModel(app) as T
        }
    }

@Composable
fun ContactCenterScreen(
    onBack: () -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: ContactCenterViewModel = viewModel(
        factory = contactCenterVmFactory(context.applicationContext as Application)
    )

    val received by vm.received.collectAsState()
    val sent by vm.sent.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val info by vm.info.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            ContactCenterTopBar(
                height = 56.dp,
                onBack = onBack,
                onDrawer = onOpenDrawer
            )

            if (info != null) {
                InfoBanner(message = info!!)
            }

            if (error != null) {
                ErrorBanner(message = error!!)
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Received") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Sent") }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    selectedTab == 0 -> {
                        ContactRequestList(
                            items = received,
                            isReceived = true,
                            onReview = { vm.markReviewed(it) }
                        )
                    }

                    else -> {
                        ContactRequestList(
                            items = sent,
                            isReceived = false,
                            onReview = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRequestList(
    items: List<ContactRequest>,
    isReceived: Boolean,
    onReview: (ContactRequest) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No request registered", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { r ->
            ContactRequestCard(
                request = r,
                isReceived = isReceived,
                onReview = onReview
            )
        }
    }
}

@Composable
private fun ContactRequestCard(
    request: ContactRequest,
    isReceived: Boolean,
    onReview: (ContactRequest) -> Unit
) {
    val otherName = if (isReceived) request.fromName ?: request.fromUid else request.toName ?: request.toUid
    val otherEmail = if (isReceived) request.fromEmail ?: "" else request.toEmail ?: ""
    val statusText = if (request.reviewed) "Reviewed" else "Pending"

    val timeSince = remember(request.contactRequestTime) {
        prettySinceContact(request.contactRequestTime)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = timeSince,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = otherName,
            fontSize = 14.sp,
            color = TitleGreen,
            fontWeight = FontWeight.SemiBold
        )
        if (otherEmail.isNotBlank()) {
            Text(
                text = otherEmail,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Status: $statusText",
            fontSize = 12.sp,
            color = if (request.reviewed) Color(0xFF1B5E20) else Color(0xFFF9A825)
        )

        if (isReceived && !request.reviewed) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onReview(request) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TitleGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Review", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ContactCenterTopBar(
    height: Dp,
    onBack: () -> Unit,
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
            androidx.compose.foundation.Image(
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
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TitleGreen
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = TitleGreen
                )
            }
        }
    }
}

@Composable
private fun InfoBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE3F2FD))
            .padding(8.dp)
    ) {
        Text(
            text = message,
            color = Color(0xFF0D47A1),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEBEE))
            .padding(8.dp)
    ) {
        Text(
            text = message,
            color = Color(0xFFB71C1C),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private fun prettySinceContact(thenMs: Long): String {
    val diff = System.currentTimeMillis() - thenMs
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}


