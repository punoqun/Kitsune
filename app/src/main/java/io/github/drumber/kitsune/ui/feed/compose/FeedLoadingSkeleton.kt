package io.github.drumber.kitsune.ui.feed.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.ui.component.compose.loading.SkeletonBox

@Composable
fun FeedLoadingSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    Column(modifier = modifier) {
        repeat(itemCount) { index ->
            FeedPostLoadingSkeleton(showMedia = index % 3 != 1)
            HorizontalDivider()
        }
    }
}

@Composable
private fun FeedPostLoadingSkeleton(showMedia: Boolean) {
    val lineShape = RoundedCornerShape(4.dp)
    val flatSkeletonModifier = Modifier

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                shadowElevation = 0.dp
            )
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SkeletonBox(
                    modifier = flatSkeletonModifier.fillMaxWidth(0.42f).height(16.dp),
                    shape = lineShape,
                    shadowElevation = 0.dp
                )
                SkeletonBox(
                    modifier = flatSkeletonModifier.fillMaxWidth(0.25f).height(12.dp),
                    shape = lineShape,
                    shadowElevation = 0.dp
                )
            }
            Spacer(Modifier.width(8.dp))
            SkeletonBox(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                shadowElevation = 0.dp
            )
        }

        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBox(
                modifier = flatSkeletonModifier.fillMaxWidth().height(14.dp),
                shape = lineShape,
                shadowElevation = 0.dp
            )
            SkeletonBox(
                modifier = flatSkeletonModifier.fillMaxWidth(0.9f).height(14.dp),
                shape = lineShape,
                shadowElevation = 0.dp
            )
            SkeletonBox(
                modifier = flatSkeletonModifier.fillMaxWidth(0.64f).height(14.dp),
                shape = lineShape,
                shadowElevation = 0.dp
            )
        }

        if (showMedia) {
            Spacer(Modifier.height(8.dp))
            SkeletonBox(
                modifier = flatSkeletonModifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 0.dp
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                shadowElevation = 0.dp
            )
            Spacer(Modifier.width(4.dp))
            SkeletonBox(
                modifier = Modifier.width(24.dp).height(12.dp),
                shape = lineShape,
                shadowElevation = 0.dp
            )
            Spacer(Modifier.weight(1f))
            SkeletonBox(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                shadowElevation = 0.dp
            )
            Spacer(Modifier.width(4.dp))
            SkeletonBox(
                modifier = Modifier.width(24.dp).height(12.dp),
                shape = lineShape,
                shadowElevation = 0.dp
            )
        }
    }
}
