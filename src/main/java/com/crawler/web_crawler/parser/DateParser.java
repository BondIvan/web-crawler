package com.crawler.web_crawler.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Component
public class DateParser {
    private final static String REGEX_FULL_YEAR = ".*\\d{4}.*";
    private final static String REGEX_SHORT_YEAR = ".*\\d{2}\\b.*";
    private final static String REGEX_TIME = "\\b\\d{1,2}:\\d{2}\\b";
    private final static String REGEX_COMMA = "[,]";

    public LocalDate toLocalDateFromString(String date) {
        date = date.toLowerCase();
        date = deleteTime(date);

        int offset = getOffsetIfAdverbTimeExist(date);
        if(offset <= 0)
            return LocalDate.now().plusDays(offset);

        LocalDate localDate;
        for(DateTimeFormatter formatter: ParseUtils.getDateTimeFormatters()) {
            try {
                if(!date.matches(REGEX_FULL_YEAR) && !date.matches(REGEX_SHORT_YEAR))
                    date = date + " " + LocalDate.now().getYear();
                localDate = LocalDate.parse(date, formatter);

                return localDate;
            } catch (DateTimeParseException ignore) {}
        }

        log.warn("Unable to recognize date: {}", date);
        return null;
    }

    private String deleteTime(String date) {
        return date
                .replaceAll(REGEX_TIME, "")
                .replaceAll(REGEX_COMMA, "")
                .trim();
    }

    private int getOffsetIfAdverbTimeExist(String date) {
        record DayAndOffset(
                String name,
                int offset
        ) {}
        DayAndOffset ruToday = new DayAndOffset("сегодня", 0);
        DayAndOffset ruYesterday = new DayAndOffset("вчера", -1);
        List<DayAndOffset> daysWithOffset = List.of(ruToday, ruYesterday);

        for(DayAndOffset landDayAndOffset: daysWithOffset) {
            String name = landDayAndOffset.name();
            int offset = landDayAndOffset.offset();
            int index = date.indexOf(name);
            if(index >= 0) {
                return offset;
            }
        }

        return 1; // No offset
    }

}
