package io.github.skyious.oas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import oneappstore.composeapp.generated.resources.Res
import oneappstore.composeapp.generated.resources.oas_logo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Drawable

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ONE APP STORE",
    ) {
        App()
    }
}


@Composable
actual fun AppLogo() {
    Image(
        painter = painterResource(Res.drawable.oas_logo),
        contentDescription = "Logo",
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
    )
}