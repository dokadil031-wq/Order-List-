package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.OrderListViewModel
import com.example.data.*

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

import android.Manifest
import android.content.Context
import android.provider.ContactsContract
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.ScrollableTabRow

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack

    @OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: OrderListViewModel,
    onLoginSuccess: (User) -> Unit
) {
    var isCreateAccount by remember { mutableStateOf(false) }
    var isForgotPassword by remember { mutableStateOf(false) }
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var fpEmailOrPhone by remember { mutableStateOf("") }
    var fpNewPassword by remember { mutableStateOf("") }
    var fpMessage by remember { mutableStateOf("") }
    var fpIsError by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            onLoginSuccess(user)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCreateAccount) "Create Account" else "Login", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(if (isCreateAccount) "Sign Up" else "Welcome Back", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isCreateAccount) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = false)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Shop Name (Optional for Buyers)") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = false)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, autoCorrectEnabled = false)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address or Mobile Number") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        }
                    )
                    
                    if (isCreateAccount) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                            trailingIcon = {
                                val image = if (confirmPasswordVisible)
                                    Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff
                                val description = if (confirmPasswordVisible) "Hide password" else "Show password"
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            }
                        )
                    }
                    
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            errorMessage = ""
                            if (isCreateAccount) {
                                if (name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()) {
                                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                        errorMessage = "Invalid email format"
                                    } else if (!android.util.Patterns.PHONE.matcher(phone).matches() || phone.length < 10) {
                                        errorMessage = "Invalid mobile number"
                                    } else if (password != confirmPassword) {
                                        errorMessage = "Passwords do not match"
                                    } else {
                                        isLoading = true
                                        viewModel.createUser(name = name, email = email, phone = phone, pass = password, shopName = shopName) { success, msg ->
                                            isLoading = false
                                            if (!success) {
                                                errorMessage = msg
                                            }
                                        }
                                    }
                                } else {
                                    errorMessage = "Please fill all required fields (Shop Name is optional for buyers)"
                                }
                            } else {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    isLoading = true
                                    viewModel.loginUser(email, password) { success, msg ->
                                        isLoading = false
                                        if (!success) {
                                            errorMessage = msg
                                        }
                                    }
                                } else {
                                    errorMessage = "Please enter email/mobile and password"
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(if (isCreateAccount) "Create Account" else "Login")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { 
                        isCreateAccount = !isCreateAccount 
                        errorMessage = ""
                    }) {
                        Text(if (isCreateAccount) "Already have an account? Login" else "Don't have an account? Create one")
                    }

                    if (!isCreateAccount) {
                        TextButton(onClick = { 
                            isForgotPassword = true 
                            fpMessage = ""
                            fpEmailOrPhone = ""
                            fpNewPassword = ""
                        }) {
                            Text("Forgot password?")
                        }
                    }
                }
            }
        }
    }

    if (isForgotPassword) {
        AlertDialog(
            onDismissRequest = { isForgotPassword = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = fpEmailOrPhone,
                        onValueChange = { fpEmailOrPhone = it },
                        label = { Text("Email or Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fpNewPassword,
                        onValueChange = { fpNewPassword = it },
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (fpMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = fpMessage,
                            color = if (fpIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fpEmailOrPhone.isNotBlank() && fpNewPassword.isNotBlank()) {
                            isLoading = true
                            viewModel.resetPassword(fpEmailOrPhone, fpNewPassword) { success, msg ->
                                isLoading = false
                                fpIsError = !success
                                fpMessage = msg
                            }
                        } else {
                            fpIsError = true
                            fpMessage = "Please enter both fields"
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Reset")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { isForgotPassword = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

enum class BottomTab { Dashboard, Messages, Profile }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: OrderListViewModel,
    onNavigateToGenerateOrder: () -> Unit,
    onNavigateToOrderDetails: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedBottomTab by remember { mutableStateOf(BottomTab.Dashboard) }
    var showGlobalSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when {
                            showGlobalSearch -> "Search Users"
                            selectedBottomTab == BottomTab.Dashboard -> "Dashboard"
                            selectedBottomTab == BottomTab.Messages -> "Messages"
                            selectedBottomTab == BottomTab.Profile -> "My Profile"
                            else -> "Dashboard"
                        },
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    if (selectedBottomTab == BottomTab.Dashboard && !showGlobalSearch) {
                        IconButton(onClick = { showGlobalSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search Users")
                        }
                        IconButton(onClick = { selectedBottomTab = BottomTab.Profile }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    }
                    if (showGlobalSearch) {
                        IconButton(onClick = { showGlobalSearch = false }) {
                            Text("Clear", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedBottomTab == BottomTab.Dashboard,
                    onClick = { 
                        selectedBottomTab = BottomTab.Dashboard 
                        showGlobalSearch = false
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == BottomTab.Messages,
                    onClick = { 
                        selectedBottomTab = BottomTab.Messages 
                        showGlobalSearch = false
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Messages") },
                    label = { Text("Messages") }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == BottomTab.Profile,
                    onClick = { 
                        selectedBottomTab = BottomTab.Profile 
                        showGlobalSearch = false
                    },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        },
        floatingActionButton = {
            if (selectedBottomTab == BottomTab.Dashboard) {
                FloatingActionButton(
                    onClick = onNavigateToGenerateOrder,
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_generate_order")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Generate Order")
                }
            }
        }
    ) { padding ->
        if (showGlobalSearch) {
            GlobalSearchContent(
                padding = padding,
                viewModel = viewModel,
                onNavigateToChat = onNavigateToChat
            )
        } else {
            when (selectedBottomTab) {
                BottomTab.Dashboard -> DashboardContent(
                    padding = padding,
                    viewModel = viewModel,
                    onNavigateToOrderDetails = onNavigateToOrderDetails,
                    onNavigateToChat = onNavigateToChat
                )
                BottomTab.Messages -> MessagesContent(
                    padding = padding,
                    viewModel = viewModel,
                    onNavigateToChat = onNavigateToChat
                )
                BottomTab.Profile -> ProfileContent(
                    padding = padding,
                    viewModel = viewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun GlobalSearchContent(
    padding: PaddingValues,
    viewModel: OrderListViewModel,
    onNavigateToChat: (String) -> Unit
) {
    var globalQuery by remember { mutableStateOf("") }
    var contactToInvite by remember { mutableStateOf<Pair<String, String>?>(null) }
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    var localContacts by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val contacts = mutableListOf<Pair<String, String>>()
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                    null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                cursor?.use {
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        val name = it.getString(nameIndex) ?: ""
                        val num = it.getString(numberIndex) ?: ""
                        val sb = StringBuilder()
                        for (i in 0 until num.length) {
                            val c = num[i]
                            if (c.isDigit() || c == '+') {
                                sb.append(c)
                            }
                        }
                        val normalized = sb.toString()
                        if (normalized.isNotBlank()) {
                            val last10 = if (normalized.length > 10) normalized.takeLast(10) else normalized
                            contacts.add(Pair(name, last10))
                        }
                    }
                }
                // Avoid duplicates by number
                localContacts = contacts.distinctBy { it.second }
            }
        }
    }

    val appUsersByPhone = remember(allUsers, currentUser) {
        val usersWithoutMe = allUsers.filter { it.id != currentUser?.id }
        val map = mutableMapOf<String, com.example.data.User>()
        for (u in usersWithoutMe) {
            val userPhone = u.phoneNumber ?: ""
            val sb = StringBuilder()
            for (i in 0 until userPhone.length) {
                val c = userPhone[i]
                if (c.isDigit() || c == '+') sb.append(c)
            }
            val normalized = sb.toString()
            if (normalized.isNotBlank()) {
                val last10 = if (normalized.length > 10) normalized.takeLast(10) else normalized
                map[last10] = u
            }
        }
        map
    }

    val matchedUsers = remember(appUsersByPhone, localContacts) {
        localContacts.mapNotNull { contact ->
            val matchingUser = appUsersByPhone[contact.second]
            if (matchingUser != null) Pair(contact.first, matchingUser) else null
        }
    }

    val unmatchedContacts = remember(appUsersByPhone, localContacts) {
        localContacts.filter { contact ->
            appUsersByPhone[contact.second] == null
        }
    }

    val filteredMatchedUsers = remember(matchedUsers, globalQuery) {
        val query = globalQuery.trim()
        if (query.isBlank()) matchedUsers else matchedUsers.filter {
            it.first.contains(query, ignoreCase = true) ||
            (it.second.name ?: "").contains(query, ignoreCase = true) ||
            (it.second.shopName ?: "").contains(query, ignoreCase = true) ||
            (it.second.phoneNumber ?: "").contains(query) ||
            (it.second.phoneNumber?.replace(Regex("[^0-9+]"), "") ?: "").contains(query)
        }
    }

    val filteredUnmatchedContacts = remember(unmatchedContacts, globalQuery) {
        val query = globalQuery.trim()
        if (query.isBlank()) unmatchedContacts else unmatchedContacts.filter {
            it.first.contains(query, ignoreCase = true) || it.second.contains(query, ignoreCase = true)
        }
    }

    val filteredOtherAppUsers = remember(allUsers, currentUser, matchedUsers, globalQuery) {
        val query = globalQuery.trim()
        val matchedUserIds = matchedUsers.map { it.second.id }.toSet()
        val otherUsers = allUsers.filter { it.id != currentUser?.id && !matchedUserIds.contains(it.id) }
        
        if (query.isBlank()) {
            emptyList()
        } else {
            otherUsers.filter {
                (it.name ?: "").contains(query, ignoreCase = true) ||
                (it.shopName ?: "").contains(query, ignoreCase = true) ||
                (it.phoneNumber ?: "").contains(query) ||
                (it.phoneNumber?.replace(Regex("[^0-9+]"), "") ?: "").contains(query)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        OutlinedTextField(
            value = globalQuery,
            onValueChange = { globalQuery = it },
            placeholder = { Text("Search your contacts on the app...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (globalQuery.isNotEmpty()) {
                    IconButton(onClick = { globalQuery = "" }) {
                        Text("X", fontWeight = FontWeight.Bold)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Contacts permission required to find users.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) {
                        Text("Grant Permission")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredMatchedUsers.isNotEmpty()) {
                    item {
                        Text("Contacts on App", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(filteredMatchedUsers) { (contactName, user) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToChat(user.id) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contactName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    val safeAppUser = user.name ?: ""
                                    Text("~ $safeAppUser", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    val safePhoneNumber = user.phoneNumber ?: ""
                                    if (safePhoneNumber.isNotBlank()) {
                                        Text(safePhoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                                IconButton(onClick = { onNavigateToChat(user.id ?: "") }) {
                                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Message", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                
                if (filteredOtherAppUsers.isNotEmpty()) {
                    item {
                        Text("Global App Search", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(filteredOtherAppUsers) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToChat(user.id) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    val safeShopName = user.shopName ?: ""
                                    if (safeShopName.isNotBlank()) {
                                        Text("Shop: $safeShopName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    val safePhoneNumber = user.phoneNumber ?: ""
                                    if (safePhoneNumber.isNotBlank()) {
                                        Text(safePhoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                                IconButton(onClick = { onNavigateToChat(user.id ?: "") }) {
                                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Message", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                if (filteredUnmatchedContacts.isNotEmpty()) {
                    item {
                        Text("Invite to App", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                    }
                    items(filteredUnmatchedContacts) { (contactName, phoneNumber) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contactName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(phoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                }
                                Button(
                                    onClick = { 
                                        contactToInvite = Pair(contactName, phoneNumber)
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Invite")
                                }
                            }
                        }
                    }
                }
                
                if (localContacts.isEmpty() && globalQuery.isBlank()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No contacts found on your device. Type a name or phone number in the search bar above to search registered app users globally.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }
        if (contactToInvite != null) {
            val contact = contactToInvite!!
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { contactToInvite = null },
                title = { Text("Invite to App", style = MaterialTheme.typography.titleLarge) },
                text = { Text("How would you like to invite ${contact.first} to join the app?", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        val waUri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=${contact.second}&text=" + android.net.Uri.encode("Hey ${contact.first}! Let's chat and manage orders on the Order List App. Download it now: https://example.com/download"))
                        val waIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, waUri)
                        try {
                            context.startActivity(waIntent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "WhatsApp not installed", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        contactToInvite = null
                    }) {
                        Text("WhatsApp", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        val inviteUri = android.net.Uri.parse("smsto:${contact.second}")
                        val smsIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO, inviteUri).apply {
                            putExtra("sms_body", "Hey ${contact.first}! Let's chat and manage orders on the Order List App. Download it now: https://example.com/download")
                        }
                        try {
                            context.startActivity(smsIntent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "No messaging app found", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        contactToInvite = null
                    }) {
                        Text("SMS", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardContent(
    padding: PaddingValues,
    viewModel: OrderListViewModel,
    onNavigateToOrderDetails: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val buyerOrders by viewModel.currentBuyerOrders.collectAsStateWithLifecycle()
    val sellerOrders by viewModel.currentSellerOrders.collectAsStateWithLifecycle()

    val sendOrders = remember(buyerOrders, searchQuery) {
        val base = buyerOrders.filter { it.order?.sellerId != "DOWNLOADED" && it.order?.sellerId != "CANCELLED" }
        if (searchQuery.isBlank()) base else base.filter {
            it.seller?.name.orEmpty().contains(searchQuery, ignoreCase = true) ||
            it.seller?.shopName.orEmpty().contains(searchQuery, ignoreCase = true) ||
            it.seller?.phoneNumber.orEmpty().contains(searchQuery, ignoreCase = true) ||
            it.seller?.email.orEmpty().contains(searchQuery, ignoreCase = true)
        }
    }

    val receiveOrders = remember(sellerOrders, searchQuery) {
        if (searchQuery.isBlank()) sellerOrders else sellerOrders.filter {
            it.buyer?.name.orEmpty().contains(searchQuery, ignoreCase = true) ||
            it.buyer?.shopName.orEmpty().contains(searchQuery, ignoreCase = true) ||
            it.buyer?.phoneNumber.orEmpty().contains(searchQuery, ignoreCase = true) ||
            it.buyer?.email.orEmpty().contains(searchQuery, ignoreCase = true)
        }
    }

    val downloadedOrders = remember(buyerOrders, searchQuery) {
        val base = buyerOrders.filter { it.order?.sellerId == "DOWNLOADED" }
        if (searchQuery.isBlank()) base else base.filter {
            it.items?.any { item -> item.name.contains(searchQuery, ignoreCase = true) } ?: false
        }
    }

    val cancelledOrders = remember(buyerOrders, searchQuery) {
        val base = buyerOrders.filter { it.order?.sellerId == "CANCELLED" }
        if (searchQuery.isBlank()) base else base.filter {
            it.items?.any { item -> item.name.contains(searchQuery, ignoreCase = true) } ?: false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Send") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Receive") }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("Download") }
            )
            Tab(
                selected = selectedTabIndex == 3,
                onClick = { selectedTabIndex = 3 },
                text = { Text("Cancel") }
            )
        }
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, shop, or phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Text("X", fontWeight = FontWeight.Bold)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    items(sendOrders) { orderWithDetails ->
                        val name = orderWithDetails.seller?.name
                        val displayPartyName = if (orderWithDetails.order?.sellerId == "EXTERNAL") "External Shared Document" else if (name.isNullOrBlank()) "Unknown Contact" else name
                        OrderCard(
                            orderWithDetails = orderWithDetails,
                            otherPartyName = displayPartyName,
                            onClick = { onNavigateToOrderDetails(orderWithDetails.order?.id ?: "") },
                            onMessageClick = { onNavigateToChat(orderWithDetails.seller?.id ?: "") },
                            showChatButton = orderWithDetails.order?.sellerId != "EXTERNAL"
                        )
                    }
                }
                1 -> {
                    items(receiveOrders) { orderWithDetails ->
                        val name = orderWithDetails.buyer?.name
                        val displayPartyName = if (name.isNullOrBlank()) "Unknown Contact" else name
                        OrderCard(
                            orderWithDetails = orderWithDetails,
                            otherPartyName = displayPartyName,
                            onClick = { onNavigateToOrderDetails(orderWithDetails.order?.id ?: "") },
                            onMessageClick = { onNavigateToChat(orderWithDetails.buyer?.id ?: "") },
                            showChatButton = true
                        )
                    }
                }
                2 -> {
                    items(downloadedOrders) { orderWithDetails ->
                        OrderCard(
                            orderWithDetails = orderWithDetails,
                            otherPartyName = "Downloaded Document",
                            onClick = { onNavigateToOrderDetails(orderWithDetails.order?.id ?: "") },
                            onMessageClick = {},
                            showChatButton = false
                        )
                    }
                }
                3 -> {
                    items(cancelledOrders) { orderWithDetails ->
                        OrderCard(
                            orderWithDetails = orderWithDetails,
                            otherPartyName = "Cancelled Document",
                            onClick = { onNavigateToOrderDetails(orderWithDetails.order?.id ?: "") },
                            onMessageClick = {},
                            showChatButton = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    orderWithDetails: OrderWithDetails,
    otherPartyName: String,
    onClick: () -> Unit,
    onMessageClick: () -> Unit,
    showChatButton: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(otherPartyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                StatusBadge(orderWithDetails.order?.status ?: "")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("${orderWithDetails.items?.size ?: 0} items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            
            if (showChatButton) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onMessageClick,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Message")
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "DONE" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "CANCELLED" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
    
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileContent(
    padding: PaddingValues,
    viewModel: OrderListViewModel,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Avatar",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = currentUser?.name ?: "Unknown",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        val phone = currentUser?.phoneNumber ?: ""
        if (phone.isNotEmpty()) {
            Text(
                text = "Phone: $phone",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        val shop = currentUser?.shopName ?: ""
        if (shop.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Shop: $shop",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
}
