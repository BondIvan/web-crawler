package com.crawler.web_crawler.model.entity;

import com.crawler.web_crawler.converter.JsonMapConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("url")
    @Column(nullable = false, unique = true)
    private String url;

    @JsonProperty("schedule")
    private String schedule;

    @Column(columnDefinition = "JSON")
    @Convert(converter = JsonMapConverter.class)
    @JsonProperty("selectors")
    private Map<String, String> selectors;

    @Column(name = "is_active")
    private Boolean isActive;

    //TODO Add language field for parsing publishDate in different languages
//    @Column(name = "lang", nullable = false)
//    private String language;

//    @Transient
//    private volatile boolean isParsing;

    @Override
    public String toString() {
        return String.format("Source: id=%d, isActive=%s, url=%s, schedule=%s, selectors=%s",
                id, isActive, url, schedule, selectors);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        Source source = (Source) obj;

        return Objects.equals(url, source.url) && Objects.equals(selectors, source.selectors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, selectors);
    }
}
