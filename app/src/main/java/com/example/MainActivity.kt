package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.DukaanViewModel
import com.example.ui.MainAppNavigation
import com.example.ui.theme.DukaanHisabTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DukaanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DukaanHisabTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
