package com.crawler.web_crawler.repository;

import com.crawler.web_crawler.model.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    boolean existsByUrl(String url);

    @Query(value = "SELECT source FROM Source source WHERE source.isActive=true")
    List<Source> findAllByIsActive();
}
