package com.alaaeltaweel.thikrallah

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.alaaeltaweel.thikrallah.presentation.screen.radio.RadioScreen

class RadioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RadioScreen(onBackClick = { finish() })
        }
    }
}
