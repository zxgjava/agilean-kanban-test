package cn.agilean.cucumber;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.StringUtils;

import com.google.common.base.Splitter;

/**
 * 尝试用多种常见的分隔符将一个多行文本分割成 HashMap&lt;String,String&gt;.
 *
 * <p>
 *
 *
 * @author Alex Lei.
 *
 */
public class KeyValueParser {

    String Delimiter = "=:|\t： ";

    public void setDelimiter(String delimiter) {
        Delimiter = delimiter;
    }

    public Map<String, String> parse(String text) {
        List<String> lines = Splitter.onPattern("\\r|\\n").trimResults()
                .omitEmptyStrings().splitToList(text);
        // 判断分隔符，每个字符都试一遍，直到找到每行都有的。
        char[] delimiters = this.Delimiter.toCharArray();
        char use = 0;
        for (char c : delimiters) {
            boolean allLineContains = true;
            for (String line : lines) {
                if (line.indexOf(c) <= 0) {
                    allLineContains = false;
                    break;
                }
            }
            if (allLineContains) {
                use = c;
                break;
            }
        }
        if (use > 0) {
            Properties properties = StringUtils
                    .splitArrayElementsIntoProperties(
                            (String[]) lines.toArray(new String[lines.size()]),
                            String.valueOf(use));
            return (Map) properties;
        }

        throw new RuntimeException("没有找到合适的分隔符。");
    }
}
