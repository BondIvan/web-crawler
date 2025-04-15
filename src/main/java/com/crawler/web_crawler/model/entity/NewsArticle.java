package com.crawler.web_crawler.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "news_article")
public class NewsArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(columnDefinition = "text")
    private String content;

    @ManyToOne
    @JoinColumn(name = "source_id")
    private Source source;

    @Column(unique = true)
    private String hash; // for check duplicates

    //TODO Если изменить title, date или source нужно изменять и hash. Подумать об этом
    public void setHash() {
        hash = String.valueOf(Objects.hash(
                this.title.toLowerCase(),
                this.publishDate,
                this.source.getId()));
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        NewsArticle article = (NewsArticle) obj;

        return Objects.equals(title, article.title) &&
                Objects.equals(source, article.source) && Objects.equals(hash, article.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, source, hash);
    }

    @Override
    public String toString() {
        return String.format("id=%d, title=%s, content=%s, publishDate=%s, hash=%s, source=%s",
                id, title, content, publishDate, hash, source.toString());
    }

}
