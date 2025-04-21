package com.crawler.web_crawler.service.newsArticle.implementation;

import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.parser.JsoupParser;
import com.crawler.web_crawler.repository.NewsArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsParserServiceImplTest {
    @Mock
    private JsoupParser parser;
    @Mock
    private NewsArticleRepository repository;
    @InjectMocks
    private NewsParserServiceImpl underTest;

    private Source source;
    private NewsArticle newsArticle1;
    private NewsArticle newsArticle2;

    @BeforeEach
    void setUp() {
        source = new Source(1L, "https://source1.com", "1 1 1 1 1 1", Map.of("Key1Source1", "Value1Source1", "Key2Source1", "Value2Source1"), true);
        newsArticle1 = new NewsArticle(1L, "title1", LocalDate.now(), "content1", source, "hash1");
        newsArticle2 = new NewsArticle(2L, "title2", LocalDate.now(), "content2", source, "hash2");
    }

    @Test
    @Disabled
    void parseAndSave_whenTimeLimit_shouldThrowException() {
        // Given
        // When
        // Then
    }

    @Test
    void parseAndSave_whenNewsArticlesAreNew_shouldSaveAll() {
        // Given
        List<NewsArticle> parsedArticles = new ArrayList<>(List.of(newsArticle1, newsArticle2));
        when(parser.parse(source)).thenReturn(parsedArticles);
        when(repository.findHashesBySource(any(Source.class))).thenReturn(Collections.emptySet());

        // When
        underTest.parseAndSave(source);

        // Then
        verify(parser).parse(source);
        verify(repository).saveAll(List.of(newsArticle1, newsArticle2));
        verify(repository, never()).save(any());
    }

    @Test
    void parseAndSave_whenSomeArticlesExist_shouldSaveOnlyNew() {
        // Given
        List<NewsArticle> parsedArticles = new ArrayList<>(List.of(newsArticle1, newsArticle2));
        when(parser.parse(source)).thenReturn(parsedArticles);
        when(repository.findHashesBySource(any(Source.class))).thenReturn(Set.of("hash1"));

        // When
        underTest.parseAndSave(source);

        // Then
        verify(parser).parse(source);
        verify(repository).saveAll(List.of(newsArticle2));
        verify(repository, never()).save(any());
    }

    @Test
    void parseAndSave_whenAllArticlesExist_shouldSaveNothing() {
        // Given
        List<NewsArticle> parsedArticles = new ArrayList<>(List.of(newsArticle1, newsArticle2));
        when(parser.parse(source)).thenReturn(parsedArticles);
        when(repository.findHashesBySource(any(Source.class))).thenReturn(Set.of("hash1", "hash2"));

        // When
        underTest.parseAndSave(source);

        // Then
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void parseAndSave_whenSaveFails_shouldReturnException() {
        // Given
        List<NewsArticle> parsedArticles = new ArrayList<>(List.of(newsArticle1, newsArticle2));
        when(parser.parse(source)).thenReturn(parsedArticles);
        when(repository.findHashesBySource(any(Source.class))).thenReturn(Set.of());

        doThrow(new DataIntegrityViolationException("DB error")).when(repository).saveAll(List.of(newsArticle1, newsArticle2));

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> underTest.parseAndSave(source)
        );

        // Then
        assertThat(exception).hasCauseExactlyInstanceOf(DataIntegrityViolationException.class);
        assertEquals("Runtime exception while saving data: DB error", exception.getMessage());
        verify(repository).saveAll(List.of(newsArticle1, newsArticle2));
    }

}