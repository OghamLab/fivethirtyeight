package com.ola.fivethirtyeight.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ola.fivethirtyeight.viewmodel.SettingsViewModel

private val DarkColorScheme = darkColorScheme(
    primary = DeepRed80,
    secondary = SecondaryDark80,
    tertiary = Black80,
    primaryContainer = DeepRed80,
    onBackground = TertiaryWhite,
    onTertiary = TertiaryWhite




)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    secondary = SecondaryOrange,
    tertiary = TertiaryWhite,
    primaryContainer = PrimaryLightOrange,
    onBackground = PrimaryBlack,
    onTertiary = OnTertiaryGray







    /* Other default colors to override
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        */
)



@Composable
fun FiveThirtyEightTheme(
    viewModel: SettingsViewModel = hiltViewModel(),

    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val settings by viewModel.settingsState.collectAsState()
    val darkTheme = when (settings.themePref) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme() // System Default
    }




        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
