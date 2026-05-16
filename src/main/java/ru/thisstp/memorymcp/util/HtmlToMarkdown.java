package ru.thisstp.memorymcp.util;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HtmlToMarkdown {

    private static final FlexmarkHtmlConverter converter;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false);
        converter = FlexmarkHtmlConverter.builder(options).build();
    }

    public static String convert(String html) {
        if (html == null || html.isBlank()) return "";

        String markdown = converter.convert(html);

        return markdown
                .replaceAll("!\\[.*?\\]\\(.*?\\)", "")            // strip images
                .replaceAll("\\{#[^}]+\\}", "")                    // strip {#anchors}
                .replaceAll("(?m)^https://sourcecraft\\.dev/\\s*$", "")
                .replaceAll("<br\\s*/?>", "")
                .replace("\\<\\<", "«")                            // guillemets (must be before single \< \>)
                .replace("\\>\\>", "»")
                .replace("\\<", "<")                               // unescape leftover \<  → <
                .replace("\\>", ">")                               // unescape leftover \>  → >
                .replace("\\&", "&")                               // unescape ampersand
                .replace("\\~", "~")                               // unescape tilde
                .replaceAll("\\[\\s*]\\([^)]*\\)", "")             // strip empty [](url)
                .replaceAll("(?<=\\s)---(?=\\s)", "—")             // em-dash (--- between spaces)
                .replaceAll("(?<=\\s)--(?=\\s)", "—")              // em-dash (-- between spaces)
                .replaceAll("\n{3,}", "\n\n")
                .strip();
    }
}
