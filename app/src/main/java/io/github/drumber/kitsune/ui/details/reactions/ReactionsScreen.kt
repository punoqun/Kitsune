package io.github.drumber.kitsune.ui.details.reactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.github.drumber.kitsune.R
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.list.PagingColumn
import io.github.drumber.kitsune.ui.component.compose.media.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionsScreen(
    title: String,
    items: LazyPagingItems<MediaReaction>,
    currentUserId: String?,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onAddReactionClick: () -> Unit,
    onReactionClick: (MediaReaction) -> Unit,
    onAuthorClick: (String) -> Unit,
    onUpvoteClick: (MediaReaction) -> Unit,
    onEditClick: (MediaReaction) -> Unit,
    onDeleteClick: (MediaReaction) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            KitsuneCollapsingTopAppBar(
                title = { Text(title) },
                navigationIcon = { KitsuneBackButton(onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReactionClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_add_reaction)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        val isRefreshing = items.loadState.refresh is LoadState.Loading && items.itemCount > 0
        KitsunePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { items.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PagingColumn(
                items = items,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                key = { it.id }
            ) { item ->
                if (item != null) {
                    ReactionItem(
                        reaction = item,
                        isOwn = item.authorId != null && item.authorId == currentUserId,
                        onClick = { onReactionClick(item) },
                        onAuthorClick = { item.authorId?.let(onAuthorClick) },
                        onUpvoteClick = { onUpvoteClick(item) },
                        onEditClick = { onEditClick(item) },
                        onDeleteClick = { onDeleteClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReactionItem(
    reaction: MediaReaction,
    isOwn: Boolean,
    onClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onUpvoteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                enabled = reaction.authorId != null,
                onClick = onAuthorClick
            )
        ) {
            Avatar(
                imageUrl = reaction.authorAvatarUrl,
                size = 36.dp,
                contentDescription = reaction.authorName
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = reaction.authorName.orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(6.dp))
        val content = reaction.reaction?.takeIf { it.isNotBlank() } ?: reaction.content
        if (!content.isNullOrBlank()) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onUpvoteClick) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = stringResource(
                        R.string.action_upvote_reaction,
                        reaction.upVotesCount
                    )
                )
            }
            Text(
                text = reaction.upVotesCount.toString(),
                style = MaterialTheme.typography.labelMedium
            )
            if (isOwn) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.action_edit)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete)
                    )
                }
            }
        }
    }
}
