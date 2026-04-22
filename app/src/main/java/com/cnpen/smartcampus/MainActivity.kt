package com.cnpen.smartcampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cnpen.smartcampus.ui.theme.SmartCampusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCampusTheme {
                SmartCampusApp()
            }
        }
    }
}
