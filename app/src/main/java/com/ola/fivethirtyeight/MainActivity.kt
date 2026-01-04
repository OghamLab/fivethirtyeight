package com.ola.fivethirtyeight
import android.app.ComponentCaller
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ola.fivethirtyeight.graphs.RootNavigationGraph
import com.ola.fivethirtyeight.notification.ACTION_OPEN_ARTICLE
import com.ola.fivethirtyeight.notification.EXTRA_TITLE
import com.ola.fivethirtyeight.notification.EXTRA_URL
import com.ola.fivethirtyeight.permission.RequestNotificationPermission
import com.ola.fivethirtyeight.routes.Routes
import com.ola.fivethirtyeight.ui.theme.FiveThirtyEightTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            FiveThirtyEightTheme {

                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize()) {
                    RequestNotificationPermission()

                    RootNavigationGraph(
                        navController = navController
                    )
                }

                // ✅ Handle notification deep-link
                LaunchedEffect(Unit) {
                    navController.currentBackStackEntryFlow.first()
                    handleNotificationIntent(
                        intent = intent,
                        navController = navController
                    )

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        setIntent(intent,caller)

        // ✅ Handle notification taps when app is already running
        handleNotificationIntent(
            intent = intent,
            navController = null // will be resolved in composition
        )
    }

}


private fun handleNotificationIntent(
    intent: Intent?,
    navController: NavHostController?
) {
    if (intent?.action != ACTION_OPEN_ARTICLE) return

    val url = intent.getStringExtra(EXTRA_URL)
    val title = intent.getStringExtra(EXTRA_TITLE)

    if (url.isNullOrBlank() || title.isNullOrBlank()) return

    navController?.navigate(
        Routes.Article.create(title, url)
    ) {
        launchSingleTop = true
        restoreState = true
    }
}







/*
class MainActivity() : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        enableEdgeToEdge()
        setContent {
            FiveThirtyEightTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RequestNotificationPermission()
                   // RootNavigationGraph()
                }
            }
            val navController = rememberNavController()
            // 1. Check for incoming “open article” intent once
            LaunchedEffect(Unit) {
                intent?.takeIf { it.action == ACTION_OPEN_ARTICLE }?.let {
                    val url = it.getStringExtra(com.ola.fivethirtyeight.notification.EXTRA_URL)
                    val title = it.getStringExtra(com.ola.fivethirtyeight.notification.EXTRA_TITLE)
                    if (!url.isNullOrBlank() && !title.isNullOrBlank()) {
                        navController.navigate(
                            DetailTopStoriesRouteScreen(
                                Uri.encode(title),
                                Uri.encode(url)
                            )
                        )
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        setIntent(intent,caller)
    }
}

*/





















