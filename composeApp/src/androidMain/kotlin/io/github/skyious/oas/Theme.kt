
package io.github.skyious.oas // The package from your original code

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
// Make sure to import the Typography object you just defined!
import io.github.skyious.oas.ui.theme.AppTypography

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {

    val colors = if (darkTheme) darkColorScheme() else lightColorScheme()


    MaterialTheme(
        colorScheme = colors,
        // Pass the Typography object you defined in Typography.kt
        typography = AppTypography,
        content = content
    )
}