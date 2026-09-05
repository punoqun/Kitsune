package io.github.drumber.kitsune.ui.component.compose.loading

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonBox(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    shadowElevation: Dp = 2.dp
) {
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_200),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonShimmerProgress"
    )
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlightColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val shadowColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)

    Box(
        modifier = modifier
            .clearAndSetSemantics {}
            .then(
                if (shadowElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = shadowElevation,
                        shape = shape,
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .drawWithCache {
                val startX = size.width * progress
                val shimmerWidth = size.width.coerceAtLeast(1f)
                val brush = Brush.linearGradient(
                    colors = listOf(baseColor, highlightColor, baseColor),
                    start = Offset(startX - shimmerWidth, 0f),
                    end = Offset(startX, size.height)
                )
                onDrawBehind { drawRect(brush) }
            }
    )
}

@Composable
fun ListLoadingSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6,
    showLeading: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(itemCount) { index ->
            Row(modifier = Modifier.fillMaxWidth()) {
                if (showLeading) {
                    SkeletonBox(modifier = Modifier.size(64.dp))
                    Spacer(Modifier.width(12.dp))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(if (index % 2 == 0) 0.72f else 0.9f)
                            .height(18.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(if (index % 3 == 0) 0.52f else 0.68f)
                            .height(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GridLoadingSkeleton(
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Adaptive(112.dp),
    itemCount: Int = 9,
    itemAspectRatio: Float = 2f / 3f,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp)
) {
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        userScrollEnabled = false
    ) {
        items(itemCount) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(itemAspectRatio)
            )
        }
    }
}

@Composable
fun HorizontalLoadingSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 4,
    itemWidth: Dp = 106.dp,
    itemHeight: Dp = 150.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp)
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        userScrollEnabled = false
    ) {
        items(itemCount) {
            SkeletonBox(modifier = Modifier.size(width = itemWidth, height = itemHeight))
        }
    }
}

@Composable
fun DetailLoadingSkeleton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            SkeletonBox(modifier = Modifier.size(width = 106.dp, height = 150.dp))
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(28.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.72f).height(20.dp))
                Spacer(Modifier.height(8.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp))
            }
        }
        TextLoadingSkeleton(lineCount = 5)
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp))
    }
}

@Composable
fun TextLoadingSkeleton(
    modifier: Modifier = Modifier,
    lineCount: Int = 8
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(lineCount) { index ->
            val widthFraction = when (index % 4) {
                1 -> 0.86f
                2 -> 0.72f
                3 -> 0.58f
                else -> 1f
            }
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(16.dp)
            )
        }
    }
}

@Composable
fun FullScreenListLoadingSkeleton(
    modifier: Modifier = Modifier,
    showLeading: Boolean = true
) {
    Box(modifier = modifier.fillMaxSize()) {
        ListLoadingSkeleton(
            modifier = Modifier.fillMaxWidth(),
            showLeading = showLeading
        )
    }
}
