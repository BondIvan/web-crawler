package com.crawler.web_crawler.parser;

import com.crawler.web_crawler.exception.JsoupException;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JsoupParser implements Parser {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36";

    @Override
    public List<NewsArticle> parse(Source source) {
        log.info("Start scanning {} for new articles", source.getUrl());
        String url = source.getUrl();
        Map<String, String> selectors = source.getSelectors(); // blockWithArticle, title, content, date

        Document document = getCurrentPage(url);
        //TODO Add scan other pages

        List<NewsArticle> articles = new ArrayList<>();
        Elements elementsFromPageByClass = document.getElementsByClass(selectors.get("block"));

        if(elementsFromPageByClass.isEmpty()) {
            log.warn("Cannot find news block with this selector: {}", selectors.get("block"));
            throw new JsoupException("Cannot find news block with this selector: " + selectors.get("block"));
        }

        checkSelectors(elementsFromPageByClass.get(0), selectors);

        for(Element block: elementsFromPageByClass) {
            NewsArticle article = new NewsArticle();

            Element title = block.selectFirst(selectors.get("title"));
            Element content = block.selectFirst(selectors.get("content"));
            Element publishDate = block.selectFirst(selectors.get("date"));

            article.setSource(source);
            article.setTitle(title.text());
            article.setContent(content.text());
            article.setPublishDate(getDateFromString(publishDate.text()));

            article.generateHash();

            articles.add(article);
        }
        log.info("Stop scanning {}", source.getUrl());

        return articles;
    }

    private void checkSelectors(Element block, Map<String, String> selectors) {
        if(block.selectFirst(selectors.get("title")) == null) {
            log.error("Cannot find selector for title: {}", selectors.get("title"));
            throw new JsoupException("Cannot find selector for title: " + selectors.get("title"));
        }

        if(block.selectFirst(selectors.get("content")) == null) {
            log.error("Cannot find selector for content: {}", selectors.get("content"));
            throw new JsoupException("Cannot find selector for content: " + selectors.get("content"));
        }

        if(block.selectFirst(selectors.get("date")) == null) {
            log.error("Cannot find selector for date: {}", selectors.get("date"));
            throw new JsoupException("Cannot find selector for date: " + selectors.get("date"));
        }
    }

    private LocalDateTime getDateFromString(String date) {
        date = date.trim();

        if(date.startsWith("Сегодня")) {
            String timePart = date.replace("Сегодня, ", "");
            LocalTime time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(LocalDate.now(), time);
        }

        if(date.startsWith("Вчера")) {
            String timePart = date.replace("Вчера, ", "");
            LocalTime time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(LocalDate.now().minusDays(1), time);
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm");
            return LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            log.warn("Failed to set date");
            return null;
        }
    }

    private Document getCurrentPage(String url) {
        try {
            return Jsoup.connect(url)
                    .timeout(10_000)
                    .userAgent(USER_AGENT)
                    .referrer("http://www.google.com")
                    .get();
        } catch (IOException e) {
            throw new JsoupException("Cannot access to this page: " + url, e);
        }
    }

}

