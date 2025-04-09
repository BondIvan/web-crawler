package com.crawler.web_crawler.service.implementation;

import com.crawler.web_crawler.converter.SourceRequestDtoMapper;
import com.crawler.web_crawler.exception.SourceAlreadyExistsException;
import com.crawler.web_crawler.exception.SourceNotFoundException;
import com.crawler.web_crawler.model.dto.SourceRequestDTO;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.repository.SourceRepository;
import com.crawler.web_crawler.service.SourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SourceServiceImpl implements SourceService {
    private final SourceRepository repository;
    private final SourceRequestDtoMapper mapper;

    public SourceServiceImpl(SourceRepository repository, SourceRequestDtoMapper sourceMapper) {
        this.repository = repository;
        mapper = sourceMapper;
    }

    @Override
    public Source getSource(Long id) {
        Optional<Source> optionalSource = repository.findById(id);

        if(optionalSource.isEmpty())
            throw new SourceNotFoundException("Source with such id not found");

        return optionalSource.get();
    }

    @Override
    public Source addSource(SourceRequestDTO sourceRequestDTO) {
        //TODO Check if such a source correct

        if(repository.existsByUrl(sourceRequestDTO.url()))
            throw new SourceAlreadyExistsException("Source with this url: " + sourceRequestDTO.url() + " already exist");

        Source source = mapper.fromDto(sourceRequestDTO);

        //TODO Add validation selectors

        log.info("Adding new source with URL: {}", sourceRequestDTO.url());

        try {
            return repository.save(source);
        } catch (DataIntegrityViolationException e) {
            throw new SourceAlreadyExistsException("Source with this url: " + sourceRequestDTO.url() + " already exist", e);
        }

    }

    @Override
    public List<SourceRequestDTO> getAllSources() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public void deleteSource(Long id) {
        Optional<Source> optionalSource = repository.findById(id);
        if(optionalSource.isEmpty())
            throw new SourceNotFoundException("Source with such id not found");

        Source source = optionalSource.get();

        log.info("Deleting source with URL: {}", source.getUrl());

        repository.delete(source);
    }
}
