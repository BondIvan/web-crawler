package com.crawler.web_crawler.parser;

import com.crawler.web_crawler.exception.JsoupException.JsoupClientException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JsoupClient {
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36";
    private static final int TIMEOUT = 10_000;

    public Document getPageByUrl(String url) throws JsoupClientException {
        try {
            return Jsoup.connect(url)
                    .timeout(TIMEOUT)
                    .userAgent(USER_AGENT)
                    .referrer("http://www.google.com")
                    .get();
        } catch (IOException e) {
            log.warn("Cannot access to this page: {}", url);
            throw new JsoupClientException("Cannot access to this page: " + url, e);
        }
    }

}
