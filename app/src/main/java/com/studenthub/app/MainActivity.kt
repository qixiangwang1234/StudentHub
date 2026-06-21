package com.studenthub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.studenthub.app.data.local.SettingsDataStore
import com.studenthub.app.ui.navigation.AppNavGraph
import com.studenthub.app.ui.navigation.BottomNavBar
import com.studenthub.app.ui.theme.StudentHubTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsDataStore.settings.collectAsState(
                initial = com.studenthub.app.data.local.AppSettings()
            )
            StudentHubTheme(darkTheme = settings.darkMode) {
                StudentHubMainScreen()
            }
        }
    }
}

@Composable
fun StudentHubMainScreen() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
