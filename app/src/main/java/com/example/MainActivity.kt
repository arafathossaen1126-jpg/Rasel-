package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.AgroMainScreen
import com.example.ui.AgroViewModel
import com.example.ui.AgroViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Access lazily loaded database container from Application class
        val app = application as AgroApp
        val factory = AgroViewModelFactory(app.repository)
        val viewModel = ViewModelProvider(this, factory)[AgroViewModel::class.java]

        setContent {
            MyApplicationTheme(dynamicColor = false) {
                AgroMainScreen(viewModel = viewModel)
            }
        }
    }
}

