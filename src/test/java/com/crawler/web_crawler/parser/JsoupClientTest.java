package com.crawler.web_crawler.parser;

import com.crawler.web_crawler.exception.JsoupException.JsoupClientException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsoupClientTest {
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36";
    private static final int TIMEOUT = 10_000;

    private JsoupClient underTest;

    @BeforeEach
    void setUp() {
        underTest = new JsoupClient();
    }

    @Test
    void getPageByUrl_whenUrlCorrect_shouldReturnHtmlDocument() throws Exception {
        try(MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            // Given
            String correctUrl = "https://www.google.com/";
            Connection connection = mock(Connection.class);
            Document document = mock(Document.class);

            mockedJsoup.when(() -> Jsoup.connect(correctUrl)).thenReturn(connection);
            when(connection.timeout(TIMEOUT)).thenReturn(connection);
            when(connection.userAgent(USER_AGENT)).thenReturn(connection);
            when(connection.referrer("http://www.google.com")).thenReturn(connection);
            when(connection.get()).thenReturn(document);

            // When
            Document resultDocument = underTest.getPageByUrl(correctUrl);

            // Then
            assertThat(resultDocument).isSameAs(document);

            verify(connection).timeout(TIMEOUT);
            verify(connection).userAgent(USER_AGENT);
            verify(connection).referrer("http://www.google.com");
            verify(connection).get();
        }
    }

    @Test
    void getPageByUrl_whenUrlIncorrect_shouldReturnException() throws Exception {
        try(MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            // Given
            String wrongUrl = "https://www.google";
            Connection connection = mock(Connection.class);
            IOException ioException = new IOException("Unknown host");

            mockedJsoup.when(() -> Jsoup.connect(wrongUrl)).thenReturn(connection);
            when(connection.timeout(anyInt())).thenReturn(connection);
            when(connection.userAgent(anyString())).thenReturn(connection);
            when(connection.referrer(anyString())).thenReturn(connection);
            when(connection.get()).thenThrow(ioException);

            // When
            JsoupClientException exception = assertThrows(
                    JsoupClientException.class,
                    () -> underTest.getPageByUrl(wrongUrl)
            );

            // Then
            assertEquals("Cannot access to this page: " + wrongUrl, exception.getMessage());
            assertThat(exception).hasCauseExactlyInstanceOf(IOException.class);
        }


    }
}