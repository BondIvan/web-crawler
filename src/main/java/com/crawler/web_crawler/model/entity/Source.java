package com.crawler.web_crawler.model.entity;

import com.crawler.web_crawler.converter.JsonMapConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.Map;
import java.util.Objects;

@Entity
public class Source {

    public Source() {}

    public Source(Long id, String url, String schedule, Map<String, String> selectors) {
        this.id = id;
        this.url = url;
        this.schedule = schedule;
        this.selectors = selectors;
    }

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

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Map<String, String> getSelectors() {
        return selectors;
    }

    public void setSelectors(Map<String, String> selectors) {
        this.selectors = selectors;
    }

    @Override
    public String toString() {
        return String.format("Source: id=%d, url=%s, schedule=%s, selectors=%s", id, url, schedule, selectors);
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
