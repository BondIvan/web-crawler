package com.crawler.web_crawler.service;

import com.crawler.web_crawler.model.dto.SourceRequestDTO;
import com.crawler.web_crawler.model.entity.Source;

import java.util.List;

public interface SourceService {
    Source getSource(Long id);
    Source addSource(SourceRequestDTO sourceRequestDTO);
    List<SourceRequestDTO> getAllSources();
    void deleteSource(Long id);
}
