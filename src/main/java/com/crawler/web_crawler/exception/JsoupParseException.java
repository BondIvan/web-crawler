package com.crawler.web_crawler.exception;

public class JsoupParseException extends RuntimeException {
    public JsoupParseException(String message) {
        super(message);
    }

    public JsoupParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
