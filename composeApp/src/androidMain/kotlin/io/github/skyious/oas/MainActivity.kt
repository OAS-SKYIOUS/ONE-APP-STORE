package io.github.skyious.oas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.skyious.oas.screens.homescreen
import org.jetbrains.compose.resources.painterResource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Splashscreen (onTimeUp =  {
                    homescreen()
                })
            }
        }
    }
}

@Preview
@Composable
fun Main () {
    Splashscreen(onTimeUp =  {
        homescreen()
    })
}

@Composable

actual fun AppLogo() {
    Image(
        painter = androidx.compose.ui.res.painterResource(id = R.drawable.oas_logo),
        contentDescription = "Logo",
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
    )
}