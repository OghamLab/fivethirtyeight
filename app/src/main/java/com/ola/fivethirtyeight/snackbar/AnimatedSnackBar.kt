package com.ola.fivethirtyeight.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


@Composable
    fun AnimatedSnackBar(snackBarHostState: SnackbarHostState, modifier: Modifier) {
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.fillMaxWidth(),
            snackbar = { data ->
                var visible by remember { mutableStateOf(true) }

                LaunchedEffect(data) {
                    visible = true
                    delay(4000L) // Wait for display time (snackbar timeout default)
                    visible = false
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Snackbar(
                        snackbarData = data,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }
            }
        )
    }




