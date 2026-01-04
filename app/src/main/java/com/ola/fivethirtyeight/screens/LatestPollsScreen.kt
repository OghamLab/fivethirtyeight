package com.ola.fivethirtyeight.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ola.fivethirtyeight.include.TabSimpleLightTopAppBar
import com.ola.fivethirtyeight.routes.openLatestPoll


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LatestPollsScreen(navController: NavController) {

    val itemsList = listOf(
        "Marist" to "https://maristpoll.marist.edu/latest-polls/",
        "Ipsos" to "https://www.ipsos.com/en-us/latest-us-opinion-polls",
        "Pew Research" to "https://www.pewresearch.org/",
        "Trafalgar" to "https://www.thetrafalgargroup.org/",
        "Quinnipiac" to "https://poll.qu.edu/poll-results/",
        "Rasmussen" to "https://www.rasmussenreports.com/",
        "Emerson" to "https://emersoncollegepolling.com/blog/",
        "Monmouth" to "https://www.monmouth.edu/polling-institute/reports/",
        "You Gov" to "https://today.yougov.com/"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiary), // remove tint
        containerColor = MaterialTheme.colorScheme.tertiary, // remove scaffold tint
        topBar = {
            TabSimpleLightTopAppBar(title = "Latest Polls")


        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(itemsList) { (label, link) ->

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable {
                            navController.openLatestPoll(link)
                        },
                    color = Color(0xFF3B0F04),
                    shadowElevation = 6.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


/*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LatestPollsScreen(navController: NavController) {

    val itemsList = listOf(
        "Marist" to "https://maristpoll.marist.edu/latest-polls/",
        "Ipsos" to  "https://www.ipsos.com/en-us/latest-us-opinion-polls",
        "Pew Research" to  "https://www.pewresearch.org/",
        "Trafalgar" to  "https://www.thetrafalgargroup.org/",
        "Quinnipiac" to   "https://poll.qu.edu/poll-results/",
        "Rasmussen" to "https://www.rasmussenreports.com/",
        "Emerson" to "https://emersoncollegepolling.com/blog/",
        "Monmouth" to  "https://www.monmouth.edu/polling-institute/reports/",
        "You Gov" to "https://today.yougov.com/",

        )

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopAppBar({ TabSimpleLightTopAppBar(title = "Latest Polls"
            ) })
        }
    ) { paddingValues ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            contentPadding = paddingValues
        ) {
            items(itemsList) { (label, link) ->

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 2.dp, end = 2.dp, bottom = 8.dp)
                        .height(140.dp)
                        .clickable {
                            navController.navigate(
                                Routes.Article.create(
                                title = label,
                                url = link
                            ))
                        },
                    color = Color(0xFF3B0F04), // 👈 very dark orange
                    shadowElevation = 10.dp,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold // 👈 semi-bold text
                            ),
                            color = Color.White, // 👈 white text for contrast
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}*/
