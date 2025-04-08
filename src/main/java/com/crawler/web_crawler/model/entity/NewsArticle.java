package com.crawler.web_crawler.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class NewsArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "text")
    private String content;
    private LocalDateTime publishDate;

    @ManyToOne
    @JoinColumn(name = "source_id")
    private Source source;

    @Column(unique = true)
    private String hash; // for check duplicates

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
