package com.ola.fivethirtyeight.detail

import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebSettingsCompat.setForceDark


@SuppressWarnings("Deprecation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatestPollsDetailScreen(encodedUrl: String, navController: NavController) {
    val context = LocalContext.current


    val decodedUrl = Uri.decode(encodedUrl)
    val loadingState = remember { mutableStateOf(true) }
    val canGoBack = remember { mutableStateOf(false) }

    val errorMessage = remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    val titleState = remember { mutableStateOf("Loading...") }

    BackHandler(enabled = canGoBack.value) {
        webViewRef.value?.goBack()
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleState.value,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
        },


                navigationIcon = {
                    IconButton(onClick = {
                        if (canGoBack.value) {
                            webViewRef.value?.goBack()
                        } else {
                            navController.popBackStack()
                        }
}){
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                                 },


                actions = {
                    IconButton(onClick = {
                        webViewRef.value?.reload()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }

            )


        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    )

    { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (loadingState.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        setForceDark(settings, FORCE_DARK_ON)

                        webViewClient = object : WebViewClient() {



                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                loadingState.value = true
                                canGoBack.value = view?.canGoBack() ?: false
                            }



                            override fun onPageFinished(view: WebView?, url: String?) {
                                loadingState.value = false
                                canGoBack.value = view?.canGoBack() ?: false
                                titleState.value =title ?: decodedUrl

                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                loadingState.value = false
                                // Optionally show error UI here

                            }
                        }
                        settings.javaScriptEnabled = true
                        loadUrl(decodedUrl)
                        webViewRef.value = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

        }


    }


}













