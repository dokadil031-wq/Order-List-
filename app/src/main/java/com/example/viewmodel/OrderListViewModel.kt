package com.example.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        viewModelScope.launch {
            try {
                if (repository.getUserByPhone(phoneTrimmed) != null) {
                    onResult(false, "App mein account is mobile number ke saath already hai.")
                    return@launch
                }
                if (emailTrimmed.isNotBlank() && repository.getUserByEmail(emailTrimmed) != null) {
                    onResult(false, "App mein is email se account already hai.")
                    return@launch
                }
                val user = repository.insertUser(name, emailTrimmed, phoneTrimmed, pass, shopName)
                setCurrentUser(user)
                onResult(true, "")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Unknown error")
            }
        }
    }
    
    fun loginUser(phoneOrEmail: String, pass: String, onResult: (Boolean, String) -> Unit) {
        val identifier = phoneOrEmail.trim()
        viewModelScope.launch {
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
                setCurrentUser(user)
                onResult(true, "")
            }
        }
    }

    fun resetPassword(phoneOrEmail: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        val identifier = phoneOrEmail.trim()
        viewModelScope.launch {
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
                        onResult(true, "Password updated successfully")
                    } else {
                        onResult(false, "Failed to update password")
                    }
                } catch (e: Exception) {
                    onResult(false, e.message ?: "Failed to update password")
                }
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
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Extract the order items and quantities from this text: $naturalLanguageText. ONLY return a JSON Array of objects, each object with 'name' and 'quantity' string keys. Nothing else like ```json")))),
                    generationConfig = GenerationConfig(
                        temperature = 0.1f
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                val cleanText = responseText.replace("```json", "").replace("```", "").trim()
                
                // Ensure we only parse the array part
                val startIndex = cleanText.indexOf('[')
                val endIndex = cleanText.lastIndexOf(']')
                val arrayString = if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                    cleanText.substring(startIndex, endIndex + 1)
                } else {
                    cleanText
                }
                
                val jsonArray = Json.parseToJsonElement(arrayString).jsonArray
                val items = jsonArray.map { element ->
                    val obj = element.jsonObject
                    Pair(
                        obj["name"]?.jsonPrimitive?.content ?: "",
                        obj["quantity"]?.jsonPrimitive?.content ?: ""
                    )
                }.filter { it.first.isNotEmpty() }

                if (items.isNotEmpty()) {
                    onSuccess(items)
                } else {
                    onError("Could not extract any items.")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            } finally {
                _isParsing.value = false
            }
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
