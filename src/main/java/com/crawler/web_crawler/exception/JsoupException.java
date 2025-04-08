package com.crawler.web_crawler.exception;

public class JsoupException extends RuntimeException {
    public JsoupException(String message) {
        super(message);
    }

    public JsoupException(String message, Throwable cause) {
        super(message, cause);
    }
}
