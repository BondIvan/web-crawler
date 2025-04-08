package com.crawler.web_crawler.converter;

import com.crawler.web_crawler.model.dto.SourceRequestDTO;
import com.crawler.web_crawler.model.entity.Source;
import org.springframework.stereotype.Component;

@Component
public class SourceRequestDtoMapper {

    public Source fromDto(SourceRequestDTO sourceRequestDTO) {
        Source source = new Source();
        source.setUrl(sourceRequestDTO.url());
        source.setSchedule(sourceRequestDTO.schedule());
        source.setSelectors(sourceRequestDTO.selectors());

        return source;
    }

    public SourceRequestDTO toDto(Source source) {
        return new SourceRequestDTO(
                source.getUrl(),
                source.getSchedule(),
                source.getSelectors());
    }

}
