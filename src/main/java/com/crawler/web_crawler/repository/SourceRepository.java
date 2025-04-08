package com.crawler.web_crawler.repository;

import com.crawler.web_crawler.model.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    boolean existsByUrl(String url);
}
