package com.crawler.web_crawler.exception.JsoupException;

public class JsoupClientException extends Exception {
    public JsoupClientException(String message) {
        super(message);
    }

    public JsoupClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
