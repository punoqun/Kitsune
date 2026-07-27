package io.github.drumber.kitsune.data.common.content

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class KitsuHtmlToMarkdownConverterTest {

    @Test
    fun `blank or null input returns empty string`() {
        assertThat(KitsuHtmlToMarkdownConverter.convert(null)).isEmpty()
        assertThat(KitsuHtmlToMarkdownConverter.convert("")).isEmpty()
        assertThat(KitsuHtmlToMarkdownConverter.convert("   ")).isEmpty()
    }

    @Test
    fun `bare top-level text without any tags is preserved`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("Just plain text")

        assertThat(markdown).isEqualTo("Just plain text")
    }

    @Test
    fun `html entities are decoded and smart punctuation is preserved`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert(
            "<p>It&rsquo;s &ldquo;great&rdquo; &amp; fun &mdash; really</p>"
        )

        assertThat(markdown).isEqualTo("It’s “great” & fun — really")
    }

    @Test
    fun `paragraphs are separated by a blank line`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("<p>First</p><p>Second</p>")

        assertThat(markdown).isEqualTo("First\n\nSecond")
    }

    @Test
    fun `br produces a hard line break within a paragraph`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("<p>Line one<br>Line two</p>")

        assertThat(markdown).isEqualTo("Line one  \nLine two")
    }

    @Test
    fun `bold and strong render as double asterisks`() {
        assertThat(KitsuHtmlToMarkdownConverter.convert("<p><strong>bold</strong></p>"))
            .isEqualTo("**bold**")
        assertThat(KitsuHtmlToMarkdownConverter.convert("<p><b>bold</b></p>"))
            .isEqualTo("**bold**")
    }

    @Test
    fun `italic and emphasis render with underscores`() {
        assertThat(KitsuHtmlToMarkdownConverter.convert("<p><em>italic</em></p>"))
            .isEqualTo("_italic_")
        assertThat(KitsuHtmlToMarkdownConverter.convert("<p><i>italic</i></p>"))
            .isEqualTo("_italic_")
    }

    @Test
    fun `strikethrough renders with double tildes`() {
        assertThat(KitsuHtmlToMarkdownConverter.convert("<p><del>gone</del></p>"))
            .isEqualTo("~~gone~~")
        assertThat(KitsuHtmlToMarkdownConverter.convert("<p><s>gone</s></p>"))
            .isEqualTo("~~gone~~")
    }

    @Test
    fun `links render as markdown links`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert(
            """<p>See <a href="https://kitsu.app" target="_blank" rel="noopener">Kitsu</a></p>"""
        )

        assertThat(markdown).isEqualTo("See [Kitsu](https://kitsu.app)")
    }

    @Test
    fun `bare urls already wrapped by kitsu render as links`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert(
            """<p><a href="https://kitsu.app/anime">https://kitsu.app/anime</a></p>"""
        )

        assertThat(markdown).isEqualTo("[https://kitsu\\.app/anime](https://kitsu.app/anime)")
    }

    @Test
    fun `inline image renders as markdown image`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert(
            """<p><img src="https://kitsu.app/image.png" alt="a cat" /></p>"""
        )

        assertThat(markdown).isEqualTo("![a cat](https://kitsu.app/image.png)")
    }

    @Test
    fun `kitsu autoembed linked image collapses to a single image`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert(
            """<p><a href="https://kitsu.app/image.png"><img class="autoembed" src="https://kitsu.app/image.png" style="max-width:100%" /></a></p>"""
        )

        assertThat(markdown).isEqualTo("![](https://kitsu.app/image.png)")
    }

    @Test
    fun `headings render with matching number of hashes`() {
        assertThat(KitsuHtmlToMarkdownConverter.convert("<h1>Title</h1>")).isEqualTo("# Title")
        assertThat(KitsuHtmlToMarkdownConverter.convert("<h3>Subtitle</h3>")).isEqualTo("### Subtitle")
    }

    @Test
    fun `unordered list renders with dash markers`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("<ul><li>One</li><li>Two</li></ul>")

        assertThat(markdown).isEqualTo("- One\n- Two")
    }

    @Test
    fun `ordered list renders with incrementing numbers`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("<ol><li>One</li><li>Two</li></ol>")

        assertThat(markdown).isEqualTo("1. One\n2. Two")
    }

    @Test
    fun `nested list is indented under its parent item`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert(
            "<ul><li>Parent<ul><li>Child</li></ul></li></ul>"
        )

        assertThat(markdown).isEqualTo("- Parent\n  - Child")
    }

    @Test
    fun `blockquote is prefixed with angle bracket`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("<blockquote><p>Quoted text</p></blockquote>")

        assertThat(markdown).isEqualTo("> Quoted text")
    }

    @Test
    fun `inline code renders with backticks`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("<p>Use <code>val x = 1</code> here</p>")

        assertThat(markdown).isEqualTo("Use `val x = 1` here")
    }

    @Test
    fun `fenced code block preserves whitespace and is not escaped`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert(
            "<pre><code class=\"language-kotlin\">fun main() {\n    print(\"*hi*\")\n}</code></pre>"
        )

        assertThat(markdown).isEqualTo(
            "```kotlin\nfun main() {\n    print(\"*hi*\")\n}\n```"
        )
    }

    @Test
    fun `trailing whitespace is trimmed`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("<p>Hello</p>\n\n   ")

        assertThat(markdown).isEqualTo("Hello")
    }

    @Test
    fun `markdown-significant characters in plain text are escaped`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("<p>1 * 2 = 2, _really_ #1 [not a link]</p>")

        assertThat(markdown).isEqualTo("1 \\* 2 = 2, \\_really\\_ \\#1 \\[not a link\\]")
    }

    @Test
    fun `unknown tags preserve safe child text instead of discarding it`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert(
            """<p>Hello <span class="mention">world</span> and <u>underline</u></p>"""
        )

        assertThat(markdown).isEqualTo("Hello world and underline")
    }

    @Test
    fun `no raw html is emitted for unsupported tags`() {
        val markdown = KitsuHtmlToMarkdownConverter.convert("<p>Hello <video src=\"movie.mp4\"></video></p>")

        assertThat(markdown).doesNotContain("<video")
        assertThat(markdown).doesNotContain("</video>")
    }

    @Test
    fun `real kitsu fixture with mixed formatting converts as expected`() {
        val html = """
            <p>Just finished watching this season and <strong>wow</strong>, what a ride!
            Check it out: <a href="https://kitsu.app/anime/example" target="_blank" rel="noopener">https://kitsu.app/anime/example</a></p>
            <p><a href="https://media.kitsu.app/example.jpg"><img class="autoembed" src="https://media.kitsu.app/example.jpg" style="max-width:100%;" /></a></p>
        """.trimIndent()

        val markdown = KitsuHtmlToMarkdownConverter.convert(html)

        assertThat(markdown).isEqualTo(
            "Just finished watching this season and **wow**, what a ride\\! " +
                "Check it out: [https://kitsu\\.app/anime/example](https://kitsu.app/anime/example)" +
                "\n\n![](https://media.kitsu.app/example.jpg)"
        )
    }
}
