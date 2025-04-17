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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        String date2 = "сегодня, 15:31";

        // When
        Optional<LocalDate> actual = underTest.toLocalDateFromString(date);
        Optional<LocalDate> actual2 = underTest.toLocalDateFromString(date2);

        // Then
        Optional<LocalDate> expectedOptionalLocalDate = Optional.of(LocalDate.of(2025, 1, 15));
        Optional<LocalDate> expectedOptionalLocalDate2 = Optional.of(LocalDate.now());

        assertEquals(expectedOptionalLocalDate, actual);
        assertEquals(expectedOptionalLocalDate2, actual2);
    }

    @Test
    void toLocalDateFromString_whenStringDateDoesntContainYear_shouldAddCurrentYear_returnLocalDate() {
        // Given
        String date = "8 апреля";
        String date2 = "вчера, 10 Июля";

        // When
        Optional<LocalDate> actual = underTest.toLocalDateFromString(date);
        Optional<LocalDate> actual2 = underTest.toLocalDateFromString(date2);

        // Then
        Optional<LocalDate> expectedOptionalLocalDate = Optional.of(LocalDate.of(LocalDate.now().getYear(), 4, 8));
        Optional<LocalDate> expectedOptionalLocalDate2 = Optional.of(LocalDate.now().plusDays(-1));

        assertEquals(expectedOptionalLocalDate, actual);
        assertEquals(expectedOptionalLocalDate2, actual2);
    }

    @Test
    void toLocalDateFromString_whenStringDateContainsTodayOrYesterday_shouldReturnLocalDateCurrentOrPreviousDay_ignoreCase() {
        // Given
        String todayDay = "Сегодня";
        String yesterday = "Вчера";

        // When
        Optional<LocalDate> actual = underTest.toLocalDateFromString(todayDay);
        Optional<LocalDate> actual2 = underTest.toLocalDateFromString(yesterday);

        // Then
        Optional<LocalDate> expectedOptionalLocalDate = Optional.of(LocalDate.now());
        Optional<LocalDate> expectedOptionalLocalDate2 = Optional.of(LocalDate.now().plusDays(-1));

        assertEquals(expectedOptionalLocalDate, actual);
        assertEquals(expectedOptionalLocalDate2, actual2);
    }

    @Test
    void toLocalDateFromString_whenStringDateIsCorrect_shouldReturnLocalDate() {
        // Given
        String date = "15 января 2025";
        String date2 = "8 апреля 25";
        String date3 = "15.01.2025";
        String date4 = "08.04.25";

        // When
        Optional<LocalDate> actual = underTest.toLocalDateFromString(date);
        Optional<LocalDate> actual2 = underTest.toLocalDateFromString(date2);
        Optional<LocalDate> actual3 = underTest.toLocalDateFromString(date3);
        Optional<LocalDate> actual4 = underTest.toLocalDateFromString(date4);

        // Then
        Optional<LocalDate> expectedOptionalLocalDate = Optional.of(LocalDate.of(2025, 1, 15));
        Optional<LocalDate> expectedOptionalLocalDate2 = Optional.of(LocalDate.of(2025, 4, 8));
        Optional<LocalDate> expectedOptionalLocalDate3 = Optional.of(LocalDate.of(2025, 1, 15));
        Optional<LocalDate> expectedOptionalLocalDate4 = Optional.of(LocalDate.of(2025, 4, 8));

        assertEquals(expectedOptionalLocalDate, actual);
        assertEquals(expectedOptionalLocalDate2, actual2);
        assertEquals(expectedOptionalLocalDate3, actual3);
        assertEquals(expectedOptionalLocalDate4, actual4);
    }

    @Test
    void toLocalDateFromString_whenParseUtilsDoesntContainsFormatterForStringDate_shouldReturnNull() {
        // Given
        String unsupportedDate = "01.01 2025";
        String unsupportedDate2 = "Январь, 1";

        // When
        Optional<LocalDate> actualOptionalIsEmpty = underTest.toLocalDateFromString(unsupportedDate);
        Optional<LocalDate> actualOptionalIsEmpty2 = underTest.toLocalDateFromString(unsupportedDate2);

        // Then
        assertThat(actualOptionalIsEmpty).isEmpty();
        assertThat(actualOptionalIsEmpty2).isEmpty();
    }


}