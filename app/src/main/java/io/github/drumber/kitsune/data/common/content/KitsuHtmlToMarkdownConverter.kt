package io.github.drumber.kitsune.data.common.content

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode

/**
 * Converts Kitsu's server-rendered Kramdown HTML (`contentFormatted`) into GitHub-flavored
 * Markdown that can be fed into [com.mikepenz.markdown.m3.Markdown].
 *
 * This object is pure Kotlin with no Android dependencies, so it can be moved to `commonMain`
 * once the app is split into Kotlin Multiplatform source sets. HTML is parsed with
 * [Ksoup](https://github.com/fleeksoft/ksoup), a Kotlin Multiplatform port of jsoup.
 */
object KitsuHtmlToMarkdownConverter {

    /** Tags that always start a new Markdown block (as opposed to flowing inline text). */
    private val BLOCK_TAGS = setOf(
        "p", "div", "section", "article", "header", "footer", "main",
        "h1", "h2", "h3", "h4", "h5", "h6",
        "ul", "ol", "li", "blockquote", "pre", "hr", "table"
    )

    private val INLINE_BREAK_TAGS = setOf("br")

    /**
     * Markdown-significant punctuation characters. CommonMark allows any of these to be
     * backslash-escaped, which makes the renderer show the literal character instead of
     * interpreting it as syntax. Escaping every occurrence in plain text guarantees
     * server-rendered text can never be reinterpreted as emphasis, links, headings, lists, etc.
     */
    private const val ESCAPABLE_PUNCTUATION = "\\`*_[]()#+-.!~|<>"

    private val ESCAPE_REGEX = Regex("[${Regex.escape(ESCAPABLE_PUNCTUATION)}]")

    private val WHITESPACE_REGEX = Regex("\\s+")

