package com.rdev.rrepodocs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rdev.rrepodocs.platform.registerAndroidActivity
import com.rdev.rrepodocs.platform.registerAndroidAppContext
import com.rdev.rrepodocs.platform.unregisterAndroidActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        registerAndroidAppContext(applicationContext)
        registerAndroidActivity(this)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        unregisterAndroidActivity(this)
        super.onDestroy()
    }
}
