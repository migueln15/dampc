package com.example.dampc
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dampc.presentation.navigation.AppNavGraph
import com.example.dampc.ui.theme.DampcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DampcTheme() {
                AppNavGraph()
            }
        }
    }
}

