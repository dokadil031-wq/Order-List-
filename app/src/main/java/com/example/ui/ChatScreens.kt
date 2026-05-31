package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.OrderListViewModel
import com.example.data.*

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun MessagesContent(
    padding: PaddingValues,
    viewModel: OrderListViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredSellers = remember(allUsers, searchQuery, currentUser) {
        val users = allUsers.filter { it.id != currentUser?.id }
        if (searchQuery.isBlank()) users
        else users.filter { 
            (it.name ?: "").contains(searchQuery, ignoreCase = true) || (it.shopName ?: "").contains(searchQuery, ignoreCase = true) 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .imePadding()
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by name or shop name") },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredSellers) { seller ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToChat(seller.id ?: "") },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(seller.name ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val safeShop = seller.shopName ?: ""
                            if (safeShop.isNotBlank()) {
                                Text(safeShop, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Button(
                            onClick = { onNavigateToChat(seller.id) },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Message")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: OrderListViewModel,
    otherUserId: String,
    onNavigateBack: () -> Unit
) {
    val messagesFlow = remember(otherUserId) { viewModel.getChatMessages(otherUserId) }
    val messages by messagesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val otherUser = allUsers.find { it.id == otherUserId }
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(otherUser?.name ?: "Chat", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        if (!otherUser?.shopName.isNullOrBlank()) {
                            Text(otherUser?.shopName ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Placeholder */ }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call")
                    }
                    IconButton(onClick = { /* Placeholder */ }) {
                        Icon(Icons.Default.VideoCall, contentDescription = "Video Call")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Placeholder */ }) {
                        Icon(Icons.Default.Photo, contentDescription = "Attach File/Photo")
                    }
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = MaterialTheme.shapes.large
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(otherUserId, messageText)
                                messageText = ""
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                val isMe = message.senderId == currentUser?.id
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    val bubbleColor = if (isMe) 
                        androidx.compose.ui.graphics.Color(0xFFE7FFDB) 
                    else 
                        androidx.compose.ui.graphics.Color(0xFFFFFFFF)
                        
                    Surface(
                        color = bubbleColor,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        ),
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            message.content,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = androidx.compose.ui.graphics.Color.Black,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
