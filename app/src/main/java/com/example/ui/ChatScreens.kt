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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.OrderListViewModel
import com.example.data.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.rotate

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
    val recentChatUserIds by viewModel.recentChatUserIds.collectAsStateWithLifecycle(initialValue = emptyList())
    val unreadChatUserIds by viewModel.unreadChatUserIds.collectAsStateWithLifecycle(initialValue = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val focusManager = LocalFocusManager.current

    val usersToShow = remember(allUsers, searchQuery, currentUser, recentChatUserIds, selectedFilter, unreadChatUserIds) {
        val users = allUsers.filter { it.id != currentUser?.id && recentChatUserIds.contains(it.id) }
        
        val sourceUsers = if (selectedFilter == "Unread") {
            users.filter { unreadChatUserIds.contains(it.id) }
        } else {
            users
        }

        val filtered = if (searchQuery.isBlank()) sourceUsers
        else sourceUsers.filter { 
            (it.name ?: "").contains(searchQuery, ignoreCase = true) || (it.shopName ?: "").contains(searchQuery, ignoreCase = true) 
        }
        
        filtered.sortedBy { recentChatUserIds.indexOf(it.id) }
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
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "All",
                onClick = { selectedFilter = "All" },
                label = { Text("All") }
            )
            FilterChip(
                selected = selectedFilter == "Unread",
                onClick = { selectedFilter = "Unread" },
                label = { Text("Unread") }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val recentUsers = usersToShow
            if (recentUsers.isNotEmpty()) {
                item { Text(if (selectedFilter == "Unread") "Unread Chats" else "Recent Chats", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp)) }
                items(recentUsers) { seller ->
                    var showChatOptions by remember { mutableStateOf(false) }
                    val hasUnread = unreadChatUserIds.contains(seller.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onNavigateToChat(seller.id ?: "") },
                                    onLongPress = { showChatOptions = true }
                                )
                            },
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    val safeShop = seller.shopName ?: ""
                                    Text(
                                        text = if (safeShop.isNotBlank()) safeShop else (seller.name ?: ""),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (safeShop.isNotBlank() && seller.name != safeShop) {
                                        Text(seller.name ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                            if (hasUnread) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
                                )
                            } else {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Open Chat", modifier = Modifier.size(16.dp).rotate(180f), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        DropdownMenu(
                            expanded = showChatOptions,
                            onDismissRequest = { showChatOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear Chat") },
                                onClick = {
                                    viewModel.clearChat(seller.id)
                                    showChatOptions = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Block User") },
                                onClick = {
                                    val currentBlockedList = currentUser?.blockedUserIds ?: emptyList()
                                    if (currentBlockedList.contains(seller.id)) {
                                        viewModel.unblockUser(seller.id, currentBlockedList)
                                    } else {
                                        viewModel.blockUser(seller.id, currentBlockedList)
                                    }
                                    showChatOptions = false
                                }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            } else if (searchQuery.isNotBlank()) {
                 item {
                     Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                         Text("No conversations found matching your search. To chat with someone new, go to the Dashboard and tap the Search icon.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                     }
                 }
            } else {
                item {
                     Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                         Text("You haven't started any chats yet. To chat with someone new, go to the Dashboard and tap the Search icon.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showChatScreenOptions by remember { mutableStateOf(false) }
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(otherUserId) {
        viewModel.markMessagesAsRead(otherUserId)
    }

    if (showProfileDialog && otherUser != null) {
        UserProfileDialog(user = otherUser, onDismiss = { showProfileDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { showProfileDialog = true }
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            val displayName = if (!otherUser?.shopName.isNullOrBlank()) otherUser?.shopName else otherUser?.name
                            Text(displayName ?: "Chat", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            if (!otherUser?.shopName.isNullOrBlank() && otherUser.name != displayName) {
                                Text(otherUser.name ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { android.widget.Toast.makeText(context, "Initiating Video Call...", android.widget.Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.VideoCall, contentDescription = "Video Call")
                    }
                    IconButton(onClick = { android.widget.Toast.makeText(context, "Initiating Voice Call...", android.widget.Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call")
                    }
                    Box {
                        IconButton(onClick = { showChatScreenOptions = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showChatScreenOptions,
                            onDismissRequest = { showChatScreenOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear Chat") },
                                onClick = { 
                                    viewModel.clearChat(otherUserId)
                                    showChatScreenOptions = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Block") },
                                onClick = { 
                                    val currentBlockedList = currentUser?.blockedUserIds ?: emptyList()
                                    if (currentBlockedList.contains(otherUserId)) {
                                        viewModel.unblockUser(otherUserId, currentBlockedList)
                                    } else {
                                        viewModel.blockUser(otherUserId, currentBlockedList)
                                    }
                                    showChatScreenOptions = false 
                                    onNavigateBack()
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (showAttachmentMenu) {
                    AttachmentMenu(
                        onDismiss = { showAttachmentMenu = false },
                        onClick = { item ->
                            android.widget.Toast.makeText(context, "$item attached (Simulation)", android.widget.Toast.LENGTH_SHORT).show()
                            showAttachmentMenu = false
                        }
                    )
                }
                Surface(
                    color = androidx.compose.ui.graphics.Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .navigationBarsPadding()
                            .imePadding(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                                IconButton(onClick = { /* Placeholder */ }) {
                                    Icon(Icons.Default.InsertEmoticon, contentDescription = "Emoji", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    if (messageText.isEmpty()) {
                                        Text("Message", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp))
                                    }
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = messageText,
                                        onValueChange = { messageText = it },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 4.dp),
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                        maxLines = 4,
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                                    )
                                }
                                if (editingMessageId != null) {
                                    IconButton(onClick = { 
                                        messageText = ""
                                        editingMessageId = null
                                    }) {
                                        Text("X", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                } else {
                                    IconButton(onClick = { showAttachmentMenu = !showAttachmentMenu }) {
                                        Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (messageText.isBlank()) {
                                        IconButton(onClick = { android.widget.Toast.makeText(context, "Opening Camera...", android.widget.Toast.LENGTH_SHORT).show() }) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                        
                        FloatingActionButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    if (editingMessageId != null) {
                                        viewModel.editMessage(editingMessageId!!, messageText)
                                        editingMessageId = null
                                    } else {
                                        viewModel.sendMessage(otherUserId, messageText)
                                    }
                                    messageText = ""
                                } else {
                                    android.widget.Toast.makeText(context, "Recording Audio...", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp),
                            modifier = Modifier.size(48.dp),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ) {
                            if (messageText.isNotBlank()) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.padding(start = 4.dp))
                            } else {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Message")
                            }
                        }
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
                var showMessageOptions by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    val bubbleColor = if (isMe) 
                        androidx.compose.ui.graphics.Color(0xFFE7FFDB) 
                    else 
                        androidx.compose.ui.graphics.Color(0xFFFFFFFF)
                        
                    Box {
                        Surface(
                            color = bubbleColor,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp).pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { showMessageOptions = true }
                                )
                            }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                val contentText = if (message.isDeletedForEveryone) "This message was deleted" else message.content
                                Text(
                                    contentText,
                                    color = if (message.isDeletedForEveryone) androidx.compose.ui.graphics.Color.Gray else androidx.compose.ui.graphics.Color.Black,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontStyle = if (message.isDeletedForEveryone) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                )
                                if (message.isEdited && !message.isDeletedForEveryone) {
                                    Text("Edited", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.Gray, modifier = Modifier.align(Alignment.End))
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showMessageOptions,
                            onDismissRequest = { showMessageOptions = false }
                        ) {
                            if (isMe && !message.isDeletedForEveryone) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = { 
                                        messageText = message.content 
                                        editingMessageId = message.id
                                        showMessageOptions = false 
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete for me") },
                                onClick = { 
                                    viewModel.deleteMessageForMe(message.id, message.deletedForUserIds)
                                    showMessageOptions = false 
                                }
                            )
                            if (isMe && !message.isDeletedForEveryone) {
                                DropdownMenuItem(
                                    text = { Text("Delete for everyone") },
                                    onClick = { 
                                        viewModel.deleteMessageForEveryone(message.id)
                                        showMessageOptions = false 
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentMenu(onDismiss: () -> Unit, onClick: (String) -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    AttachmentIcon(icon = Icons.Default.InsertDriveFile, color = androidx.compose.ui.graphics.Color(0xFF5F66CD), text = "Document") { onClick("Document") }
                    AttachmentIcon(icon = Icons.Default.CameraAlt, color = androidx.compose.ui.graphics.Color(0xFFD3396D), text = "Camera") { onClick("Camera") }
                    AttachmentIcon(icon = Icons.Default.Photo, color = androidx.compose.ui.graphics.Color(0xFFAC44CF), text = "Gallery") { onClick("Gallery") }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    AttachmentIcon(icon = Icons.Default.Audiotrack, color = androidx.compose.ui.graphics.Color(0xFFE95922), text = "Audio") { onClick("Audio") }
                    AttachmentIcon(icon = Icons.Default.LocationOn, color = androidx.compose.ui.graphics.Color(0xFF13A660), text = "Location") { onClick("Location") }
                    AttachmentIcon(icon = Icons.Default.Person, color = androidx.compose.ui.graphics.Color(0xFF009DE1), text = "Contact") { onClick("Contact") }
                }
            }
        }
    }
}

@Composable
fun AttachmentIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color, text: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = text, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun UserProfileDialog(user: User, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val displayName = if (!user.shopName.isNullOrBlank()) user.shopName else user.name
                Text(
                    text = displayName ?: "User Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (!user.shopName.isNullOrBlank() && user.name != displayName) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Owner: ${user.name}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (!user.phoneNumber.isNullOrBlank()) {
                    Text(user.phoneNumber, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                if (!user.email.isNullOrBlank()) {
                    Text(user.email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { }) {
                        Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Audio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { }) {
                        Icon(Icons.Default.VideoCall, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Video", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
