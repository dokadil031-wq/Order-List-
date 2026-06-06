package com.example.repository

import com.example.data.Message
import com.example.data.Order
import com.example.data.OrderItem
import com.example.data.OrderWithDetails
import com.example.data.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.combine
import java.util.UUID

class AppRepository() {
    private val dbRef: DatabaseReference? by lazy {
        var dr: DatabaseReference? = null
        try {
            dr = FirebaseDatabase.getInstance().reference.child("order_list_app")
        } catch (e: Exception) {
            try {
                dr = FirebaseDatabase.getInstance("https://order-list-81886-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("order_list_app")
            } catch (e2: Exception) {
                try {
                    dr = FirebaseDatabase.getInstance("https://order-list-81886-default-rtdb.firebaseio.com").reference.child("order_list_app")
                } catch (e3: Exception) {
                    android.util.Log.e("AppRepository", "Failed to initialize Firebase db: ${e3.message}")
                }
            }
        }
        dr?.keepSynced(true)
        dr
    }
    
    // We will use dbRef where db was used
    private val db: DatabaseReference? get() = dbRef

    fun getAllUsers(): Flow<List<User>> = db?.child("users")?.asFlow()?.map { snapshot ->
        snapshot?.children?.mapNotNull { it.getValue(User::class.java) } ?: emptyList()
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getSellers(): Flow<List<User>> = db?.child("users")?.orderByChild("type")?.equalTo("SELLER")?.asFlow()?.map { snapshot ->
        snapshot?.children?.mapNotNull { it.getValue(User::class.java) } ?: emptyList()
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    
    fun getBuyers(): Flow<List<User>> = db?.child("users")?.orderByChild("type")?.equalTo("BUYER")?.asFlow()?.map { snapshot ->
        snapshot?.children?.mapNotNull { it.getValue(User::class.java) } ?: emptyList()
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertUser(name: String, email: String = "", phone: String = "", pass: String = "", shopName: String = ""): User {
        val id = db?.child("users")?.push()?.key ?: java.util.UUID.randomUUID().toString()
        val type = if (shopName.isNotBlank()) "SELLER" else "BUYER"
        val user = User(id = id, name = name, email = email, phoneNumber = phone, password = pass, type = type, shopName = shopName)
        try {
            kotlinx.coroutines.withTimeoutOrNull(8000) {
                db?.child("users")?.child(id)?.setValue(user)?.await()
            }
        } catch (e: Exception) {
            android.util.Log.e("AppRepository", "Error creating user: ${e.message}")
        }
        return user
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            var user: User? = null
            try {
                val snapshot = kotlinx.coroutines.withTimeoutOrNull(12000) {
                    db?.child("users")?.orderByChild("email")?.equalTo(email)?.get()?.await()
                }
                user = snapshot?.children?.firstOrNull()?.getValue(User::class.java)
            } catch (e: Exception) {
                android.util.Log.e("AppRepository", "Email index query failed: ${e.message}")
            }
            if (user == null) {
                // Fallback to fetching all and filtering manually
                val allUsersSnapshot = kotlinx.coroutines.withTimeoutOrNull(12000) {
                    db?.child("users")?.get()?.await()
                }
                user = allUsersSnapshot?.children?.mapNotNull { it.getValue(User::class.java) }?.find { it.email.equals(email, ignoreCase = true) }
            }
            user
        } catch (e: Exception) {
            android.util.Log.e("AppRepository", "Error getting user by email: ${e.message}")
            null
        }
    }

    suspend fun getUserByPhone(phone: String): User? {
        return try {
            var user: User? = null
            try {
                val snapshot = kotlinx.coroutines.withTimeoutOrNull(12000) {
                    db?.child("users")?.orderByChild("phoneNumber")?.equalTo(phone)?.get()?.await()
                }
                user = snapshot?.children?.firstOrNull()?.getValue(User::class.java)
            } catch (e: Exception) {
                android.util.Log.e("AppRepository", "Phone index query failed: ${e.message}")
            }
            if (user == null) {
                // Fallback to fetching all and filtering manually
                val allUsersSnapshot = kotlinx.coroutines.withTimeoutOrNull(12000) {
                    db?.child("users")?.get()?.await()
                }
                user = allUsersSnapshot?.children?.mapNotNull { it.getValue(User::class.java) }?.find { it.phoneNumber == phone }
            }
            user
        } catch (e: Exception) {
            android.util.Log.e("AppRepository", "Error getting user by phone: ${e.message}")
            null
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val snapshot = kotlinx.coroutines.withTimeoutOrNull(8000) {
                db?.child("users")?.child(userId)?.get()?.await()
            }
            snapshot?.getValue(User::class.java)
        } catch (e: Exception) {
            android.util.Log.e("AppRepository", "Error getting user by id: ${e.message}")
            null
        }
    }

    suspend fun createOrder(buyerId: String, sellerId: String, items: List<Pair<String, String>>) {
        val orderId = db?.child("orders")?.push()?.key ?: java.util.UUID.randomUUID().toString()
        val order = Order(id = orderId, buyerId = buyerId, sellerId = sellerId, status = "PENDING")
        db?.child("orders")?.child(orderId)?.setValue(order)
        
        for ((name, qty) in items) {
            val itemId = db?.child("order_items")?.push()?.key ?: java.util.UUID.randomUUID().toString()
            val orderItem = OrderItem(id = itemId, orderId = orderId, name = name, quantity = qty)
            db?.child("order_items")?.child(itemId)?.setValue(orderItem)
        }
    }

    suspend fun updatePassword(userId: String, newPass: String): Boolean {
        return try {
            kotlinx.coroutines.withTimeoutOrNull(8000) {
                db?.child("users")?.child(userId)?.child("password")?.setValue(newPass)?.await()
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("AppRepository", "Error updating password: ${e.message}")
            false
        }
    }

    suspend fun updateOrderStatus(order: Order, newStatus: String) {
        db?.child("orders")?.child(order.id)?.child("status")?.setValue(newStatus)
    }

    suspend fun toggleItemAvailability(item: OrderItem) {
        db?.child("order_items")?.child(item.id)?.child("isAvailable")?.setValue(!item.isAvailable)
    }

    suspend fun updateItemPrice(item: OrderItem, price: Double?) {
        db?.child("order_items")?.child(item.id)?.child("unitPrice")?.setValue(price)
    }

    private fun getOrdersFlow(queryItem: String, queryValue: String): Flow<List<OrderWithDetails>> {
        val ordersFlow = db?.child("orders")?.orderByChild(queryItem)?.equalTo(queryValue)?.asFlow()?.map { snapshot ->
            snapshot?.children?.mapNotNull { it.getValue(Order::class.java) } ?: emptyList()
        } ?: kotlinx.coroutines.flow.flowOf(emptyList())
        val itemsFlow = db?.child("order_items")?.asFlow()?.map { snapshot ->
            snapshot?.children?.mapNotNull { it.getValue(OrderItem::class.java) } ?: emptyList()
        } ?: kotlinx.coroutines.flow.flowOf(emptyList())
        val usersFlow = db?.child("users")?.asFlow()?.map { snapshot ->
            snapshot?.children?.mapNotNull { it.getValue(User::class.java) }?.associateBy { it.id } ?: emptyMap()
        } ?: kotlinx.coroutines.flow.flowOf(emptyMap())

        return combine(ordersFlow, itemsFlow, usersFlow) { orders, items, users ->
            orders.sortedByDescending { it.timestamp }.map { o ->
                OrderWithDetails(
                    order = o,
                    items = items.filter { it.orderId == o.id },
                    buyer = users[o.buyerId] ?: User(),
                    seller = users[o.sellerId] ?: User()
                )
            }
        }
    }

    fun getOrdersForBuyer(buyerId: String): Flow<List<OrderWithDetails>> = getOrdersFlow("buyerId", buyerId)
    fun getOrdersForSeller(sellerId: String): Flow<List<OrderWithDetails>> = getOrdersFlow("sellerId", sellerId)

    fun getOrderWithDetails(orderId: String): Flow<OrderWithDetails?> {
        val orderFlow = db?.child("orders")?.child(orderId)?.asFlow()?.map { it?.getValue(Order::class.java) } ?: kotlinx.coroutines.flow.flowOf(null)
        val itemsFlow = db?.child("order_items")?.orderByChild("orderId")?.equalTo(orderId)?.asFlow()?.map { snapshot ->
            snapshot?.children?.mapNotNull { it.getValue(OrderItem::class.java) } ?: emptyList()
        } ?: kotlinx.coroutines.flow.flowOf(emptyList())
        val usersFlow = db?.child("users")?.asFlow()?.map { snapshot ->
            snapshot?.children?.mapNotNull { it.getValue(User::class.java) }?.associateBy { it.id } ?: emptyMap()
        } ?: kotlinx.coroutines.flow.flowOf(emptyMap())
        
        return combine(orderFlow, itemsFlow, usersFlow) { o, items, users ->
            if (o == null) return@combine null
            OrderWithDetails(
                order = o,
                items = items,
                buyer = users[o.buyerId] ?: User(),
                seller = users[o.sellerId] ?: User()
            )
        }
    }

    suspend fun sendMessage(senderId: String, receiverId: String, content: String) {
        val messageId = db?.child("messages")?.push()?.key ?: java.util.UUID.randomUUID().toString()
        val message = Message(id = messageId, senderId = senderId, receiverId = receiverId, content = content)
        db?.child("messages")?.child(messageId)?.setValue(message)
    }

    suspend fun editMessage(messageId: String, newContent: String) {
        db?.child("messages")?.child(messageId)?.child("content")?.setValue(newContent)
        db?.child("messages")?.child(messageId)?.child("isEdited")?.setValue(true)
    }

    suspend fun deleteMessageForMe(messageId: String, currentUserId: String, currentDeletedList: List<String>) {
        val newList = currentDeletedList + currentUserId
        db?.child("messages")?.child(messageId)?.child("deletedForUserIds")?.setValue(newList)
    }

    suspend fun deleteMessageForEveryone(messageId: String) {
        db?.child("messages")?.child(messageId)?.child("isDeletedForEveryone")?.setValue(true)
        db?.child("messages")?.child(messageId)?.child("content")?.setValue("This message was deleted")
    }

    suspend fun clearChat(user1: String, user2: String) {
        val snapshot = db?.child("messages")?.get()?.await()
        snapshot?.children?.forEach { child ->
            val msg = child.getValue(Message::class.java)
            if (msg != null && ((msg.senderId == user1 && msg.receiverId == user2) || (msg.senderId == user2 && msg.receiverId == user1))) {
                val newList = msg.deletedForUserIds + user1
                child.ref.child("deletedForUserIds").setValue(newList)
            }
        }
    }

    suspend fun deleteChat(user1: String, user2: String) {
        clearChat(user1, user2)
    }

    suspend fun blockUser(currentUserId: String, blockUserId: String, currentBlockedList: List<String>) {
        val newList = currentBlockedList + blockUserId
        db?.child("users")?.child(currentUserId)?.child("blockedUserIds")?.setValue(newList)
    }

    suspend fun unblockUser(currentUserId: String, blockUserId: String, currentBlockedList: List<String>) {
        val newList = currentBlockedList - blockUserId
        db?.child("users")?.child(currentUserId)?.child("blockedUserIds")?.setValue(newList)
    }

    fun getMessages(user1: String, user2: String): Flow<List<Message>> = db?.child("messages")?.asFlow()?.map { snapshot ->
        snapshot?.children?.mapNotNull { it.getValue(Message::class.java) }
            ?.filter { (it.senderId == user1 && it.receiverId == user2) || (it.senderId == user2 && it.receiverId == user1) }
            ?.filter { !it.deletedForUserIds.contains(user1) }
            ?.sortedBy { it.timestamp } ?: emptyList()
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getMyRecentChatUsers(userId: String): Flow<List<String>> = db?.child("messages")?.asFlow()?.map { snapshot ->
        val userIds = mutableMapOf<String, Long>()
        snapshot?.children?.mapNotNull { it.getValue(Message::class.java) }?.forEach { message ->
            if (!message.deletedForUserIds.contains(userId)) {
                if (message.senderId == userId) {
                    val current = userIds[message.receiverId] ?: 0L
                    if (message.timestamp > current) userIds[message.receiverId] = message.timestamp
                } else if (message.receiverId == userId) {
                    val current = userIds[message.senderId] ?: 0L
                    if (message.timestamp > current) userIds[message.senderId] = message.timestamp
                }
            }
        }
        userIds.entries.sortedByDescending { it.value }.map { it.key }
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getUnreadChatUsers(userId: String): Flow<List<String>> = db?.child("messages")?.asFlow()?.map { snapshot ->
        val unreadSenders = mutableSetOf<String>()
        snapshot?.children?.mapNotNull { it.getValue(Message::class.java) }?.forEach { message ->
            if (!message.deletedForUserIds.contains(userId) && message.receiverId == userId && !message.isRead) {
                unreadSenders.add(message.senderId)
            }
        }
        unreadSenders.toList()
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun markMessagesAsRead(senderId: String, currentUserId: String) {
        val snapshot = db?.child("messages")?.get()?.await()
        snapshot?.children?.forEach { child ->
            val msg = child.getValue(Message::class.java)
            if (msg != null && msg.senderId == senderId && msg.receiverId == currentUserId && !msg.isRead) {
                child.ref.child("isRead").setValue(true)
            }
        }
    }
}
