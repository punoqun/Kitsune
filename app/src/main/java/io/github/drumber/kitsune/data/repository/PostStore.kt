package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.config.Repository
import io.github.drumber.kitsune.data.presentation.model.feed.Post

/**
 * In-memory LRU cache of posts loaded during the current app session.
 *
 * Feed and detail screens share this store so opening a visible post can render immediately while
 * its latest network representation is refreshed in the background.
 */
class PostStore(
    private val maxSize: Int = Repository.MAX_CACHED_ITEMS
) {

    private val posts = LinkedHashMap<String, Post>(maxSize, 0.75f, true)

    @Synchronized
    fun get(postId: String): Post? = posts[postId]

    @Synchronized
    fun put(post: Post) {
        posts[post.id] = post
        while (posts.size > maxSize) {
            posts.remove(posts.keys.first())
        }
    }

    @Synchronized
    fun remove(postId: String) {
        posts.remove(postId)
    }
}
