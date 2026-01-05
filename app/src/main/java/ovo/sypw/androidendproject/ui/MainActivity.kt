package ovo.sypw.androidendproject.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ovo.sypw.androidendproject.ui.navigation.AppNavigation
import ovo.sypw.androidendproject.ui.theme.AndroidEndProject_ComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AndroidEndProject_ComposeTheme {
                AppNavigation()
            }
        }
    }
}
