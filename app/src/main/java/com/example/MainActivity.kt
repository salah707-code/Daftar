package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.DaftarApp
import com.example.ui.viewmodel.DaftarViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DaftarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaftarApp(viewModel = viewModel)
        }
    }
}
