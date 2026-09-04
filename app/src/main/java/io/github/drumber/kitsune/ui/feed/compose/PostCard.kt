package io.github.drumber.kitsune.ui.feed.compose

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Embed
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.PostInteractionStore
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.ui.component.compose.media.MarkdownText
import io.github.drumber.kitsune.ui.component.compose.media.MediaCover
import io.github.drumber.kitsune.util.parseUtcDate

@Composable
fun PostCard(
    post: Post,
    interactionState: PostInteractionStore.State?,
    isRevealed: Boolean,
    nsfwAllowed: Boolean,
    currentUserId: String?,
    canReport: Boolean = false,
    onPostClick: (Post) -> Unit,
    onLikeClick: (Post, Boolean) -> Unit,
    onRevealClick: (Post) -> Unit,
    onMediaClick: (Post) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onEmbedClick: (String) -> Unit = {},
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onReportClick: (Post) -> Unit = {},
    onAuthorClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val needsWarning = post.spoiler || (post.nsfw && !nsfwAllowed)
    val gated = needsWarning && !isRevealed
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick(post) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        PostCardHeader(
            post = post,
            currentUserId = currentUserId,
            canReport = canReport,
            onAuthorClick = onAuthorClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReportClick = onReportClick
        )
        Spacer(Modifier.height(8.dp))
        if (gated) {
            PostContentWarning(
                isNsfw = post.nsfw && !post.spoiler,
                onReveal = { onRevealClick(post) }
            )
        } else {
            PostContentBody(
                post = post,
                onMediaClick = onMediaClick,
                onImageClick = onImageClick,
                onEmbedClick = onEmbedClick
            )
        }
        Spacer(Modifier.height(8.dp))
        PostCardFooter(
            post = post,
            interactionState = interactionState,
            onLikeClick = onLikeClick
        )
    }
    HorizontalDivider()
}

@Composable
private fun PostCardHeader(
    post: Post,
    currentUserId: String?,
    canReport: Boolean,
    onAuthorClick: (String) -> Unit,
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onReportClick: (Post) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Avatar(
            imageUrl = post.authorAvatarUrl,
            size = 40.dp,
            modifier = Modifier.clickable(enabled = post.authorId != null) {
                post.authorId?.let(onAuthorClick)
            }
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.authorName ?: stringResource(R.string.feed_unknown_user),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(enabled = post.authorId != null) {
                    post.authorId?.let(onAuthorClick)
                }
            )
            val timestamp = remember(post.createdAt) {
                post.createdAt?.parseUtcDate()?.let { date ->
                    DateUtils.getRelativeTimeSpanString(
                        date.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString()
                }
            }
            if (timestamp != null) {
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        val isOwner = currentUserId != null && post.authorId == currentUserId
        if (isOwner || canReport) {
            PostOverflowMenu(
                isOwner = isOwner,
                onEditClick = { onEditClick(post) },
                onDeleteClick = { onDeleteClick(post) },
                onReportClick = { onReportClick(post) }
            )
        }
    }
}

@Composable
private fun PostOverflowMenu(
    isOwner: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = null)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        if (isOwner) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_edit)) },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = { expanded = false; onEditClick() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_delete)) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = { expanded = false; onDeleteClick() }
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_report)) },
                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                onClick = { expanded = false; onReportClick() }
            )
        }
    }
}

@Composable
private fun PostContentWarning(isNsfw: Boolean, onReveal: () -> Unit) {
    FilledTonalButton(onClick = onReveal, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                if (isNsfw) R.string.feed_nsfw_warning_title else R.string.feed_spoiler_warning_title
            )
        )
    }
}

@Composable
private fun PostContentBody(
    post: Post,
    onMediaClick: (Post) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onEmbedClick: (String) -> Unit
) {
    if (!post.contentFormatted.isNullOrBlank() || !post.content.isNullOrBlank()) {
        MarkdownText(
            content = post.content,
            contentFormatted = post.contentFormatted,
            modifier = Modifier.fillMaxWidth()
        )
    }
    if (post.imageUrls.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        PostImagePreview(
            imageUrls = post.imageUrls,
            onImageClick = { index -> onImageClick(post.imageUrls, index) }
        )
    }
    val embed = post.embed
    if (embed != null && embed.hasRenderableContent) {
        Spacer(Modifier.height(8.dp))
        PostEmbedCard(embed = embed, onClick = onEmbedClick)
    }
    if (!post.mediaTitle.isNullOrBlank()) {
        Spacer(Modifier.height(8.dp))
        PostMediaCard(post = post, onMediaClick = onMediaClick)
    }
}

