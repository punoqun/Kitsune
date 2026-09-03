fun main() {
    val ESCAPABLE_PUNCTUATION = "\\`*_[]()#+-.!~|<>"
    val escaped = Regex.escape(ESCAPABLE_PUNCTUATION)
    println("Regex.escape result: $escaped")
    val pattern = "[$escaped]"
    println("Pattern: $pattern")
    val regex = Regex(pattern)
    val testStr = "1 * 2 = 2, _really_ #1 [not a link] and a-b (c) `d` \\e"
    println(regex.replace(testStr) { "\\" + it.value })
}
