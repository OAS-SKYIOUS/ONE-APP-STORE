package io.github.skyious.oas

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
actual fun AppLogo() {
    Image(
        painter = painterResource("oas_logo.png"),
        contentDescription = "Logo",
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
    )
}