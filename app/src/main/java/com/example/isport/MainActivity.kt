package com.example.isport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISportApp()
        }
    }
}

@Composable
fun ISportApp() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Text("Benvenuto su iSport")
    }
}