@Composable
private fun PostImagePreview(
    imageUrls: List<String>,
    onImageClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        when (imageUrls.size) {
            1 -> PostPreviewImage(
                imageUrl = imageUrls[0],
                index = 0,
                onClick = onImageClick,
                modifier = Modifier.fillMaxSize()
            )
            2 -> Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                imageUrls.forEachIndexed { index, imageUrl ->
                    PostPreviewImage(
                        imageUrl = imageUrl,
                        index = index,
                        onClick = onImageClick,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
            3 -> Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                PostPreviewImage(
                    imageUrl = imageUrls[0],
                    index = 0,
                    onClick = onImageClick,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    imageUrls.drop(1).forEachIndexed { offset, imageUrl ->
                        PostPreviewImage(
                            imageUrl = imageUrl,
                            index = offset + 1,
                            onClick = onImageClick,
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        )
                    }
                }
            }
            else -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                imageUrls.take(4).chunked(2).forEachIndexed { rowIndex, rowImages ->
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        rowImages.forEachIndexed { columnIndex, imageUrl ->
                            val index = rowIndex * 2 + columnIndex
                            PostPreviewImage(
                                imageUrl = imageUrl,
                                index = index,
                                remainingCount = if (index == 3) imageUrls.size - 4 else 0,
                                onClick = onImageClick,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostPreviewImage(
    imageUrl: String,
    index: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    remainingCount: Int = 0
) {
    Box(modifier = modifier.clickable { onClick(index) }) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_insert_photo_48),
            error = painterResource(R.drawable.ic_insert_photo_48),
            modifier = Modifier.fillMaxSize()
        )
        if (remainingCount > 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.feed_image_count_more, remainingCount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PostEmbedCard(embed: Embed, onClick: (String) -> Unit) {
    val targetUrl = embed.url?.takeIf { it.isNotBlank() }
        ?: embed.videoUrl?.takeIf { it.isNotBlank() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = targetUrl != null) {
                targetUrl?.let(onClick)
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (!embed.imageUrl.isNullOrBlank()) {
                Box {
                    MediaCover(
                        imageUrl = embed.imageUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    if (embed.isVideo) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.55f),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            if (!embed.siteName.isNullOrBlank()) {
                Text(
                    text = embed.siteName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!embed.title.isNullOrBlank()) {
                Text(text = embed.title, style = MaterialTheme.typography.titleSmall)
            }
            if (!embed.description.isNullOrBlank()) {
                Text(
                    text = embed.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private val Embed.hasRenderableContent: Boolean
    get() = !imageUrl.isNullOrBlank() ||
        !title.isNullOrBlank() ||
        !description.isNullOrBlank() ||
        !siteName.isNullOrBlank() ||
        !url.isNullOrBlank() ||
        !videoUrl.isNullOrBlank()

@Composable
private fun PostMediaCard(post: Post, onMediaClick: (Post) -> Unit) {
    val canOpen = !post.mediaSlug.isNullOrBlank() && post.mediaIsAnime != null
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canOpen) { onMediaClick(post) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            MediaCover(
                imageUrl = post.mediaPosterUrl,
                modifier = Modifier
                    .size(width = 48.dp, height = 68.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.mediaTitle ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!post.mediaSynopsis.isNullOrBlank()) {
                    Text(
                        text = post.mediaSynopsis,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PostLikerAvatars(likerAvatars: List<String>, totalLikes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val visibleLikers = likerAvatars.take(3)
        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
            visibleLikers.forEachIndexed { index, url ->
                Avatar(
                    imageUrl = url,
                    size = 20.dp,
                    modifier = Modifier.zIndex((visibleLikers.size - index).toFloat())
                )
            }
        }
        val remaining = (totalLikes - visibleLikers.size).coerceAtLeast(0)
        if (remaining > 0) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.feed_likers_more, remaining),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostCardFooter(
    post: Post,
    interactionState: PostInteractionStore.State?,
    onLikeClick: (Post, Boolean) -> Unit
) {
    val isLiked = interactionState?.isLiked ?: false
    val likesCount = interactionState?.likesCount ?: post.likesCount
    val commentsCount = interactionState?.commentsCount ?: post.commentsCount
    val likerAvatars = interactionState?.likerAvatars.orEmpty()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onLikeClick(post, !isLiked) }, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isLiked) R.string.cd_unlike_post else R.string.cd_like_post,
                        likesCount
                    ),
                    tint = if (isLiked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Text(
                text = likesCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (likerAvatars.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                PostLikerAvatars(likerAvatars = likerAvatars, totalLikes = likesCount)
            }
        }
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = stringResource(R.string.cd_comments_count, commentsCount),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = commentsCount.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DeletePostConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_post_confirm_title)) },
        text = { Text(stringResource(R.string.delete_post_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}
