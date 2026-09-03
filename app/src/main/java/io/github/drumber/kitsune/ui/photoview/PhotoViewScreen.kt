package io.github.drumber.kitsune.ui.photoview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.component.compose.NavigationBarIconAppearance
import io.github.drumber.kitsune.ui.component.compose.StatusBarIconAppearance
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

/**
 * Full screen zoomable photo viewer. Replaces the former `PhotoViewActivity`, so it can be
 * hosted by the Compose navigation graph on any platform.
 */
@Composable
fun PhotoViewScreen(
    imageUrls: List<String>,
    initialIndex: Int,
    title: String?,
    onClose: () -> Unit,
    onSaveImage: (String) -> Unit,
    onOpenInBrowser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultUseDarkIcons = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    StatusBarIconAppearance(
        useDarkIcons = false,
        defaultUseDarkIcons = defaultUseDarkIcons
    )
    NavigationBarIconAppearance(
        useDarkIcons = false,
        defaultUseDarkIcons = defaultUseDarkIcons
    )

    var controlsVisible by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(imageUrls.indices),
        pageCount = imageUrls::size
    )
    val currentImageUrl = imageUrls[pagerState.currentPage]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            key = { page -> page },
            modifier = Modifier.fillMaxSize()
        ) { page ->
            PhotoGalleryPage(
                imageUrl = imageUrls[page],
                title = title,
                onClick = { controlsVisible = !controlsVisible }
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            IconButton(
                onClick = onClose,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(8.dp)
            ) {
                Icon(Icons.Filled.Close, stringResource(R.string.action_close))
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    if (!title.isNullOrBlank()) {
                        Text(
                            text = title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (imageUrls.size > 1) {
                        Text(
                            text = stringResource(
                                R.string.feed_image_indicator,
                                pagerState.currentPage + 1,
                                imageUrls.size
                            ),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                IconButton(
                    onClick = { onSaveImage(currentImageUrl) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.Download, stringResource(R.string.action_save))
                }
                IconButton(
                    onClick = { onOpenInBrowser(currentImageUrl) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.OpenInBrowser, stringResource(R.string.action_open_in_browser))
                }
            }
        }
    }
}

@Composable
private fun PhotoGalleryPage(
    imageUrl: String,
    title: String?,
    onClick: () -> Unit
) {
    val zoomableState = rememberZoomableState()
    val imageState = rememberZoomableImageState(zoomableState)

    Box(modifier = Modifier.fillMaxSize()) {
        ZoomableAsyncImage(
            model = imageUrl,
            contentDescription = title,
            state = imageState,
            modifier = Modifier.fillMaxSize(),
            onClick = { onClick() }
        )
        if (!imageState.isImageDisplayed) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
