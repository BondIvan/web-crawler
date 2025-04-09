package com.crawler.web_crawler.controller;

import com.crawler.web_crawler.model.dto.SourceRequestDTO;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.service.NewsParserService;
import com.crawler.web_crawler.service.SourceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
public class SourceController {
    private final SourceService service;
    private final NewsParserService articleService;

    public SourceController(SourceService sourceService, NewsParserService articleService) {
        this.service = sourceService;
        this.articleService = articleService;
    }

    @Operation(summary = "Add new source")
    @PostMapping("/sources")
    public ResponseEntity<SourceRequestDTO> addSource(@RequestBody @Valid SourceRequestDTO sourceRequestDTO) {
        service.addSource(sourceRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(sourceRequestDTO);
    }

    @GetMapping("/sources")
    public ResponseEntity<List<SourceRequestDTO>> getAllSources() {
        return ResponseEntity.ok(service.getAllSources());
    }

    @DeleteMapping("/sources/{id}")
    public ResponseEntity<String> deleteSource(@PathVariable("id") Long id) {
        service.deleteSource(id);
        return ResponseEntity.ok("Deleted");
    }

    @PostMapping("/source/{id}/parse")
    public void parseSource(@PathVariable("id") Long id) {
        Source source = service.getSource(id);
        articleService.parseAndSave(source);
    }

}
