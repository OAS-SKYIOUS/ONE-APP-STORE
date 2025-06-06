package io.github.skyious.oas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.res.painterResource


@Composable

fun Splashscreen (onTimeUp: () -> Unit) {
    LaunchedEffect(Unit) {

        delay(2000L)
        onTimeUp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            AppLogo()

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Loading...",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

@Composable
expect fun AppLogo()
