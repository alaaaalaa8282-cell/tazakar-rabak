package com.alaaeltaweel.thikrallah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alaaeltaweel.thikrallah.presentation.screen.radio.RadioScreen

class RadioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RadioScreen(onBackClick = { finish() })
        }
    }
}

