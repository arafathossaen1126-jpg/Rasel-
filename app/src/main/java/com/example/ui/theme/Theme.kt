package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NaturalGreen,
    secondary = NaturalHighlight,
    tertiary = NaturalSecondaryBg,
    background = DarkNaturalGreen,
    surface = DarkNaturalGreen,
    onPrimary = Color.White,
    onSecondary = NaturalTextMain,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalGreen,
    secondary = NaturalHighlight,
    tertiary = NaturalSecondaryBg,
    background = NaturalLightBg,
    surface = NaturalCardBg,
    onPrimary = Color.White,
    onSecondary = DarkNaturalGreen,
    onBackground = NaturalTextMain,
    onSurface = NaturalTextMain,
    outline = NaturalBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
