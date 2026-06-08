package com.example.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.Message
import com.example.data.Order
import com.example.data.OrderItem
import com.example.data.OrderWithDetails
import com.example.data.User
import com.example.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderListViewModel(private val repository: AppRepository, private val prefs: SharedPreferences) : ViewModel() {

    val allUsers: StateFlow<List<User>> = repository.getAllUsers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBuyers = repository.getBuyers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSellers = repository.getSellers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing

    init {
        viewModelScope.launch {
            val phone = prefs.getString("currentUserId", null)
            if (phone != null) {
                val cachedId = prefs.getString("currentId", "") ?: ""
                val cachedName = prefs.getString("currentName", "") ?: ""
                val cachedEmail = prefs.getString("currentEmail", "") ?: ""
                val cachedType = prefs.getString("currentType", "USER") ?: "USER"
                val cachedShop = prefs.getString("currentShop", "") ?: ""
                
                if (cachedId.isNotEmpty()) {
                    _currentUser.value = User(id = cachedId, name = cachedName, phoneNumber = phone, email = cachedEmail, type = cachedType, shopName = cachedShop)
                }

                val serverUser = repository.getUserByPhone(phone)
                if (serverUser != null) {
                    _currentUser.value = serverUser
                    setCurrentUser(serverUser)
                }
            }
        }
    }

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
        if (user != null) {
            prefs.edit()
                .putString("currentUserId", user.phoneNumber)
                .putString("currentId", user.id)
                .putString("currentName", user.name)
                .putString("currentEmail", user.email)
                .putString("currentType", user.type)
                .putString("currentShop", user.shopName)
                .apply()
        } else {
            prefs.edit().clear().apply()
        }
    }

    fun createUser(name: String, email: String = "", phone: String = "", pass: String = "", shopName: String = "", onResult: (Boolean, String) -> Unit) {
        val emailTrimmed = email.trim()
        val phoneTrimmed = phone.trim()
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        
        viewModelScope.launch {
            try {
                if (auth.currentUser == null) {
                    try { auth.signInAnonymously().await() } catch (e: Exception) {}
                }
                if (repository.getUserByPhone(phoneTrimmed) != null) {
                    onResult(false, "App mein account is mobile number ke saath already hai.")
                    return@launch
                }
                if (emailTrimmed.isNotBlank() && repository.getUserByEmail(emailTrimmed) != null) {
                    onResult(false, "App mein is email se account already hai.")
                    return@launch
                }
                
                try {
                    val authResult = auth.createUserWithEmailAndPassword(emailTrimmed, pass).await()
                    val userId = authResult.user?.uid ?: java.util.UUID.randomUUID().toString()
                    val user = repository.insertUserWithId(userId, name, emailTrimmed, phoneTrimmed, pass, shopName)
                    setCurrentUser(user)
                    onResult(true, "")
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseAuth", "Auth failed: ${e.message}")
                    val user = repository.insertUser(name, emailTrimmed, phoneTrimmed, pass, shopName)
                    setCurrentUser(user)
                    onResult(true, "")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Unknown error")
            }
        }
    }
    
    fun loginUser(phoneOrEmail: String, pass: String, onResult: (Boolean, String) -> Unit) {
        val identifier = phoneOrEmail.trim()
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        
        viewModelScope.launch {
            try {
                if (auth.currentUser == null) {
                    try { auth.signInAnonymously().await() } catch (e: Exception) {}
                }
                val user = if (identifier.contains("@")) {
                    repository.getUserByEmail(identifier)
                } else {
                    repository.getUserByPhone(identifier)
                }
                
                if (user == null) {
                    onResult(false, "User not found")
                } else if (user.password != pass) {
                    onResult(false, "Incorrect password")
                } else {
                    try {
                        auth.signInWithEmailAndPassword(user.email, pass).await()
                    } catch (e: Exception) {
                        try {
                            auth.createUserWithEmailAndPassword(user.email, pass).await()
                        } catch (e2: Exception) {
                            android.util.Log.e("FirebaseAuth", "Migration failed: ${e2.message}")
                        }
                    }
                    setCurrentUser(user)
                    onResult(true, "")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Authentication error")
            }
        }
    }

    fun resetPassword(phoneOrEmail: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        val identifier = phoneOrEmail.trim()
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        viewModelScope.launch {
            try {
                if (auth.currentUser == null) {
                    try { auth.signInAnonymously().await() } catch (e: Exception) {}
                }
                val user = if (identifier.contains("@")) {
                    repository.getUserByEmail(identifier)
                } else {
                    repository.getUserByPhone(identifier)
                }
                
                if (user == null) {
                    onResult(false, "User not found")
                } else {
                    try {
                        val success = repository.updatePassword(user.id, newPass)
                        if (success) {
                            try {
                                auth.signInWithEmailAndPassword(user.email, user.password).await()
                                auth.currentUser?.updatePassword(newPass)?.let { it.await() }
                            } catch (e: Exception) {}
                            onResult(true, "Password updated successfully")
                        } else {
                            onResult(false, "Failed to update password")
                        }
                    } catch (e: Exception) {
                        onResult(false, e.message ?: "Failed to update password")
                    }
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error updating password")
            }
        }
    }

    fun updateUserProfile(name: String, shopName: String, profileImageBase64: String?, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "User not authenticated")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateUserProfile(user.id, name.trim(), shopName.trim(), profileImageBase64)
                val updatedUser = user.copy(
                    name = name.trim(),
                    shopName = shopName.trim(),
                    profileImage = profileImageBase64 ?: user.profileImage
                )
                setCurrentUser(updatedUser)
                onResult(true, "Profile updated successfully")
            } catch (e: Exception) {
                onResult(false, "Failed to update profile: ${e.message}")
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentBuyerOrders: StateFlow<List<OrderWithDetails>> = _currentUser.flatMapLatest { user ->
        if (user == null) {
            MutableStateFlow(emptyList())
        } else {
            repository.getOrdersForBuyer(user.id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentSellerOrders: StateFlow<List<OrderWithDetails>> = _currentUser.flatMapLatest { user ->
        if (user == null) {
            MutableStateFlow(emptyList())
        } else {
            repository.getOrdersForSeller(user.id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getOrderDetails(orderId: String) = repository.getOrderWithDetails(orderId)

    fun parseOrderFromText(naturalLanguageText: String, onSuccess: (List<Pair<String, String>>) -> Unit, onError: (String) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isParsing.value = true
            try {
                // Parse order locally with intelligent regex that handles multiple separators
                // Split by newlines, commas, "and", "and then", bullet points, etc.
                val delimiters = arrayOf("\n", ",", " and then ", " and ", " And ", " & ")
                var textToProcess = naturalLanguageText
                
                // Replace bullets or dashes with newlines
                textToProcess = textToProcess.replace(Regex("""^[-\*•]\s*""", RegexOption.MULTILINE), "\n")
                
                // Split the text into parts using multiple delimiters
                for (delimiter in delimiters) {
                    textToProcess = textToProcess.replace(delimiter, "|||")
                }
                
                val items = textToProcess.split("|||")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.equals("i want", ignoreCase = true) && !it.equals("give me", ignoreCase = true) && !it.equals("please", ignoreCase = true) }
                    .map { itemStr ->
                        // Attempt to extract quantity and name handling scenarios like "5 items of X" or "X 5 items"
                        // Regex looks for a number (optionally with units) either at start or end of string
                        val units = "kg|g|gm|mg|l|lt|ltr|litre|litres|liter|liters|ml|pcs|pack|packet|packets|packs|boxes|box|pieces|piece|cm|meter|meters|m|inch|inches"
                        val startQtyMatch = Regex("""^(\d+(?:\.\d+)?\s*(?:$units)?)\s+(?:of\s+)?(.+)$""", RegexOption.IGNORE_CASE).find(itemStr)
                        val endQtyMatch = Regex("""^(.+?)(?:\s+of)?\s+((?:x|qty|quantity)\s*\d+(?:\.\d+)?|\d+(?:\.\d+)?\s*(?:$units))$""", RegexOption.IGNORE_CASE).find(itemStr)
                        
                        if (startQtyMatch != null) {
                            Pair(startQtyMatch.groupValues[2].trim(), startQtyMatch.groupValues[1].trim())
                        } else if (endQtyMatch != null) {
                            Pair(endQtyMatch.groupValues[1].trim(), endQtyMatch.groupValues[2].trim())
                        } else {
                            // If no quantity detected, we assume 1
                            Pair(itemStr, "1")
                        }
                    }

                if (items.isNotEmpty()) {
                    onSuccess(items)
                } else {
                    onError("Could not extract any items.")
                }
            } catch (e: Exception) {
                onError("Error parsing order: ${e.message}")
            } finally {
                _isParsing.value = false
            }
        }
    }

    fun createOrder(buyerId: String, sellerId: String, items: List<Pair<String, String>>) {
        viewModelScope.launch {
            repository.createOrder(buyerId, sellerId, items)
        }
    }

    fun updateOrderStatus(order: Order, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(order, status)
        }
    }

    fun toggleItemAvailability(item: OrderItem) {
        viewModelScope.launch {
            repository.toggleItemAvailability(item)
        }
    }

    fun updateItemPrice(item: OrderItem, price: Double?) {
        viewModelScope.launch {
            repository.updateItemPrice(item, price)
        }
    }

    val recentChatUserIds: kotlinx.coroutines.flow.Flow<List<String>> = _currentUser.flatMapLatest { user ->
        if (user == null) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            repository.getMyRecentChatUsers(user.id)
        }
    }

    val unreadChatUserIds: kotlinx.coroutines.flow.Flow<List<String>> = _currentUser.flatMapLatest { user ->
        if (user == null) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            repository.getUnreadChatUsers(user.id)
        }
    }

    fun markMessagesAsRead(senderId: String) {
        val currentUserId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.markMessagesAsRead(senderId, currentUserId)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getChatMessages(otherUserId: String): kotlinx.coroutines.flow.Flow<List<Message>> {
        return _currentUser.flatMapLatest { user ->
            if (user == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                repository.getMessages(user.id, otherUserId)
            }
        }
    }

    fun sendMessage(receiverId: String, content: String) {
        val senderId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.sendMessage(senderId, receiverId, content)
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        viewModelScope.launch {
            repository.editMessage(messageId, newContent)
        }
    }

    fun deleteMessageForMe(messageId: String, currentDeletedList: List<String>) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.deleteMessageForMe(messageId, userId, currentDeletedList)
        }
    }

    fun deleteMessageForEveryone(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessageForEveryone(messageId)
        }
    }

    fun clearChat(otherUserId: String) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.clearChat(userId, otherUserId)
        }
    }

    fun blockUser(blockUserId: String, currentBlockedList: List<String>) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.blockUser(userId, blockUserId, currentBlockedList)
        }
    }

    fun unblockUser(blockUserId: String, currentBlockedList: List<String>) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.unblockUser(userId, blockUserId, currentBlockedList)
        }
    }
}

class OrderListViewModelFactory(private val repository: AppRepository, private val prefs: SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderListViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
