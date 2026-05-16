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
                .replaceAll("!\\[.*?\\]\\(.*?\\)", "")
                .replaceAll("\\{#[^}]+\\}", "")
                .replaceAll("(?m)^https://sourcecraft\\.dev/\\s*$", "")
                .replaceAll("<br\\s*/?>", "")
                .replaceAll("\n{3,}", "\n\n")
                .strip();
    }
}
