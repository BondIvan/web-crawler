package com.crawler.web_crawler.converter;

import com.crawler.web_crawler.model.dto.SourceRequestDTO;
import com.crawler.web_crawler.model.entity.Source;
import org.springframework.stereotype.Component;

@Component
public class SourceRequestDtoMapper {

    public Source fromDto(SourceRequestDTO sourceRequestDTO) {
        return Source.builder()
                .url(sourceRequestDTO.url())
                .schedule(sourceRequestDTO.schedule())
                .selectors(sourceRequestDTO.selectors())
                .build();
    }

    public SourceRequestDTO toDto(Source source) {
        return new SourceRequestDTO(
                source.getUrl(),
                source.getSchedule(),
                source.getSelectors());
    }

}
