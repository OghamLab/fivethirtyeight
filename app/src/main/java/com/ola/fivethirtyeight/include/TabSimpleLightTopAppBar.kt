package com.ola.fivethirtyeight.include

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ola.fivethirtyeight.ui.theme.TimesNewRoman

@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Composable
fun TabSimpleLightTopAppBar(title: String, modifier: Modifier = Modifier ) {


    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = TimesNewRoman,
                    fontSize = 24.sp, // ✅ Font size explicitly set to 24sp
                    fontWeight = FontWeight.ExtraBold
                )

            )

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            scrolledContainerColor = MaterialTheme.colorScheme.tertiary,
        )



    )
}
