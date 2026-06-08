package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.painterResource
import com.example.viewmodel.OrderListViewModel
import kotlinx.coroutines.delay
import com.example.R

@Composable
fun SplashScreen(
    viewModel: OrderListViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(1500)
        if (viewModel.currentUser.value != null) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF081B3B)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_clipboard_logo),
            contentDescription = "App Logo",
            tint = Color(0xFFF8FAFC),
            modifier = Modifier.size(120.dp)
        )
    }
}
