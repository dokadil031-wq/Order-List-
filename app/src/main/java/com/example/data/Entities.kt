package com.example.data

data class User(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val type: String = "USER",
    val shopName: String = "",
    val blockedUserIds: List<String> = emptyList()
)

data class Order(
    val id: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val status: String = "", // "PENDING", "DONE", "CANCELLED"
    val timestamp: Long = System.currentTimeMillis()
)

data class OrderItem(
    val id: String = "",
    val orderId: String = "",
    val name: String = "",
    val quantity: String = "",
    val isAvailable: Boolean = true,
    val unitPrice: Double? = null
)

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDeletedForEveryone: Boolean = false,
    val deletedForUserIds: List<String> = emptyList(),
    val isEdited: Boolean = false,
    val isRead: Boolean = false
)

data class OrderWithDetails(
    val order: Order,
    val items: List<OrderItem>,
    val buyer: User,
    val seller: User
)
