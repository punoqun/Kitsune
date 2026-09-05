package io.github.drumber.kitsune.ui.postdetail.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.PagingErrorContent
import io.github.drumber.kitsune.ui.component.compose.loading.DetailLoadingSkeleton
import io.github.drumber.kitsune.ui.component.compose.loading.ListLoadingSkeleton
import io.github.drumber.kitsune.ui.feed.compose.PostCard
import io.github.drumber.kitsune.ui.postdetail.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: Post?,
    postLoadState: PostDetailViewModel.PostLoadState,
    postLikeState: PostDetailViewModel.PostLikeUiState,
    isPostRevealed: Boolean,
    nsfwAllowed: Boolean,
    comments: LazyPagingItems<Comment>,
    commentLikeOverrides: Map<String, Pair<Boolean, Int>>,
    composerMode: PostDetailViewModel.ComposerMode,
    composerResetKey: Int,
    currentUserId: String?,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    onNavigateUp: () -> Unit,
    onRetryPost: () -> Unit,
    onPostLikeClick: () -> Unit,
    onRevealPost: () -> Unit,
    onMediaClick: (Post) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onEmbedClick: (String) -> Unit,
    onEditPost: (Post) -> Unit,
    onDeletePost: () -> Unit,
    onReportPost: (Post) -> Unit,
    onAuthorClick: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onCommentLikeClick: (Comment) -> Unit,
    onReplyClick: (Comment) -> Unit,
    onViewAllRepliesClick: (Comment) -> Unit,
    onEditComment: (Comment) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    onReportComment: (Comment) -> Unit,
    onCancelComposer: () -> Unit,
    onSubmitComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    var showDeletePost by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KitsuneTopAppBar(
                title = { Text(stringResource(R.string.title_post)) },
                navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            CommentInputBar(
                composerMode = composerMode,
                resetKey = composerResetKey,
                onCancel = onCancelComposer,
                onSubmit = onSubmitComment
            )
        }
    ) { innerPadding ->
        PostDetailContent(
            post = post,
            postLoadState = postLoadState,
            postLikeState = postLikeState,
            isPostRevealed = isPostRevealed,
            nsfwAllowed = nsfwAllowed,
            comments = comments,
            commentLikeOverrides = commentLikeOverrides,
            currentUserId = currentUserId,
            onPostLikeClick = onPostLikeClick,
            onRetryPost = onRetryPost,
            onRevealPost = onRevealPost,
            onMediaClick = onMediaClick,
            onImageClick = onImageClick,
            onEmbedClick = onEmbedClick,
            onEditPost = onEditPost,
            onDeletePost = { showDeletePost = true },
            onReportPost = onReportPost,
            onAuthorClick = onAuthorClick,
            onGroupClick = onGroupClick,
            onCommentLikeClick = onCommentLikeClick,
            onReplyClick = onReplyClick,
            onViewAllRepliesClick = onViewAllRepliesClick,
            onEditComment = onEditComment,
            onDeleteComment = { commentToDelete = it },
            onReportComment = onReportComment,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }

    if (showDeletePost) {
        io.github.drumber.kitsune.ui.feed.compose.DeletePostConfirmDialog(
            onConfirm = { onDeletePost(); showDeletePost = false },
            onDismiss = { showDeletePost = false }
        )
    }
    commentToDelete?.let { comment ->
        DeleteCommentConfirmDialog(
            onConfirm = { onDeleteComment(comment); commentToDelete = null },
            onDismiss = { commentToDelete = null }
        )
    }
}

