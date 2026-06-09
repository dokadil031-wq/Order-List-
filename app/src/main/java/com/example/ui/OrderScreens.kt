package com.example.ui

import androidx.activity.compose.BackHandler
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.OrderListViewModel
import com.example.data.*

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateOrderScreen(
    viewModel: OrderListViewModel,
    onNavigateBack: () -> Unit,
    onOrderCreated: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    var orderText by remember { mutableStateOf("") }
    val isParsing by viewModel.isParsing.collectAsStateWithLifecycle()
    var error by remember { mutableStateOf<String?>(null) }
    var parsedItems by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    var showUserSelection by remember { mutableStateOf(false) }
    val committedActions = remember { androidx.compose.runtime.mutableStateListOf<String>() }

    BackHandler {
        if (committedActions.isEmpty()) {
            if (parsedItems != null) {
                viewModel.createOrder(currentUser?.id ?: "", "CANCELLED", parsedItems!!)
            }
            committedActions.add("CANCELLED")
        }
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Order List", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        if (committedActions.isEmpty()) {
                            if (parsedItems != null) {
                                viewModel.createOrder(currentUser?.id ?: "", "CANCELLED", parsedItems!!)
                            }
                            committedActions.add("CANCELLED")
                        }
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .imePadding()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            if (parsedItems == null) {
                Text("Describe your order:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = orderText,
                    onValueChange = { orderText = it },
                    label = { Text("Type order (e.g. 10kg sugar, 2 boxes of soap)") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 300.dp),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    minLines = 5,
                    maxLines = 15
                )

                val safeError = error
                if (safeError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(safeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (orderText.isNotBlank()) {
                            error = null
                            viewModel.parseOrderFromText(
                                naturalLanguageText = orderText,
                                onSuccess = { items ->
                                    parsedItems = items
                                },
                                onError = { error = it }
                            )
                        } else {
                            error = "Please type your order first."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_button"),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isParsing
                ) {
                    if (isParsing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Generate Document")
                    }
                }
            } else {
                Text("Extracted Order Items:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(parsedItems!!) { (name, qty) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(name, fontWeight = FontWeight.Bold)
                                    Text("Qty: $qty", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { 
                                if (committedActions.isEmpty()) {
                                    if (parsedItems != null) {
                                        viewModel.createOrder(currentUser?.id ?: "", "CANCELLED", parsedItems!!)
                                    }
                                    committedActions.add("CANCELLED")
                                }
                                onNavigateBack() 
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (parsedItems != null) {
                                    val pdfUri = PdfHelper.generateOrderPdf(context, parsedItems!!, currentUser)
                                    if (pdfUri != null) {
                                        if ("DOWNLOADED" !in committedActions) {
                                            viewModel.createOrder(currentUser?.id ?: "", "DOWNLOADED", parsedItems!!)
                                            committedActions.add("DOWNLOADED")
                                        }
                                        PdfHelper.copyToDownloads(context, pdfUri)
                                    } else {
                                        android.widget.Toast.makeText(context, "Error generating PDF", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Download")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (parsedItems != null) {
                                    val pdfUri = PdfHelper.generateOrderPdf(context, parsedItems!!, currentUser)
                                    if (pdfUri != null) {
                                        if ("EXTERNAL" !in committedActions) {
                                            viewModel.createOrder(currentUser?.id ?: "", "EXTERNAL", parsedItems!!)
                                            committedActions.add("EXTERNAL")
                                        }
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, pdfUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        try {
                                            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Order Document"))
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Error sharing PDF", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        android.widget.Toast.makeText(context, "Error generating PDF", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                            Spacer(Modifier.width(4.dp))
                            Text("Share")
                        }
                        Button(
                            onClick = {
                                showUserSelection = true
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send in App")
                            Spacer(Modifier.width(4.dp))
                            Text("Send in App")
                        }
                    }
                }
            }
        }
        
        if (showUserSelection) {
            val recentChatUserIds by viewModel.recentChatUserIds.collectAsStateWithLifecycle(initialValue = emptyList())
            val usersToShow = remember(allUsers, currentUser, recentChatUserIds) {
                val others = allUsers
                val (recent, rest) = others.partition { recentChatUserIds.contains(it.id) }
                val sortedRecent = recent.sortedBy { recentChatUserIds.indexOf(it.id) }
                Pair(sortedRecent, rest)
            }
            
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showUserSelection = false },
                title = { Text("Select User to Send") },
                text = {
                    LazyColumn {
                        val (recentUsers, otherUsers) = usersToShow
                        if (recentUsers.isEmpty() && otherUsers.isEmpty()) {
                            item { Text("No other users available.", modifier = Modifier.padding(16.dp)) }
                        } else {
                            if (recentUsers.isNotEmpty()) {
                                item { Text("Recent Chats", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp)) }
                                items(recentUsers) { user ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                            showUserSelection = false
                                            if (user.id !in committedActions) {
                                                if (parsedItems != null) {
                                                    viewModel.createOrder(currentUser?.id ?: "", user.id, parsedItems!!)
                                                }
                                                committedActions.add(user.id)
                                            }
                                            val formattedOrder = parsedItems?.joinToString("\n") { "- ${it.first}: ${it.second}" } ?: ""
                                            val messageText = "Here is an order list:\n$formattedOrder"
                                            viewModel.sendMessage(user.id, messageText)
                                            onNavigateToChat(user.id)
                                        },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            val displayName = (user.name ?: "") + if (user.id == currentUser?.id) " (You)" else ""
                                            Text(displayName, fontWeight = FontWeight.Bold)
                                            if (!user.shopName.isNullOrBlank()) {
                                                Text(user.shopName ?: "", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                            
                            if (otherUsers.isNotEmpty()) {
                                item { Text("Other Contacts", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp)) }
                                items(otherUsers) { user ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                            showUserSelection = false
                                            if (user.id !in committedActions) {
                                                if (parsedItems != null) {
                                                    viewModel.createOrder(currentUser?.id ?: "", user.id, parsedItems!!)
                                                }
                                                committedActions.add(user.id)
                                            }
                                            val formattedOrder = parsedItems?.joinToString("\n") { "- ${it.first}: ${it.second}" } ?: ""
                                            val messageText = "Here is an order list:\n$formattedOrder"
                                            viewModel.sendMessage(user.id, messageText)
                                            onNavigateToChat(user.id)
                                        },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            val displayName = (user.name ?: "") + if (user.id == currentUser?.id) " (You)" else ""
                                            Text(displayName, fontWeight = FontWeight.Bold)
                                            if (!user.shopName.isNullOrBlank()) {
                                                Text(user.shopName ?: "", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showUserSelection = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun extractQuantityNumber(quantity: String): Double {
    val regex = Regex("([0-9]+(?:\\.[0-9]+)?)")
    val match = regex.find(quantity)
    return match?.value?.toDoubleOrNull() ?: 1.0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    viewModel: OrderListViewModel,
    orderId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val orderDetailsFlow = remember(orderId) { viewModel.getOrderDetails(orderId) }
    val orderWithDetails by orderDetailsFlow.collectAsStateWithLifecycle(initialValue = null)
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val orderTextForWhatsApp = "=== ORDER DOCUMENT ===\n" + 
                        "Order ID: #$orderId\n" + 
                        "Items:\n" + 
                        (orderWithDetails?.items?.joinToString("\n") { 
                            val total = (it.unitPrice ?: 0.0) * extractQuantityNumber(it.quantity)
                            val priceStr = if (total > 0) " (Total: $total)" else ""
                            "- ${it.name}: ${it.quantity}$priceStr" 
                        } ?: "") + 
                        "\n====================\nPlease confirm."
                    IconButton(onClick = {
                        val itemsList = orderWithDetails?.items?.map { Pair(it.name, it.quantity) } ?: emptyList()
                        val pdfUri = PdfHelper.generateOrderPdf(context, itemsList, orderWithDetails?.buyer)
                        if (pdfUri != null) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, pdfUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Order Document"))
                        } else {
                            android.widget.Toast.makeText(context, "Error generating PDF", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Order Document")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val safeOrder = orderWithDetails
        val safeUser = currentUser
        if (safeOrder != null && safeUser != null) {
            val isSellerInThisOrder = safeUser.id == safeOrder.seller?.id
            val otherUserId = if (isSellerInThisOrder) safeOrder.buyer?.id ?: "" else safeOrder.seller?.id ?: ""

            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Status", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                            StatusBadge(safeOrder.order?.status ?: "")
                        }
                        if (isSellerInThisOrder) {
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(
                                    onClick = { safeOrder.order?.let { viewModel.updateOrderStatus(it, "DONE") } },
                                    shape = MaterialTheme.shapes.medium
                                ) { Text("Mark Done") }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { safeOrder.order?.let { viewModel.updateOrderStatus(it, "CANCELLED") } },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = MaterialTheme.shapes.medium
                                ) { Text("Cancel") }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigateToChat(otherUserId) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Message " + if (isSellerInThisOrder) "Buyer" else "Wholesaler")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Items List", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(safeOrder.items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Qty: ${item.quantity}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                                    
                                    val qtyNum = extractQuantityNumber(item.quantity)
                                    
                                    if (isSellerInThisOrder) {
                                        var priceInput by remember(item.unitPrice) { mutableStateOf(item.unitPrice?.toString() ?: "") }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = priceInput,
                                            onValueChange = { 
                                                priceInput = it 
                                                viewModel.updateItemPrice(item, it.toDoubleOrNull()) 
                                            },
                                            label = { Text("Unit Price") },
                                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(0.8f)
                                        )
                                        if (item.unitPrice != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Total: ${(item.unitPrice * qtyNum).formatTo2Decimal()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    } else if (item.unitPrice != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Unit price: ${item.unitPrice} | Total: ${(item.unitPrice * qtyNum).formatTo2Decimal()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                if (isSellerInThisOrder) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                        Checkbox(
                                            checked = item.isAvailable,
                                            onCheckedChange = { viewModel.toggleItemAvailability(item) }
                                        )
                                    }
                                } else {
                                    val availColor = if (item.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    Text(if (item.isAvailable) "Available" else "Not Available", color = availColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                
                val grandTotal = safeOrder.items.sumOf { item ->
                    (item.unitPrice ?: 0.0) * extractQuantityNumber(item.quantity)
                }
                
                if (grandTotal > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("₹ ${grandTotal.formatTo2Decimal()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}

fun Double.formatTo2Decimal(): String {
    return String.format(java.util.Locale.US, "%.2f", this)
}
