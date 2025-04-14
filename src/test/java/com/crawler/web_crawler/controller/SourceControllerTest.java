package com.crawler.web_crawler.controller;

import com.crawler.web_crawler.exception.SourceAlreadyExistsException;
import com.crawler.web_crawler.exception.SourceNotFoundException;
import com.crawler.web_crawler.model.dto.SourceRequestDTO;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.service.NewsParserService;
import com.crawler.web_crawler.service.implementation.SourceServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SourceController.class)
class SourceControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SourceServiceImpl sourceService;
    @MockBean
    private NewsParserService articleService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void parseSource_whenSourceExist_shouldReturn200() throws Exception {
        // Given
        Long id = 1L;
        Source mockSource = mock(Source.class);

        when(sourceService.getSource(id)).thenReturn(mockSource);
        doNothing().when(articleService).parseAndSave(mockSource);

        // When & Then
        mockMvc.perform(post("/api/v1/source/{id}/parse", id))
                .andExpect(status().isOk());

        verify(sourceService).getSource(id);
        verify(articleService).parseAndSave(mockSource);
    }

    @Test
    void parseSource_whenSourceDoesntExist_shouldReturn404WithMessage() throws Exception {
        // Given
        Long id = 1L;
        String errorMessage = "Source with such id not found";

        doThrow(new SourceNotFoundException(errorMessage)).when(sourceService).getSource(id);

        // When & Then
        mockMvc.perform(post("/api/v1/source/{id}/parse", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorMessage));
    }

    @Test
    void addSource_whenValidRequest_shouldReturn201WithSourceDTO() throws Exception {
        // Given
        SourceRequestDTO sourceRequestDTO = new SourceRequestDTO("https://source.com", "1 1 1 1 1 1", Map.of("key", "value"), true);
        String jsonRequestBody = objectMapper.writeValueAsString(sourceRequestDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/sources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(sourceRequestDTO.url()))
                .andExpect(jsonPath("$.schedule").value(sourceRequestDTO.schedule()))
                .andExpect(jsonPath("$.selectors.key").value(sourceRequestDTO.selectors().get("key")));

        verify(sourceService).addSource(sourceRequestDTO);
    }

    @Test
    void addSource_whenDuplicateSource_shouldReturn409WithException() throws Exception {
        // Given
        SourceRequestDTO sourceRequestDTO = new SourceRequestDTO("https://duplicate.com", "1 1 1 1 1 1", Map.of("key", "value"), true);
        String jsonRequestBody = objectMapper.writeValueAsString(sourceRequestDTO);
        String errorMessage = "Source with this url: " + sourceRequestDTO.url() + " already exist";

        doThrow(new SourceAlreadyExistsException(errorMessage)).when(sourceService).addSource(any());

        // When & Then
        mockMvc.perform(post("/api/v1/sources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorMessage));

        verify(sourceService).addSource(any());
    }

    @Test
    void addSource_whenValidationFailed_shouldReturn400WithException() throws Exception {
        // Given
        String invalidJsonRequestBody = """
                 {
                    "url": "",
                    "schedule": ""
                 }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/sources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.schedule").exists())
                .andExpect(jsonPath("$.selectors").exists());

        verifyNoInteractions(sourceService);
    }

    @Test
    void getAllSources_whenSourcesExist_shouldReturn200WithSources() throws Exception {
        // Given
        SourceRequestDTO sourceRequestDTO1 = new SourceRequestDTO("https://source1.com", "1 1 1 1 1 1", Map.of("key1", "value1"), true);
        SourceRequestDTO sourceRequestDTO2 = new SourceRequestDTO("https://source2.com", "2 2 2 2 2 2", Map.of("key2", "value2"), true);

        when(sourceService.getAllSources()).thenReturn(List.of(sourceRequestDTO1, sourceRequestDTO2));

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/v1/sources")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String resultJson = result.getResponse().getContentAsString();
        List<SourceRequestDTO> resultDTOS = objectMapper.readValue(resultJson, new TypeReference<>() {});

        assertEquals(2, resultDTOS.size());
        assertThat(resultDTOS).containsExactlyInAnyOrder(sourceRequestDTO1, sourceRequestDTO2);
        verify(sourceService, times(1)).getAllSources();
    }

    @Test
    void getAllSources_whenNoSources_shouldReturnEmptyList() throws Exception {
        // Given
        when(sourceService.getAllSources()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/sources")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(sourceService, times(1)).getAllSources();
    }

    @Test
    void deleteSource_whenSourceExist_shouldReturn200WithMessage() throws Exception {
        // Given
        Long id = 1L;

        doNothing().when(sourceService).deleteSource(id);

        // When & Then
        mockMvc.perform(delete("/api/v1/sources/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Deleted"));

        verify(sourceService).deleteSource(id);
    }

    @Test
    void deleteSource_whenSourceDoesntExist_shouldReturn404WithMessage() throws Exception {
        // Given
        Long id = 1L;
        String errorMessage = "Source with such id not found";

        doThrow(new SourceNotFoundException(errorMessage)).when(sourceService).deleteSource(id);

        // When & Then
        mockMvc.perform(delete("/api/v1/sources/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorMessage));

        verify(sourceService).deleteSource(id);
    }


}