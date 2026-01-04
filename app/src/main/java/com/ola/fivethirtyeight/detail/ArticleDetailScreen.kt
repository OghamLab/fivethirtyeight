package com.ola.fivethirtyeight.detail


import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.ola.fivethirtyeight.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    url: String,
    title: String,
    viewModel: SharedViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val decodedUrl = url

    val isSaved by viewModel.isArticleSaved(url).collectAsState(initial = false)

    var webView: WebView? by remember { mutableStateOf(null) }
    var isPageLoaded by remember { mutableStateOf(false) }
    var readerModeEnabled by rememberSaveable { mutableStateOf(false) }

    val readabilityJs = remember {
        context.assets.open("readability.js").bufferedReader().use { it.readText() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.toggleSave(url, !isSaved)
                    }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Saved" else "Save"
                        )
                    }
                    IconButton(onClick = { shareArticle(context, url) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { openInBrowser(context, url) }) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in Browser")
                    }
                    IconButton(onClick = {
                        readerModeEnabled = !readerModeEnabled
                        if (readerModeEnabled && isPageLoaded) {
                            injectReadability(webView, readabilityJs)
                        } else if (!readerModeEnabled) {
                            webView?.loadUrl(decodedUrl) // reload original page
                        }
                    }) {
                        Icon(
                            imageVector = if (readerModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Reader Mode"
                        )
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webView = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isPageLoaded = true
                            if (readerModeEnabled) {
                                injectReadability(this@apply, readabilityJs)
                            }
                        }
                    }
                    loadUrl(decodedUrl)
                }
            },
            update = {
                if (it.url != decodedUrl) {
                    it.loadUrl(decodedUrl)
                }
            }
        )
    }
}











/*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    viewModel: TopStoriesViewModel = hiltViewModel(), url: String, title: String,
    onBackClick: () -> Unit = {},


    ) {
    val context = LocalContext.current
    val decodedUrl = url


    val isSaved by viewModel.isArticleSaved(url).collectAsState(initial = false)


    // Keep a reference so we can manipulate WebView if needed later
   // var webView: WebView? by remember { mutableStateOf(null) }


    // Keep reference to WebView
    val webViewRef = remember { mutableStateOf<WebView?>(null) }



    var readerModeEnabled by rememberSaveable { mutableStateOf(false) }
    val readabilityJs = remember {
        context.assets.open("readability.js").bufferedReader().use { it.readText() }
    }

    var webView: WebView? by remember { mutableStateOf(null) }
    var isPageLoaded by remember { mutableStateOf(false) }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {

                        viewModel.toggleSave(url, !isSaved)

                    })
                    {
                        Icon(
                            if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Saved" else "Save"
                        )
                    }
                    IconButton(onClick = { shareArticle(context, url) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { openInBrowser(context, url) }) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in Browser")
                    }
                    IconButton(onClick = {
                        readerModeEnabled = !readerModeEnabled
                        if (readerModeEnabled && isPageLoaded) {
                            injectReadability(webView, readabilityJs)
                        } else if (!readerModeEnabled) {
                            webView?.loadUrl(decodedUrl) // reload original page
                        }

                    }) {
                        Icon(
                            if (readerModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Reader Mode"
                        )
                    }





                },





                )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webView = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isPageLoaded = true
                            if (readerModeEnabled) {
                                injectReadability(this@apply, readabilityJs)
                            }
                        }
                    }



                    loadUrl(decodedUrl)

                }
            },
            update = {
                // Optional: reload or perform actions if url changes
                if (it.url != decodedUrl) {
                    it.loadUrl(decodedUrl)
                }
            }
        )
    }
}

*/

fun shareArticle(context: Context, url: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share via"))
}

fun openInBrowser(context: Context, url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(browserIntent)
}




private fun injectReadability(webView: WebView?, readabilityJs: String) {
    webView?.evaluateJavascript(
        """
        (function() {
            $readabilityJs
            var documentClone = document.cloneNode(true);
            var article = new Readability(documentClone).parse();
            if(article && article.content) {
                document.body.innerHTML = 
                    '<div style="font-family:sans-serif;line-height:1.6;font-size:18px;padding:16px;color:#222;background:#fff">' 
                    + '<h1 style="font-size:22px;margin-bottom:0.5em;">' + article.title + '</h1>' 
                    + article.content 
                    + '</div>';
                    
                // Style images
                var imgs = document.querySelectorAll("img");
                imgs.forEach(function(img) {
                    img.style.display = "block";
                    img.style.margin = "16px auto";
                    img.style.maxWidth = "90%";
                    img.style.height = "auto";
                    img.style.borderRadius = "8px";
                });
            }
        })();
        """.trimIndent(),
        null
    )
}
