package com.ola.fivethirtyeight.include

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ola.fivethirtyeight.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleLightTopAppBar() {
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(bottom = 6.dp),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.tertiary

        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.picsart),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .height(20.dp)
                        .align(Alignment.Top)

                )

                Spacer(modifier = Modifier.width(2.dp))

                Image(
                    painter = painterResource(id = R.drawable.fivessss),
                    contentDescription = "Brand Mark",
                    modifier = Modifier
                        .height(24.dp)
                        .align(Alignment.Top),

                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }
        }
    )
}