@Composable
private fun PostDetailContent(
    post: Post?,
    postLoadState: PostDetailViewModel.PostLoadState,
    postLikeState: PostDetailViewModel.PostLikeUiState,
    isPostRevealed: Boolean,
    nsfwAllowed: Boolean,
    comments: LazyPagingItems<Comment>,
    commentLikeOverrides: Map<String, Pair<Boolean, Int>>,
    currentUserId: String?,
    onPostLikeClick: () -> Unit,
    onRetryPost: () -> Unit,
    onRevealPost: () -> Unit,
    onMediaClick: (Post) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onEmbedClick: (String) -> Unit,
    onEditPost: (Post) -> Unit,
    onDeletePost: () -> Unit,
    onReportPost: (Post) -> Unit,
    onAuthorClick: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onCommentLikeClick: (Comment) -> Unit,
    onReplyClick: (Comment) -> Unit,
    onViewAllRepliesClick: (Comment) -> Unit,
    onEditComment: (Comment) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    onReportComment: (Comment) -> Unit,
    modifier: Modifier = Modifier
) {
    val refreshState = comments.loadState.refresh
    val appendState = comments.loadState.append
    LazyColumn(modifier = modifier) {
        when {
            post != null -> {
                item(key = "post_${post.id}") {
                    PostDetailHeader(
                        post = post,
                        postLikeState = postLikeState,
                        isPostRevealed = isPostRevealed,
                        nsfwAllowed = nsfwAllowed,
                        currentUserId = currentUserId,
                        onPostLikeClick = onPostLikeClick,
                        onRevealPost = onRevealPost,
                        onMediaClick = onMediaClick,
                        onImageClick = onImageClick,
                        onEmbedClick = onEmbedClick,
                        onEditPost = onEditPost,
                        onDeletePost = onDeletePost,
                        onReportPost = onReportPost,
                        onAuthorClick = onAuthorClick,
                        onGroupClick = onGroupClick
                    )
                    HorizontalDivider()
                }
            }
            postLoadState == PostDetailViewModel.PostLoadState.Error -> {
                item {
                    PostLoadingError(onRetry = onRetryPost)
                }
            }
            else -> {
                item {
                    DetailLoadingSkeleton(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                    )
                }
            }
        }
        when {
            refreshState is LoadState.Loading && comments.itemCount == 0 ->
                item {
                    ListLoadingSkeleton(
                        modifier = Modifier.fillMaxWidth(),
                        itemCount = 3
                    )
                }
            refreshState is LoadState.Error && comments.itemCount == 0 ->
                item {
                    PagingErrorContent(
                        modifier = Modifier.fillMaxWidth(),
                        onRetry = { comments.retry() }
                    )
                }
            else -> {
                items(count = comments.itemCount, key = comments.itemKey { it.id }) { index ->
                    comments[index]?.let { comment ->
                        val (liked, count) = commentLikeOverrides[comment.id]
                            ?: Pair(comment.isLikedByMe, comment.likesCount)
                        CommentCard(
                            comment = comment,
                            isLiked = liked,
                            likesCount = count,
                            currentUserId = currentUserId,
                            onLikeClick = onCommentLikeClick,
                            onReplyClick = onReplyClick,
                            onViewAllRepliesClick = onViewAllRepliesClick,
                            onEditClick = onEditComment,
                            onDeleteClick = onDeleteComment,
                            onReportClick = onReportComment,
                            onImageClick = { imageUrl -> onImageClick(listOf(imageUrl), 0) },
                            onAuthorClick = onAuthorClick,
                            likeOverrides = commentLikeOverrides
                        )
                    }
                }
            }
        }
        when (appendState) {
            is LoadState.Loading -> item { PagingAppendLoading() }
            is LoadState.Error -> item { PagingAppendError(onRetry = { comments.retry() }) }
            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun PostLoadingError(onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.error_resource_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        androidx.compose.material3.TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Composable
private fun PostDetailHeader(
    post: Post,
    postLikeState: PostDetailViewModel.PostLikeUiState,
    isPostRevealed: Boolean,
    nsfwAllowed: Boolean,
    currentUserId: String?,
    onPostLikeClick: () -> Unit,
    onRevealPost: () -> Unit,
    onMediaClick: (Post) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onEmbedClick: (String) -> Unit,
    onEditPost: (Post) -> Unit,
    onDeletePost: () -> Unit,
    onReportPost: (Post) -> Unit,
    onAuthorClick: (String) -> Unit,
    onGroupClick: (String) -> Unit
) {
    val likeState = io.github.drumber.kitsune.data.repository.PostInteractionStore.State(
        isLiked = postLikeState.isLiked,
        likesCount = postLikeState.count
    )
    PostCard(
        post = post,
        interactionState = likeState,
        isRevealed = isPostRevealed,
        nsfwAllowed = nsfwAllowed,
        currentUserId = currentUserId,
        canReport = currentUserId != null,
        onPostClick = {},
        onLikeClick = { _, _ -> onPostLikeClick() },
        onRevealClick = { onRevealPost() },
        onMediaClick = onMediaClick,
        onImageClick = onImageClick,
        onEmbedClick = onEmbedClick,
        onEditClick = onEditPost,
        onDeleteClick = { onDeletePost() },
        onReportClick = onReportPost,
        onAuthorClick = onAuthorClick,
        onGroupClick = onGroupClick
    )
}

@Composable
private fun CommentInputBar(
    composerMode: PostDetailViewModel.ComposerMode,
    resetKey: Int,
    onCancel: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var inputText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(composerMode, resetKey) {
        when (composerMode) {
            is PostDetailViewModel.ComposerMode.Normal -> inputText = ""
            is PostDetailViewModel.ComposerMode.Edit ->
                inputText = composerMode.comment.content ?: ""
            is PostDetailViewModel.ComposerMode.Reply -> Unit
        }
    }

    Surface(shadowElevation = 4.dp) {
        Column(modifier = Modifier.imePadding()) {
            if (composerMode !is PostDetailViewModel.ComposerMode.Normal) {
                ComposerContextRow(composerMode = composerMode, onCancel = onCancel)
                HorizontalDivider()
            }
            val hint = when (composerMode) {
                is PostDetailViewModel.ComposerMode.Reply ->
                    stringResource(R.string.comment_reply_hint)
                else -> stringResource(R.string.hint_add_comment)
            }
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text(hint) },
                trailingIcon = {
                    IconButton(
                        onClick = { if (inputText.isNotBlank()) onSubmit(inputText.trim()) },
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.action_send)
                        )
                    }
                },
                maxLines = 3,
                singleLine = false
            )
        }
    }
}

@Composable
private fun ComposerContextRow(
    composerMode: PostDetailViewModel.ComposerMode,
    onCancel: () -> Unit
) {
    val contextText = when (composerMode) {
        is PostDetailViewModel.ComposerMode.Reply -> stringResource(
            R.string.comment_replying_to,
            composerMode.comment.authorName ?: stringResource(R.string.feed_unknown_user)
        )
        is PostDetailViewModel.ComposerMode.Edit -> stringResource(R.string.comment_editing)
        PostDetailViewModel.ComposerMode.Normal -> ""
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = contextText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
        }
    }
}

@Composable
private fun PagingAppendLoading() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun PagingAppendError(onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.error_resource_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        androidx.compose.material3.TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}
