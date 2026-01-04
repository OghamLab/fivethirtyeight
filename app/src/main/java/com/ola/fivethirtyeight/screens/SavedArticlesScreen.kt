package com.ola.fivethirtyeight.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ola.fivethirtyeight.include.TabSimpleLightTopAppBar
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.utils.AnimatedFeedCardSaved
import com.ola.fivethirtyeight.viewmodel.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SavedArticlesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val savedArticles by viewModel.getSavedArticles().collectAsState(emptyList())
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TabSimpleLightTopAppBar(title = "Collections")
        }
    ) { padding ->
        if (savedArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved articles yet", color = Color.Gray)
            }
        } else {


            Surface(modifier = Modifier.fillMaxSize()) {


            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                items(savedArticles) { item ->
                   AnimatedFeedCardSaved(
                       item = item,
                       onClick = {
                           viewModel.selectFeedItem(it)
                           onArticleClick(it)
                       }
                    )
                }



            }


            }

       }
        }
    }






fun formatDate(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(raw)
        val formatter = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        formatter.format(date!!)
    } catch (e: Exception) {
        raw // fallback
    }
}


