import java.util.regex.Pattern;

public class TestEscape {
    public static void main(String[] args) {
        String ESCAPABLE_PUNCTUATION = "\\`*_[]()#+-.!~|<>";
        String escaped = Pattern.quote(ESCAPABLE_PUNCTUATION);
        System.out.println("Quoted: " + escaped);
        String patternStr = "[" + escaped + "]";
        System.out.println("Pattern: " + patternStr);
        Pattern p = Pattern.compile(patternStr);
        String testStr = "1 * 2 = 2, a-b (c) test";
        java.util.regex.Matcher m = p.matcher(testStr);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "\\\\" + m.group());
        }
        m.appendTail(sb);
        System.out.println("Result: " + sb.toString());
    }
}
