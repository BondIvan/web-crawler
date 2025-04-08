package com.crawler.web_crawler.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "news_article")
public class NewsArticle {

    public NewsArticle() {}

    public NewsArticle(Long id, String title, LocalDateTime publishDate, String content, Source source, String hash) {
        this.id = id;
        this.title = title;
        this.publishDate = publishDate;
        this.content = content;
        this.source = source;
        this.hash = hash;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(columnDefinition = "text")
    private String content;

    @ManyToOne
    @JoinColumn(name = "source_id")
    private Source source;

    @Column(unique = true)
    private String hash; // for check duplicates

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        NewsArticle article = (NewsArticle) obj;

        return Objects.equals(title, article.title) && Objects.equals(source, article.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, source);
    }

    @Override
    public String toString() {
        return String.format("id=%d, title=%s, content=%s, publishDate=%s, hash=%s, source=%s",
                id, title, content, publishDate, hash, source.toString());
    }

    @PrePersist
    public void generateHash() {
        hash = String.valueOf(Objects.hash(
                this.title.toLowerCase(),
                this.publishDate,
                this.source.getId()
        ));
    }
}
