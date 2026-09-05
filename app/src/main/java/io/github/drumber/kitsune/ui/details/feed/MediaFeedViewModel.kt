package io.github.drumber.kitsune.ui.details.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.ContentRevealStore
import io.github.drumber.kitsune.data.repository.FeedRepository
import io.github.drumber.kitsune.data.repository.PostInteractionRepository
import io.github.drumber.kitsune.data.repository.PostInteractionStore
import io.github.drumber.kitsune.data.repository.PostManagementRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.user.model.LocalSfwFilterPreference
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MediaFeedViewModel(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val postManagementRepository: PostManagementRepository,
    private val postInteractionRepository: PostInteractionRepository,
    private val postInteractionStore: PostInteractionStore,
    private val contentRevealStore: ContentRevealStore,
    private val getLocalUserId: GetLocalUserIdUseCase,
) : ViewModel() {

    private sealed interface MediaFeedKey {
        data class Media(val mediaId: String, val isAnime: Boolean) : MediaFeedKey
        data class Unit(val unitId: String, val isEpisode: Boolean) : MediaFeedKey
    }

    sealed interface ActionEvent {
        data object LoginRequired : ActionEvent
        data object PostDeleted : ActionEvent
        data object Error : ActionEvent
    }

    private val mediaFeedKey = MutableStateFlow<MediaFeedKey?>(null)

    private val actionEventChannel = Channel<ActionEvent>(Channel.BUFFERED)
    val actionEvents: Flow<ActionEvent> = actionEventChannel.receiveAsFlow()

    val localUserId: String?
        get() = getLocalUserId()

    val nsfwAllowed: Boolean
        get() = userRepository.localUser.value?.sfwFilterPreference ==
                LocalSfwFilterPreference.NSFW_EVERYWHERE

    val revealedPosts = contentRevealStore.revealed
    val interactionStates = postInteractionStore.states

    fun initMediaFeed(mediaId: String, isAnime: Boolean) {
        val key = MediaFeedKey.Media(mediaId, isAnime)
        if (mediaFeedKey.value != key) {
            mediaFeedKey.value = key
        }
    }

    fun initUnitFeed(unitId: String, isEpisode: Boolean) {
        val key = MediaFeedKey.Unit(unitId, isEpisode)
        if (mediaFeedKey.value != key) {
            mediaFeedKey.value = key
        }
    }

    val dataSource: Flow<PagingData<Post>> = mediaFeedKey.filterNotNull().flatMapLatest { key ->
        when (key) {
            is MediaFeedKey.Media -> feedRepository.mediaFeedPager(key.isAnime, key.mediaId)
            is MediaFeedKey.Unit -> feedRepository.mediaUnitFeedPager(key.isEpisode, key.unitId)
        }
    }.cachedIn(viewModelScope)

    /** Remembers that the user revealed the gated content of the given post. */
    fun revealPost(post: Post) {
        contentRevealStore.reveal(post.id)
    }

    fun togglePostLike(post: Post, targetLiked: Boolean) {
        val userId = getLocalUserId()
        if (userId == null) {
            actionEventChannel.trySend(ActionEvent.LoginRequired)
            return
        }

        val previousState = postInteractionStore.get(post.id)
        val previousLiked = previousState?.isLiked ?: !targetLiked
        val previousCount = previousState?.likesCount ?: post.likesCount
        val targetCount = (previousCount + if (targetLiked) 1 else -1).coerceAtLeast(0)
        val revision = postInteractionStore.setLikeState(post.id, targetLiked, targetCount)

        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                postInteractionRepository.setPostLiked(post.id, userId, targetLiked)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logE("Failed to toggle like for post '${post.id}'.", e)
                postInteractionStore.restoreLikeState(
                    post.id,
                    revision,
                    previousLiked,
                    previousCount
                )
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                postManagementRepository.deletePost(post.id)
                actionEventChannel.send(ActionEvent.PostDeleted)
            } catch (e: Exception) {
                logE("Failed to delete post '${post.id}'.", e)
                actionEventChannel.send(ActionEvent.Error)
            }
        }
    }
}
