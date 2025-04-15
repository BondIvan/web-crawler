package com.crawler.web_crawler.parser;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ParseUtils {
    public static DateTimeFormatter[] getDateTimeFormatters() {
        return new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("ru")),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru")),
                DateTimeFormatter.ofPattern("dd MMMM yy", Locale.forLanguageTag("ru")),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru")),
                DateTimeFormatter.ofPattern("d MM yyyy"),
                DateTimeFormatter.ofPattern("d MM yy"),
                DateTimeFormatter.ofPattern("dd MM yy"),
                DateTimeFormatter.ofPattern("dd MM yyyy"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                DateTimeFormatter.ofPattern("dd.MM.yy"),
        };

    }
}
