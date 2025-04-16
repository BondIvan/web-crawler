package com.crawler.web_crawler.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DateParserTest {

    private DateParser underTest;

    @BeforeEach
    void setUp() {
        underTest = new DateParser();
    }

    @Test
    void toLocalDateTimeFromString_whenStringDateHasTime_shouldDeleteTimeAndCommas_returnLocalDate() {
        // Given
        String date = "15 января 2025, 13:41";
        String date2 = "Сегодня, 15:31";

        // When
        LocalDate expected = underTest.toLocalDateFromString(date);
        LocalDate expected2 = underTest.toLocalDateFromString(date2);

        // Then
        assertEquals(expected, LocalDate.of(2025, 1, 15));
        assertEquals(expected2, LocalDate.now());
    }

    @Test
    void toLocalDateFromString_whenStringDateDoesntContainYear_shouldAddCurrentYear_returnLocalDate() {
        // Given
        String date = "8 апреля";
        String date2 = "Вчера, 10 Июля";

        // When
        LocalDate expected = underTest.toLocalDateFromString(date);
        LocalDate expected2 = underTest.toLocalDateFromString(date2);

        // Then
        assertEquals(expected, LocalDate.of(LocalDate.now().getYear(), 4, 8));
        assertEquals(expected2, LocalDate.now().plusDays(-1));
    }

    @Test
    void toLocalDateFromString_whenStringDateContainsTodayOrYesterday_shouldReturnLocalDateCurrentOrPreviousDay_ignoreCase() {
        // Given
        String todayDay = "Сегодня";
        String yesterday = "Вчера";

        // When
        LocalDate expectedToday = underTest.toLocalDateFromString(todayDay);
        LocalDate expectedYesterday = underTest.toLocalDateFromString(yesterday);

        // Then
        assertEquals(expectedToday, LocalDate.now());
        assertEquals(expectedYesterday, LocalDate.now().plusDays(-1));
    }

    @Test
    void toLocalDateFromString_whenStringDateIsCorrect_shouldReturnLocalDate() {
        // Given
        String date = "15 января 2025";
        String date2 = "8 апреля 25";
        String date3 = "15.01.2025";
        String date4 = "08.04.25";

        // When
        LocalDate expected = underTest.toLocalDateFromString(date);
        LocalDate expected2 = underTest.toLocalDateFromString(date2);
        LocalDate expected3 = underTest.toLocalDateFromString(date3);
        LocalDate expected4 = underTest.toLocalDateFromString(date4);

        // Then
        assertEquals(expected, LocalDate.of(2025, 1, 15));
        assertEquals(expected2, LocalDate.of(2025, 4, 8));
        assertEquals(expected3, LocalDate.of(2025, 1, 15));
        assertEquals(expected4, LocalDate.of(2025, 4, 8));
    }

    @Test
    void toLocalDateFromString_whenParseUtilsDoesntContainsFormatterForStringDate_shouldReturnNull() {
        // Given
        String unsupportedDate = "01.01 2025";
        String unsupportedDate2 = "Январь, 1";

        // When
        LocalDate expectedNull = underTest.toLocalDateFromString(unsupportedDate);
        LocalDate expectedNull2 = underTest.toLocalDateFromString(unsupportedDate2);

        // Then
        assertNull(expectedNull);
        assertNull(expectedNull2);
    }


}