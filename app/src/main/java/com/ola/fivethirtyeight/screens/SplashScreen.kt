package com.ola.fivethirtyeight.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ola.fivethirtyeight.R
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val globalAlpha = remember { Animatable(1f) }
    val alpha1 = remember { Animatable(0f) }
    val offsetY1 = remember { Animatable(100f) }
    val alpha2 = remember { Animatable(0f) }
    val offsetY2 = remember { Animatable(100f) }
    val taglineAlpha = remember { Animatable(0f) }
    val taglineOffset = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        alpha1.animateTo(1f, tween(900))
        offsetY1.animateTo(0f, tween(800))

        delay(200)
        alpha2.animateTo(1f, tween(900))
        offsetY2.animateTo(0f, tween(800))

        delay(400)
        taglineAlpha.animateTo(1f, tween(900))
        taglineOffset.animateTo(0f, tween(800))

        delay(1200)
        globalAlpha.animateTo(0f, tween(700))

        onFinished()
    }

    // Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiary)
            .graphicsLayer { alpha = globalAlpha.value } // whole-screen fade
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp) // keeps content in upper half
        ) {
            // 🦊 First logo
            Image(
                painter = painterResource(id = R.drawable.picsart),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .aspectRatio(1.5f)
                    .padding(bottom = 2.dp)

                    .graphicsLayer {
                        alpha = alpha1.value
                        translationY = offsetY1.value
                    }
            )

            // 📰 Second logo
            Image(
                painter = painterResource(id = R.drawable.fivessss),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(6f)
                    .padding(top = 2.dp)
                    .graphicsLayer {
                        alpha = alpha2.value
                        translationY = offsetY2.value
                    },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )

            // ✨ Tagline
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Created by Ogham Lab",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = taglineAlpha.value
                    translationY = taglineOffset.value
                }
            )
        }
    }
}
