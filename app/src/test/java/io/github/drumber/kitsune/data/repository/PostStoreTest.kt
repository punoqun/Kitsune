package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.presentation.model.feed.Post
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PostStoreTest {

    @Test
    fun `returns cached posts and replaces stale values`() {
        val store = PostStore()
        val original = post("1", likesCount = 1)
        val updated = post("1", likesCount = 2)

        store.put(original)
        store.put(updated)

        assertThat(store.get("1")).isEqualTo(updated)
    }

    @Test
    fun `evicts the least recently used post`() {
        val store = PostStore(maxSize = 2)
        store.put(post("1"))
        store.put(post("2"))
        store.get("1")

        store.put(post("3"))

        assertThat(store.get("1")).isNotNull()
        assertThat(store.get("2")).isNull()
        assertThat(store.get("3")).isNotNull()
    }

    private fun post(id: String, likesCount: Int = 0) = Post(
        id = id,
        createdAt = null,
        content = null,
        contentFormatted = null,
        spoiler = false,
        nsfw = false,
        commentsCount = 0,
        likesCount = likesCount,
        authorId = null,
        authorName = null,
        authorAvatarUrl = null,
        mediaTitle = null,
        mediaId = null,
        mediaPosterUrl = null,
        mediaSynopsis = null,
        mediaSlug = null,
        mediaIsAnime = null,
        spoiledUnitNumber = null,
        spoiledUnitId = null,
        spoiledUnitTitle = null,
        spoiledUnitIsEpisode = false,
        imageUrls = emptyList(),
        uploadIds = emptyList(),
        embed = null
    )
}
