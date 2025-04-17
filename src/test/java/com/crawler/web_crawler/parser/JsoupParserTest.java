package com.crawler.web_crawler.parser;

import com.crawler.web_crawler.exception.JsoupException.JsoupClientException;
import com.crawler.web_crawler.exception.JsoupException.JsoupParseException;
import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsoupParserTest {
    @Mock
    private JsoupClient jsoupClient;
    @Mock
    private DateParser dateParser;
    @InjectMocks
    private JsoupParser underTest;

    private Source source;
    private final String selectorTitle = JsoupParser.SELECTOR_TITLE;
    private final String selectorContent = JsoupParser.SELECTOR_CONTENT;
    private final String selectorDate = JsoupParser.SELECTOR_DATE;

    @BeforeEach
    void setUp() {
        source = new Source();
        source.setUrl("https://google.com");
        source.setSelectors(Map.of(
                selectorTitle, "css-title",
                selectorContent, "css-content",
                selectorDate, "css-date"
        ));
    }

    @Test
    void parse_whenParseSuccessful_shouldReturnListWithNewsArticles() throws Exception {
        // Given
        Document document = mock(Document.class);
        String url = source.getUrl();
        Map<String, String> selectors = source.getSelectors();

        when(jsoupClient.getPageByUrl(url)).thenReturn(document);

        Elements titleElements = createElements("Title1", "Title2", "Title3");
        Elements contentElements = createElements("Content1", "Content2", "Content3");
        Elements publishDateElements = createElements("01.01.2025", "02.01.2025", "03.01.2025");

        when(document.select(selectors.get(selectorTitle))).thenReturn(titleElements);
        when(document.select(selectors.get(selectorContent))).thenReturn(contentElements);
        when(document.select(selectors.get(selectorDate))).thenReturn(publishDateElements);

        Optional<LocalDate> optionalLocalDate = Optional.of(LocalDate.of(2025, 1, 1));
        Optional<LocalDate> optionalLocalDate2 = Optional.of(LocalDate.of(2025, 1, 2));
        Optional<LocalDate> optionalLocalDate3 = Optional.of(LocalDate.of(2025, 1, 3));

        when(dateParser.toLocalDateFromString(publishDateElements.get(0).text())).thenReturn(optionalLocalDate);
        when(dateParser.toLocalDateFromString(publishDateElements.get(1).text())).thenReturn(optionalLocalDate2);
        when(dateParser.toLocalDateFromString(publishDateElements.get(2).text())).thenReturn(optionalLocalDate3);

        // When
        List<NewsArticle> articleList = underTest.parse(source);

        // Then
        NewsArticle checkArticle = articleList.get(0);
        assertEquals("Title1", checkArticle.getTitle());
        assertEquals("Content1", checkArticle.getContent());
        assertEquals(LocalDate.of(2025, 1,1), checkArticle.getPublishDate());
        assertThat(source).isSameAs(checkArticle.getSource());
        assertNotNull(checkArticle.getHash());

        assertThat(articleList).hasSize(3);

        verify(jsoupClient).getPageByUrl(url);
        verify(dateParser, times(3)).toLocalDateFromString(anyString());
    }

    @Test
    void parse_whenThrownJsoupClientException_shouldReturnJsoupParseException() throws Exception {
        // Given
        String wrongUrl = source.getUrl();
        String errorMessage = "Cannot access to this page: " + wrongUrl;

        when(jsoupClient.getPageByUrl(anyString())).thenThrow(new JsoupClientException(errorMessage));

        // When
        JsoupParseException exception = assertThrows(
                JsoupParseException.class,
                () -> underTest.parse(source)
        );

        // Then
        assertEquals("Cannot access to this page: " + wrongUrl, exception.getMessage());
        assertThat(exception).hasCauseExactlyInstanceOf(JsoupClientException.class);
    }

    @Test
    void parse_whenTitleAndDateCountDoesntMatch_shouldReturnException() throws Exception {
        // Given
        Document document = mock(Document.class);
        String url = source.getUrl();
        Map<String, String> selectors = source.getSelectors();

        when(jsoupClient.getPageByUrl(url)).thenReturn(document);

        Elements titleElements = createElements("Title1", "Title2", "Title3");
        Elements contentElements = createElements("Content1", "Content2", "Content3");
        Elements publishDateElements = createElements("01.01.2025", "02.01.2025");

        when(document.select(selectors.get(selectorTitle))).thenReturn(titleElements);
        when(document.select(selectors.get(selectorContent))).thenReturn(contentElements);
        when(document.select(selectors.get(selectorDate))).thenReturn(publishDateElements);

        // When
        JsoupParseException exception = assertThrows(
                JsoupParseException.class,
                () -> underTest.parse(source)
        );

        // Then
        assertEquals("The number of titles does not match the number of dates", exception.getMessage());
    }

    @Test
    void parse_whenTitleOrDateIsNull_shouldSkipNewsArticle() throws Exception {
        // Given
        Document document = mock(Document.class);
        String url = source.getUrl();
        Map<String, String> selectors = source.getSelectors();

        when(jsoupClient.getPageByUrl(url)).thenReturn(document);

        Elements titleElements = createElements("Title1", null, "Title3");
        Elements contentElements = createElements("Content1", "Content2", "Content3");
        Elements publishDateElements = createElements("01.01.2025", "02.01.2025", null);

        when(document.select(selectors.get(selectorTitle))).thenReturn(titleElements);
        when(document.select(selectors.get(selectorContent))).thenReturn(contentElements);
        when(document.select(selectors.get(selectorDate))).thenReturn(publishDateElements);

        Optional<LocalDate> optionalLocalDate = Optional.of(LocalDate.of(2025, 1, 1));
        Optional<LocalDate> optionalLocalDate2 = Optional.of(LocalDate.of(2025, 1, 2));
        Optional<LocalDate> optionalLocalDate3 = Optional.empty();

        when(dateParser.toLocalDateFromString(publishDateElements.get(0).text())).thenReturn(optionalLocalDate);
        when(dateParser.toLocalDateFromString(publishDateElements.get(1).text())).thenReturn(optionalLocalDate2);
        when(dateParser.toLocalDateFromString(publishDateElements.get(2).text())).thenReturn(optionalLocalDate3);

        // When
        List<NewsArticle> articleList = underTest.parse(source);

        // Then
        assertThat(articleList).hasSize(1);
        assertEquals("Title1", articleList.get(0).getTitle());
        assertEquals("Content1", articleList.get(0).getContent());
        assertEquals(LocalDate.of(2025, 1, 1), articleList.get(0).getPublishDate());
    }

    @Test
    void parse_whenFromElementsToListGotNull_shouldReturnException() throws Exception {
        // Given
        Document document = mock(Document.class);

        when(jsoupClient.getPageByUrl(anyString())).thenReturn(document);

        Elements nullElements = null;

        when(document.select(anyString())).thenReturn(nullElements);

        // When
        JsoupParseException exception = assertThrows(
                JsoupParseException.class,
                () -> underTest.parse(source)
        );

        // Then
        assertEquals("The elements list received from jsoup is null", exception.getMessage());
    }

    @Test
    void parse_whenContentIsNull_shouldReturnNewsArticleWhereArticleContentIsNull() throws Exception {
        // Given
        Document document = mock(Document.class);
        String url = source.getUrl();
        Map<String, String> selectors = source.getSelectors();

        when(jsoupClient.getPageByUrl(url)).thenReturn(document);

        Elements titleElements = createElements("Title1", "Title2", "Title3");
        Elements contentElements = createElements("Content1");
        Elements publishDateElements = createElements("01.01.2025", "02.01.2025", "03.01.2025");

        when(document.select(selectors.get(selectorTitle))).thenReturn(titleElements);
        when(document.select(selectors.get(selectorContent))).thenReturn(contentElements);
        when(document.select(selectors.get(selectorDate))).thenReturn(publishDateElements);

        Optional<LocalDate> optionalLocalDate = Optional.of(LocalDate.of(2025, 1, 1));
        Optional<LocalDate> optionalLocalDate2 = Optional.of(LocalDate.of(2025, 1, 2));
        Optional<LocalDate> optionalLocalDate3 = Optional.of(LocalDate.of(2025, 1, 3));

        when(dateParser.toLocalDateFromString(publishDateElements.get(0).text())).thenReturn(optionalLocalDate);
        when(dateParser.toLocalDateFromString(publishDateElements.get(1).text())).thenReturn(optionalLocalDate2);
        when(dateParser.toLocalDateFromString(publishDateElements.get(2).text())).thenReturn(optionalLocalDate3);

        // When
        List<NewsArticle> articleList = underTest.parse(source);

        // Then
        assertEquals("Content1", articleList.get(0).getContent());
        assertNull(articleList.get(1).getContent());
        assertNull(articleList.get(2).getContent());
        assertThat(source).isSameAs(articleList.get(0).getSource());
        assertNotNull(articleList.get(0).getHash());

        assertThat(articleList).hasSize(3);

        verify(jsoupClient).getPageByUrl(url);
        verify(dateParser, times(3)).toLocalDateFromString(anyString());
    }

    private Elements createElements(String... texts) {
        Elements elements = new Elements();
        for(String text: texts) {
            Element element = mock(Element.class);
            when(element.text()).thenReturn(text);
            elements.add(element);
        }

        return elements;
    }

}