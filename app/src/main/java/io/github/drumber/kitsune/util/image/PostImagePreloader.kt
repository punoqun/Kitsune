package io.github.drumber.kitsune.util.image

import android.content.Context
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import io.github.drumber.kitsune.data.presentation.model.feed.Post

/**
 * Warms Coil's shared cache for images in a feed page before their cards enter the viewport.
 */
class PostImagePreloader(
    private val context: Context,
    private val imageLoader: ImageLoader
) {

    fun preload(posts: List<Post>) {
        posts.forEach { post ->
            preload(post.authorAvatarUrl, AVATAR_SIZE)
            preload(post.mediaPosterUrl, POSTER_SIZE)
            post.imageUrls.take(MAX_PREVIEW_IMAGES).forEach { preload(it, CONTENT_IMAGE_SIZE) }
            preload(post.embed?.imageUrl, CONTENT_IMAGE_SIZE)
        }
    }

    suspend fun preloadAvatars(urls: List<String>) {
        urls.forEach { url ->
            imageLoader.execute(request(url, AVATAR_SIZE))
        }
    }

    private fun preload(url: String?, size: Size) {
        if (url.isNullOrBlank()) return
        imageLoader.enqueue(request(url, size))
    }

    private fun request(url: String, size: Size) = ImageRequest.Builder(context)
        .data(url)
        .size(size)
        .build()

    private companion object {
        val AVATAR_SIZE = Size(96, 96)
        val POSTER_SIZE = Size(180, 260)
        val CONTENT_IMAGE_SIZE = Size(960, 540)
        const val MAX_PREVIEW_IMAGES = 4
    }
}
