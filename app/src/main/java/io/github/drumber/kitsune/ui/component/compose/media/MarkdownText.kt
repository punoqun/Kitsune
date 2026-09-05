package io.github.drumber.kitsune.ui.component.compose.media

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.extendedspans.ExtendedSpans
import com.mikepenz.markdown.compose.extendedspans.RoundedCornerSpanPainter
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownExtendedSpans
import com.mikepenz.markdown.model.markdownPadding
import io.github.drumber.kitsune.data.common.content.KitsuHtmlToMarkdownConverter
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * Renders Kitsu post/comment content as Compose-native Markdown.
 *
 * Published content uses Kitsu's server-rendered Kramdown HTML ([contentFormatted]), which is
 * converted to Markdown (see [KitsuHtmlToMarkdownConverter]) so it matches the formatting shown on
 * the website while staying fully Compose/KMP-portable (no Android HTML APIs). Raw Markdown
 * ([content]) is rendered for previews and as a fallback when formatted content is unavailable or
 * converts to nothing renderable.
 *
 * @param content The original Markdown source.
 * @param contentFormatted The server-rendered HTML, when available.
 */
@Composable
fun MarkdownText(
    modifier: Modifier = Modifier,
    content: String?,
    contentFormatted: String? = null
) {
    val source = remember(content, contentFormatted) {
        val convertedFormatted = contentFormatted
            ?.takeIf { it.isNotBlank() }
            ?.let { KitsuHtmlToMarkdownConverter.convert(it) }
            ?.takeIf { it.isNotBlank() }
        convertedFormatted ?: content?.takeIf { it.isNotBlank() }
    } ?: return

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val bodyStyle = typography.bodyMedium

    Markdown(
        content = source,
        modifier = modifier,
        colors = markdownColor(
            text = colorScheme.onSurface,
            codeBackground = colorScheme.onSurface.copy(alpha = 0.08f),
            inlineCodeBackground = colorScheme.onSurface.copy(alpha = 0.08f),
            dividerColor = colorScheme.outlineVariant,
            tableBackground = colorScheme.onSurface.copy(alpha = 0.04f)
        ),
        typography = markdownTypography(
            h1 = typography.headlineLarge,
            h2 = typography.headlineMedium,
            h3 = typography.headlineSmall,
            h4 = typography.titleLarge,
            h5 = typography.titleMedium,
            h6 = typography.titleSmall,
            text = bodyStyle,
            code = bodyStyle.copy(
                color = colorScheme.primary,
                fontFamily = FontFamily.Monospace
            ),
            inlineCode = bodyStyle.copy(
                color = colorScheme.primary,
                fontFamily = FontFamily.Monospace
            ),
            quote = bodyStyle.copy(
                color = colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            ),
            paragraph = bodyStyle,
            ordered = bodyStyle,
            bullet = bodyStyle,
            list = bodyStyle,
            textLink = TextLinkStyles(
                style = SpanStyle(color = colorScheme.primary)
            ),
            table = bodyStyle
        ),
        padding = markdownPadding(
            block = 6.dp,
            listItemTop = 2.dp,
            listItemBottom = 2.dp,
            codeBlock = PaddingValues(10.dp)
        ),
        dimens = markdownDimens(
            codeBackgroundCornerSize = 6.dp
        ),
        extendedSpans = markdownExtendedSpans {
            ExtendedSpans(
                RoundedCornerSpanPainter(
                    cornerRadius = 4.sp,
                    padding = RoundedCornerSpanPainter.TextPaddingValues(
                        horizontal = 4.sp,
                        vertical = 1.sp
                    )
                )
            )
        },
        imageTransformer = Coil3ImageTransformerImpl
    )
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextHtmlPreview() {
    KitsuneTheme {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            content = "This is **bold** and _italic_ Markdown.",
            contentFormatted = """
                <p>This is <strong>bold</strong> and <em>italic</em> with a
                <a href="https://kitsu.app">link</a>.</p>
            """.trimIndent()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextRawPreview() {
    KitsuneTheme {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            content = "This is **bold** and _italic_ Markdown with a [link](https://kitsu.app)."
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextNullPreview() {
    KitsuneTheme {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            content = null
        )
    }
}
