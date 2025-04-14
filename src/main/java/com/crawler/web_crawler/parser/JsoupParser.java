package com.crawler.web_crawler.parser;

import com.crawler.web_crawler.exception.JsoupParseException;
import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class JsoupParser implements Parser {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36";
    public static final String SELECTOR_BLOCK = "";
    public static final String SELECTOR_TITLE = "";
    public static final String SELECTOR_CONTENT = "";
    public static final String SELECTOR_DATE = "";
    public static final String SELECTOR_DATE_PATTERN = "";

    @Override
    public List<NewsArticle> parse(Source source) {
        log.info("Start scanning {} for new articles", source.getUrl());
        String url = source.getUrl();
        Map<String, String> selectors = source.getSelectors(); // blockWithArticle, title, content, date, datePattern

        Document document = getCurrentPage(url);
        //TODO Add scan other pages
        //TODO Сделать отдельную странцу для тестирования css-селекторов

        List<NewsArticle> articles = new ArrayList<>();
        Elements elementsFromPageByClass = document.select(selectors.get("block"));

        if(elementsFromPageByClass.isEmpty()) {
            log.warn("Cannot find news block with this selector: {}", selectors.get("block"));
            throw new JsoupParseException("Cannot find news block with this selector: " + selectors.get("block"));
        }

        for(Element block: elementsFromPageByClass) {
            NewsArticle article = new NewsArticle();

            Element title = block.selectFirst(selectors.get("title"));
            Element content = block.selectFirst(selectors.get("content"));
            Element publishDate = block.selectFirst(selectors.get("date"));

            if(title != null) {
                article.setTitle(title.text());
            } else {
                log.warn("Cannot find selector for title: {}", selectors.get("title"));
            }

            if(content != null) {
                article.setContent(content.text());
            } else {
                log.warn("Cannot find selector for content: {}", selectors.get("content"));
            }

            if(publishDate != null) {
                LocalDate localDate = getDateFromString(publishDate.text(), selectors.get("datePattern"));
                article.setPublishDate(localDate);
            } else {
                log.warn("Cannot find selector for date: {}", selectors.get("date"));
            }

            article.setSource(source);
            article.generateHash();

            articles.add(article);
        }
        log.info("Stop scanning {}", source.getUrl());

        return articles;
    }

    private LocalDate getDateFromString(String date, String pattern) {
        date = date.toLowerCase().trim();
        pattern = pattern.trim();

        int offset = getOffsetIfAdverbTimeExist(date);
        if(offset <= 0)
            return LocalDate.now().plusDays(offset);

        if(!pattern.contains("yy")) {
            date = date + " " + LocalDate.now().getYear();
            pattern = pattern + " yyyy";
        }

        Pattern regex = Pattern.compile(patternToRegex(pattern));
        Matcher matcher = regex.matcher(date);

        if(matcher.find())
            date = matcher.group();
        else {
            log.warn("The pattern does not match the date type");
            return null;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("ru"));
        return LocalDate.parse(date, dateTimeFormatter);
    }

    private int getOffsetIfAdverbTimeExist(String date) {
        record DayAndOffset(
                String name,
                int offset
        ) {}
        DayAndOffset ruToday = new DayAndOffset("сегодня", 0);
        DayAndOffset ruYesterday = new DayAndOffset("вчера", -1);
        List<DayAndOffset> daysWithOffset = List.of(ruToday, ruYesterday);

        for(DayAndOffset landDayAndOffset: daysWithOffset) {
            String name = landDayAndOffset.name();
            int offset = landDayAndOffset.offset();
            int index = date.indexOf(name);
            if(index >= 0) {
                return offset;
            }
        }

        return 1;
    }

    private String patternToRegex(String pattern) {
        return pattern
                .replace("dd", "(0[1-9]|[12][0-9]|3[01])") // Day with 0
                .replace("d", "([1-9]|[12][0-9]|3[01])") // Day without 0
                .replace("MMMM", "[А-Яа-яЁё]+") // Full name of month (ru)
                .replace("MM", "(0[1-9]|1[0-2])") // Month with 0
                .replace("yyyy", "\\d{4}") // Four digit year
                .replace("yy", "\\d{2}"); // Two digit year
    }

    private Document getCurrentPage(String url) {
        try {
            return Jsoup.connect(url)
                    .timeout(10_000)
                    .userAgent(USER_AGENT)
                    .referrer("http://www.google.com")
                    .get();
        } catch (IOException e) {
            throw new JsoupParseException("Cannot access to this page: " + url, e);
        }
    }

}

