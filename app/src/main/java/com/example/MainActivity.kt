package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.repository.AppRepository
import com.example.ui.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.OrderListViewModel
import com.example.viewmodel.OrderListViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val repository = AppRepository()
    
    setContent {
      val prefs = remember { getSharedPreferences("app_prefs", MODE_PRIVATE) }
      val viewModel: OrderListViewModel = viewModel(factory = OrderListViewModelFactory(repository, prefs))
      
      MyApplicationTheme {
        val navController = rememberNavController()
        
        AppNavigation(navController = navController, viewModel = viewModel)
      }
    }
  }
}
