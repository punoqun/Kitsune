package io.github.drumber.kitsune.ui.details.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.list.PagingColumn
import io.github.drumber.kitsune.data.repository.PostInteractionStore
import io.github.drumber.kitsune.ui.feed.compose.PostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaFeedScreen(
    title: String,
    items: LazyPagingItems<Post>,
    interactionStates: Map<String, PostInteractionStore.State>,
    revealedPosts: Set<String>,
    nsfwAllowed: Boolean,
    currentUserId: String?,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onPostClick: (Post) -> Unit,
    onLikeClick: (Post, Boolean) -> Unit,
    onRevealClick: (Post) -> Unit,
    onMediaClick: (Post) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onEmbedClick: (String) -> Unit,
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onReportClick: (Post) -> Unit,
    onAuthorClick: (String) -> Unit,
    onGroupClick: (String) -> Unit
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
                    PostCard(
                        post = item,
                        interactionState = interactionStates[item.id],
                        isRevealed = item.id in revealedPosts,
                        nsfwAllowed = nsfwAllowed,
                        currentUserId = currentUserId,
                        truncateContent = true,
                        canReport = currentUserId != null,
                        onPostClick = onPostClick,
                        onLikeClick = onLikeClick,
                        onRevealClick = onRevealClick,
                        onMediaClick = onMediaClick,
                        onImageClick = onImageClick,
                        onEmbedClick = onEmbedClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick,
                        onReportClick = onReportClick,
                        onAuthorClick = onAuthorClick,
                        onGroupClick = onGroupClick
                    )
                }
            }
        }
    }
}
