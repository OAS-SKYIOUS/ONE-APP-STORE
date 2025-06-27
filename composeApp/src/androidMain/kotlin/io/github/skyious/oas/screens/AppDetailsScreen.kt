import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.util.DebugLogger
import io.github.skyious.oas.R
import io.github.skyious.oas.data.model.AppDetail
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.ui.DetailUiState
import io.github.skyious.oas.ui.DetailViewModel
import kotlinx.coroutines.launch
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// Define colors to match the HTML design
val textPrimary = Color(0xFF0D151C)
val textSecondary = Color(0xFF49749C)
val primaryAction = Color(0xFF0B80EE)
val surfaceBackground = Color(0xFFF8FAFC) // slate-50
val progressBackground = Color(0xFFCEDCE8)

@Composable
fun AppDetailsScreen(
    appInfo: AppInfo,
    onBackPress: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    LaunchedEffect(key1 = appInfo.id) {
        viewModel.loadDetail(appInfo)
    }

    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val imageLoader = remember {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .logger(DebugLogger())
            .build()
    }

    Scaffold(
        topBar = { AppDetailsTopBar(onBackPress = onBackPress) },
        bottomBar = { InstallButton() },
        containerColor = surfaceBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is DetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is DetailUiState.Success -> {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        AppHeader(appInfo = appInfo, detail = state.appDetail, imageLoader = imageLoader)
                        RatingsSection()
                        AppTabs()
                        AboutSection(description = state.appDetail.description ?: state.appDetail.summary)
                        ScreenshotsSection(images = state.appDetail.images ?: emptyList(), imageLoader = imageLoader)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsTopBar(onBackPress: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "App Details",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPress) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textPrimary
                )
            }
        },
        actions = {
            Spacer(modifier = Modifier.width(48.dp)) // To balance the title
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceBackground)
    )
}

@Composable
fun AppHeader(appInfo: AppInfo, detail: AppDetail, imageLoader: ImageLoader) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val logoUrl = detail.logoUrl ?: appInfo.logoUrl

        if (logoUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (detail.name ?: appInfo.name).take(1),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            AsyncImage(
                model = logoUrl,
                contentDescription = "App Icon",
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.oas_logo),
                error = painterResource(id = R.drawable.oas_logo),
                onError = { error ->
                    Log.e("COIL_ERROR", "Failed to load logo '$logoUrl': ${error.result.throwable}")
                }
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = detail.name ?: appInfo.name,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = textPrimary
            )
            Text(
                text = (detail.author ?: appInfo.author).orEmpty(),
                fontSize = 14.sp,
                color = textSecondary
            )
        }
    }
}

@Composable
fun RatingsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(end = 24.dp)
        ) {
            Text(
                text = "4.6",
                fontWeight = FontWeight.Black,
                fontSize = 40.sp,
                color = textPrimary
            )
            Row {
                repeat(4) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = primaryAction,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Star, // Using filled star and tinting to represent "regular"
                    contentDescription = null,
                    tint = progressBackground, // A neutral color for the non-filled part
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "12,345 reviews",
                fontSize = 16.sp,
                color = textPrimary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            RatingProgress(star = 5, percentage = 70)
            RatingProgress(star = 4, percentage = 15)
            RatingProgress(star = 3, percentage = 7)
            RatingProgress(star = 2, percentage = 3)
            RatingProgress(star = 1, percentage = 5)
        }
    }
}

@Composable
fun RatingProgress(star: Int, percentage: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(text = star.toString(), fontSize = 14.sp, color = textPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape),
            color = primaryAction,
            trackColor = progressBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$percentage%",
            fontSize = 14.sp,
            color = textSecondary,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTabs() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("About", "Reviews", "Related")

    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = surfaceBackground,
        contentColor = primaryAction
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTabIndex == index) textPrimary else textSecondary
                    )
                }
            )
        }
    }
}

@Composable
fun AboutSection(description: String?) {
    Text(
        text = description ?: "No description available.",
        modifier = Modifier.padding(16.dp),
        fontSize = 16.sp,
        color = textPrimary,
        lineHeight = 24.sp
    )
}

@Composable
fun ScreenshotsSection(images: List<String>, imageLoader: ImageLoader) {
    if (images.isNotEmpty()) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Text(
                text = "Screenshots",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Screenshot",
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = R.drawable.oas_logo),
                        error = painterResource(id = R.drawable.oas_logo),
                        onError = { error ->
                            Log.e("COIL_ERROR", "Failed to load screenshot '$imageUrl': ${error.result.throwable}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenshotCard(imageUrl: String, caption: String) {
    Column(
        modifier = Modifier.width(150.dp) // min-w-60 is large, adjusted for a better look
    ) {
        Image(
            painter = painterResource(id = R.drawable.oas_logo),
            contentDescription = caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = caption,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textPrimary
        )
    }
}

@Composable
fun InstallButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = { /* Handle install click */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = primaryAction)
        ) {
            Text(
                text = "Install",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

//@Preview(showBackground = true, device = "id:pixel_6")
//@Composable
//fun DefaultPreview() {
//    MaterialTheme {
//        AppDetailsScreen()
//    }
//}