package com.crawler.web_crawler.service.implementation;

import com.crawler.web_crawler.converter.SourceRequestDtoMapper;
import com.crawler.web_crawler.exception.SourceAlreadyExistsException;
import com.crawler.web_crawler.exception.SourceNotFoundException;
import com.crawler.web_crawler.model.dto.SourceRequestDTO;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.repository.SourceRepository;
import com.crawler.web_crawler.service.SourceService;
import com.crawler.web_crawler.service.scheduler.SchedulerParserService;
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
    private final SchedulerParserService schedulerParserService;

    public SourceServiceImpl(SourceRepository repository,
                             SourceRequestDtoMapper sourceMapper,
                             SchedulerParserService schedulerParserService) {
        this.repository = repository;
        mapper = sourceMapper;
        this.schedulerParserService = schedulerParserService;
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

        log.info("Adding new source with URL: {}", sourceRequestDTO.url());
        try {
            Source savedSource = repository.save(source);
            schedulerParserService.addNewScheduleSource(source);
            return savedSource;
        } catch (DataIntegrityViolationException e) {
            throw new SourceAlreadyExistsException("Source with this url: " + sourceRequestDTO.url() + " already exist", e);
        }

    }

    @Override
    public SourceRequestDTO updateSource(Long id, SourceRequestDTO sourceRequestDTO) {
        Optional<Source> optionalSource = repository.findById(id);
        if(optionalSource.isEmpty())
            throw new SourceNotFoundException("Source with such id not found");

        Source source = optionalSource.get();
        String oldUrl = source.getUrl();

        source.setUrl(sourceRequestDTO.url());
        source.setSchedule(sourceRequestDTO.schedule());
        source.setSelectors(sourceRequestDTO.selectors());
        source.setIsActive(sourceRequestDTO.isActive());

        try {
            Source updatedSource = repository.save(source);
            schedulerParserService.cancelScheduleSource(oldUrl);
            schedulerParserService.addNewScheduleSource(updatedSource);
            return mapper.toDto(updatedSource);
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
    public List<Source> getAllActiveSources() {
        return repository.findAllByIsActive();
    }

    @Override
    public void deleteSource(Long id) {
        Optional<Source> optionalSource = repository.findById(id);
        if(optionalSource.isEmpty())
            throw new SourceNotFoundException("Source with such id not found");

        Source source = optionalSource.get();
        schedulerParserService.cancelScheduleSource(source.getUrl());

        log.info("Deleting source with URL: {}", source.getUrl());

        repository.delete(source);
    }
}
