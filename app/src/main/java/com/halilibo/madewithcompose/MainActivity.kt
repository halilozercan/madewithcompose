package com.halilibo.madewithcompose

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.halilibo.madewithcompose.pip.LocalPipState
import com.halilibo.madewithcompose.pip.composePip
import com.halilibo.madewithcompose.ui.theme.LocalNightMode
import com.halilibo.madewithcompose.ui.theme.MadeWithComposeTheme

class MainActivity : ComponentActivity() {

    private val composePipController = composePip()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MadeWithComposeTheme {
                ProvideWindowInsets {
                    composePipController.ProvidePip {
                        val navController = rememberNavController()
                        Scaffold(
                            topBar = {
                                TopBar(navController = navController)
                            }
                        ) {
                            Surface(modifier = Modifier.padding(it).navigationBarsPadding()) {
                                NavHost(navController = navController, startDestination = "home") {
                                    composable("home") {
                                        HomePage(navController)
                                    }
                                    DemoEntry.values().forEach { demo ->
                                        composable(demo.destination) {
                                            demo.composable()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        composePipController.onUserLeaveHint(this)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        composePipController.pictureInPictureModeChanged(isInPictureInPictureMode)
    }
}

@Composable
fun TopBar(navController: NavController) {
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRouteName = currentBackStackEntry?.destination?.route
    val currentRouteTitle = DemoEntry.values().firstOrNull { it.destination == currentRouteName }?.title

    val pipController = LocalPipState.current

    if (!pipController.isInPictureInPicture.value) {
        TopAppBar(
            title = {
                Text(text = currentRouteTitle ?: "Home")
            },
            actions = {
                val nightMode = LocalNightMode.current
                Icon(Icons.Default.LightMode, contentDescription = "")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = nightMode.isNight,
                    onCheckedChange = { isNight ->
                        if (isNight) nightMode.setNight() else nightMode.setDay()
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.DarkMode, contentDescription = "")
                Spacer(modifier = Modifier.width(16.dp))
            },
            modifier = Modifier.statusBarsPadding()
        )
    }
}