    private val SUPERSCRIPT_CHARACTERS = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'e' to 'ᵉ',
        'f' to 'ᶠ', 'g' to 'ᵍ', 'h' to 'ʰ', 'i' to 'ⁱ', 'j' to 'ʲ',
        'k' to 'ᵏ', 'l' to 'ˡ', 'm' to 'ᵐ', 'n' to 'ⁿ', 'o' to 'ᵒ',
        'p' to 'ᵖ', 'r' to 'ʳ', 's' to 'ˢ', 't' to 'ᵗ', 'u' to 'ᵘ',
        'v' to 'ᵛ', 'w' to 'ʷ', 'x' to 'ˣ', 'y' to 'ʸ', 'z' to 'ᶻ',
        'A' to 'ᴬ', 'B' to 'ᴮ', 'D' to 'ᴰ', 'E' to 'ᴱ', 'G' to 'ᴳ',
        'H' to 'ᴴ', 'I' to 'ᴵ', 'J' to 'ᴶ', 'K' to 'ᴷ', 'L' to 'ᴸ',
        'M' to 'ᴹ', 'N' to 'ᴺ', 'O' to 'ᴼ', 'P' to 'ᴾ', 'R' to 'ᴿ',
        'T' to 'ᵀ', 'U' to 'ᵁ', 'V' to 'ⱽ', 'W' to 'ᵂ'
    )

    private val SUBSCRIPT_CHARACTERS = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ',
        'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ',
        'p' to 'ₚ', 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ',
        'v' to 'ᵥ', 'x' to 'ₓ'
    )

    /**
     * Converts [html] (Kitsu's `contentFormatted`) to Markdown. Returns an empty string for null
     * or blank input.
     */
    fun convert(html: String?): String {
        val source = html?.takeIf { it.isNotBlank() } ?: return ""
        val document = Ksoup.parse(source)
        val blocks = renderBlocks(document.body().childNodes())
        return blocks.joinToString("\n\n").trimEnd()
    }

    // region block-level rendering

    /**
     * Renders a sequence of sibling nodes into a list of Markdown block strings. Consecutive
     * inline/text nodes that are not wrapped in a block tag (e.g. bare top-level text) are
     * accumulated into a single loose paragraph block.
     */
    private fun renderBlocks(nodes: List<Node>): List<String> {
        val blocks = mutableListOf<String>()
        val loose = StringBuilder()

        fun flushLoose() {
            val text = loose.toString().trim()
            if (text.isNotEmpty()) blocks += text
            loose.clear()
        }

        for (node in nodes) {
            when {
                node is Element && node.tagName().lowercase() in BLOCK_TAGS -> {
                    flushLoose()
                    val rendered = renderBlockElement(node)
                    if (!rendered.isNullOrBlank()) blocks += rendered
                }

                node is TextNode -> loose.append(escapeText(node.text()))
                node is Element -> renderInline(node, loose)
                else -> Unit
            }
        }
        flushLoose()
        return blocks
    }

    private fun renderBlockElement(element: Element): String? {
        val tag = element.tagName().lowercase()
        return when {
            tag == "p" -> renderInlineChildren(element).trim().takeIf { it.isNotEmpty() }

            tag.length == 2 && tag[0] == 'h' && tag[1] in '1'..'6' -> {
                val text = renderInlineChildren(element).trim()
                if (text.isEmpty()) null else "#".repeat(tag[1] - '0') + " " + text
            }

            tag == "ul" -> renderList(element, ordered = false).takeIf { it.isNotEmpty() }
            tag == "ol" -> renderList(element, ordered = true).takeIf { it.isNotEmpty() }
            tag == "blockquote" -> renderBlockquote(element)
            tag == "pre" -> renderCodeBlock(element)
            tag == "hr" -> "---"

            // A stray <li> outside of a list (malformed HTML) - render as its own paragraph.
            tag == "li" -> renderBlocks(element.childNodes()).joinToString("\n\n").takeIf { it.isNotEmpty() }

            else -> {
                // Unknown/transparent block containers (div, section, table, etc.): recurse so
                // that safe child text/blocks are preserved instead of being discarded.
                val nested = renderBlocks(element.childNodes())
                nested.takeIf { it.isNotEmpty() }?.joinToString("\n\n")
            }
        }
    }

    private fun renderList(list: Element, ordered: Boolean): String {
        val items = mutableListOf<String>()
        var index = 1
        for (item in list.children()) {
            if (item.tagName().lowercase() != "li") continue
            val marker = if (ordered) "${index++}. " else "- "
            items += renderListItem(item, marker)
        }
        return items.joinToString("\n")
    }

    private fun renderListItem(li: Element, marker: String): String {
        val segments = renderListItemSegments(li)
        if (segments.isEmpty()) return marker.trimEnd()

        val combined = buildString {
            segments.forEachIndexed { i, (text, isNestedList) ->
                if (i > 0) append(if (isNestedList) "\n" else "\n\n")
                append(text)
            }
        }
        val indent = " ".repeat(marker.length)
        val lines = combined.split("\n")
        return buildString {
            lines.forEachIndexed { i, line ->
                if (i == 0) {
                    append(marker).append(line)
                } else {
                    append('\n')
                    append(if (line.isBlank()) line else indent + line)
                }
            }
        }
    }

    /**
     * Renders a list item's direct children into (text, isNestedList) segments. A nested
     * `<ul>`/`<ol>` is joined to the preceding text with a single newline (a "tight" sublist,
     * e.g. `- Parent\n  - Child`) instead of a blank line, matching conventional Markdown nested
     * list syntax; other block-level content (nested paragraphs, blockquotes, etc.) still gets a
     * blank line separator.
     */
    private fun renderListItemSegments(li: Element): List<Pair<String, Boolean>> {
        val segments = mutableListOf<Pair<String, Boolean>>()
        val loose = StringBuilder()

        fun flushLoose() {
            val text = loose.toString().trim()
            if (text.isNotEmpty()) segments += text to false
            loose.clear()
        }

        for (node in li.childNodes()) {
            when {
                node is Element && node.tagName().lowercase() in setOf("ul", "ol") -> {
                    flushLoose()
                    val rendered = renderList(node, ordered = node.tagName().lowercase() == "ol")
                    if (rendered.isNotEmpty()) segments += rendered to true
                }

                node is Element && node.tagName().lowercase() in BLOCK_TAGS -> {
                    flushLoose()
                    val rendered = renderBlockElement(node)
                    if (!rendered.isNullOrBlank()) segments += rendered to false
                }

                node is TextNode -> loose.append(escapeText(node.text()))
                node is Element -> renderInline(node, loose)
                else -> Unit
            }
        }
        flushLoose()
        return segments
    }

    private fun renderBlockquote(element: Element): String {
        val blocks = renderBlocks(element.childNodes())
        if (blocks.isEmpty()) return ">"
        val combined = blocks.joinToString("\n\n")
        return combined.split("\n").joinToString("\n") { line ->
            if (line.isBlank()) ">" else "> $line"
        }
    }

    private fun renderCodeBlock(pre: Element): String {
        val codeElement = pre.children().firstOrNull { it.tagName().lowercase() == "code" }
        val raw = rawText(codeElement ?: pre).trim('\n')
        val language = codeElement?.classNames()
            ?.firstOrNull { it.startsWith("language-") }
            ?.removePrefix("language-")
            .orEmpty()
        val fence = "`".repeat(maxOf(3, longestBacktickRun(raw) + 1))
        return "$fence$language\n$raw\n$fence"
    }

    private fun longestBacktickRun(text: String): Int =
        Regex("`+").findAll(text).maxOfOrNull { it.value.length } ?: 0

    /** Extracts raw (non-whitespace-normalized, non-Markdown-escaped) text, honoring `<br>`. */
    private fun rawText(node: Node): String = buildString { appendRawText(node, this) }

    private fun appendRawText(node: Node, sb: StringBuilder) {
        when (node) {
            is TextNode -> sb.append(node.getWholeText())
            is Element -> {
                if (node.tagName().lowercase() in INLINE_BREAK_TAGS) {
                    sb.append('\n')
                } else {
                    for (child in node.childNodes()) appendRawText(child, sb)
                }
            }

            else -> Unit
        }
    }

    // endregion

    // region inline rendering

    private fun renderInlineChildren(element: Element): String = buildString {
        for (child in element.childNodes()) {
            when (child) {
                is TextNode -> append(escapeText(child.text()))
                is Element -> renderInline(child, this)
                else -> Unit
            }
        }
    }

    private fun renderInline(element: Element, sb: StringBuilder) {
        val tag = element.tagName().lowercase()
        when (tag) {
            "strong", "b" -> wrapInline(element, sb, "**")
            "em", "i" -> wrapInline(element, sb, "_")
            "del", "s", "strike" -> wrapInline(element, sb, "~~")
            "sup" -> renderScript(element, sb, SUPERSCRIPT_CHARACTERS)
            "sub" -> renderScript(element, sb, SUBSCRIPT_CHARACTERS)
            "code" -> renderInlineCode(element, sb)
            "br" -> sb.append("  \n")
            "a" -> renderLink(element, sb)
            "img" -> renderImage(element, sb)
            else -> {
                // Unknown inline tags (span, u, sup, sub, etc.): drop the tag but keep its
                // rendered child content so text is never silently discarded.
                for (child in element.childNodes()) {
                    when (child) {
                        is TextNode -> sb.append(escapeText(child.text()))
                        is Element -> renderInline(child, sb)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun wrapInline(element: Element, sb: StringBuilder, marker: String) {
        val inner = renderInlineChildren(element)
        appendWrappedInline(sb, inner, marker, marker)
    }

    private fun renderScript(
        element: Element,
        sb: StringBuilder,
        characterMap: Map<Char, Char>
    ) {
        val inner = renderInlineChildren(element)
        for (character in inner) {
            sb.append(characterMap[character] ?: character)
        }
    }

    private fun renderInlineCode(element: Element, sb: StringBuilder) {
        // rawText already collapses <br> to \n; normalize any remaining whitespace runs to a
        // single space, matching how browsers render inline <code> content.
        val normalized = WHITESPACE_REGEX.replace(rawText(element).replace('\n', ' '), " ").trim()
        if (normalized.isEmpty()) return
        val fence = "`".repeat(maxOf(1, longestBacktickRun(normalized) + 1))
        val padding = if (normalized.startsWith("`") || normalized.endsWith("`")) " " else ""
        sb.append(fence).append(padding).append(normalized).append(padding).append(fence)
    }

    private fun renderLink(element: Element, sb: StringBuilder) {
        // Kitsu auto-embeds images as `<a href=X><img class=autoembed src=X /></a>`. Render as a
        // single image instead of nesting a linked image.
        val onlyChild = element.childNodes().singleOrNullSignificant()
        if (onlyChild is Element && onlyChild.tagName().lowercase() == "img") {
            renderImage(onlyChild, sb)
            return
        }

        val href = element.attr("href")
        val text = renderInlineChildren(element).ifEmpty { escapeText(href) }
        if (href.isEmpty()) {
            sb.append(text)
            return
        }
        appendWrappedInline(sb, text, "[", "](${formatUrl(href)})")
    }

    private fun renderImage(element: Element, sb: StringBuilder) {
        val src = element.attr("src")
        if (src.isEmpty()) return
        val alt = escapeText(element.attr("alt"))
        sb.append("![").append(alt).append("](").append(formatUrl(src)).append(')')
    }

    /** Wraps the URL in angle brackets if it contains characters unsafe for `(...)` syntax. */
    private fun formatUrl(url: String): String {
        return if (url.any { it == ' ' || it == '(' || it == ')' }) "<$url>" else url
    }

    /** Returns the single significant (non-blank) child node, if there is exactly one, else null. */
    private fun List<Node>.singleOrNullSignificant(): Node? {
        val significant = filter { it !is TextNode || it.text().isNotBlank() }
        return significant.singleOrNull()
    }

    /**
     * CommonMark delimiters cannot open or close next to whitespace. HTML has no such restriction,
     * so move collapsible edge whitespace outside generated emphasis/link delimiters.
     */
    private fun appendWrappedInline(
        sb: StringBuilder,
        content: String,
        opening: String,
        closing: String
    ) {
        if (content.isEmpty()) return

        val firstContentIndex = content.indexOfFirst { !it.isWhitespace() }
        if (firstContentIndex == -1) {
            sb.append(' ')
            return
        }
        val lastContentIndex = content.indexOfLast { !it.isWhitespace() }

        if (firstContentIndex > 0) sb.append(' ')
        sb.append(opening)
            .append(content, firstContentIndex, lastContentIndex + 1)
            .append(closing)
        if (lastContentIndex < content.lastIndex) sb.append(' ')
    }

    // endregion

    private fun escapeText(text: String): String {
        val normalized = WHITESPACE_REGEX.replace(text, " ")
        return ESCAPE_REGEX.replace(normalized) { "\\" + it.value }
    }
}
