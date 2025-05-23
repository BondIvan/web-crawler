package com.crawler.web_crawler.repository;

import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    @Query(value = "SELECT article.hash FROM NewsArticle article WHERE article.source = ?1")
    Set<String> findHashesBySource(Source source);
}
