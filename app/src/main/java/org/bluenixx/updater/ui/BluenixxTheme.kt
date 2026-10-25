/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.bluenixx.updater.ui

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
import com.android.settingslib.spa.framework.theme.SettingsTheme

private val BluenixxLightColors = lightColorScheme(
    primary = Color(0xFF006A70),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB5F1F1),
    onPrimaryContainer = Color(0xFF002F33),
    secondary = Color(0xFF4A6264),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8E9),
    onSecondaryContainer = Color(0xFF061F21),
    tertiary = Color(0xFF52618A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCE1FF),
    onTertiaryContainer = Color(0xFF0C1940),
    background = Color(0xFFF6FAF9),
    onBackground = Color(0xFF171D1E),
    surface = Color(0xFFF6FAF9),
    onSurface = Color(0xFF171D1E),
    surfaceVariant = Color(0xFFDCE5E5),
    onSurfaceVariant = Color(0xFF404849),
)

private val BluenixxDarkColors = darkColorScheme(
    primary = Color(0xFF86D7D9),
    onPrimary = Color(0xFF003739),
    primaryContainer = Color(0xFF005055),
    onPrimaryContainer = Color(0xFFB5F1F1),
    secondary = Color(0xFFB1CBCC),
    onSecondary = Color(0xFF1B3435),
    secondaryContainer = Color(0xFF324B4D),
    onSecondaryContainer = Color(0xFFCCE8E9),
    tertiary = Color(0xFFBAC5F5),
    onTertiary = Color(0xFF24305A),
    tertiaryContainer = Color(0xFF3B4672),
    onTertiaryContainer = Color(0xFFDCE1FF),
    background = Color(0xFF101516),
    onBackground = Color(0xFFE0E7E7),
    surface = Color(0xFF101516),
    onSurface = Color(0xFFE0E7E7),
    surfaceVariant = Color(0xFF3F4849),
    onSurfaceVariant = Color(0xFFBEC8C8),
)

@Composable
fun BluenixxTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    SettingsTheme {
        val settingsTypography = MaterialTheme.typography
        val colors = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme ->
                dynamicDarkColorScheme(context)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                dynamicLightColorScheme(context)
            darkTheme -> BluenixxDarkColors
            else -> BluenixxLightColors
        }
        MaterialTheme(
            colorScheme = colors,
            typography = settingsTypography,
            content = content,
        )
    }
}
