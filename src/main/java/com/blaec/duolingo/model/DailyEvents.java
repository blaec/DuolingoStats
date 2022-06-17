package com.blaec.duolingo.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DailyEvents {
    private final LocalDate date;
    private final Map<String, Long> dailyXpByType;
    private final Map<String, Long> dailyCountByType;
    private Long totalXp;

    private DailyEvents(LocalDate date) {
        this.date = date;
        this.dailyXpByType = new HashMap<>();
        this.dailyCountByType = new HashMap<>();
        this.totalXp = 0L;
    }

    private DailyEvents(LocalDate date, String event, Long xp) {
        this(date);
        this.dailyXpByType.put(event, xp);
        this.dailyCountByType.put(event, 1L);
        this.totalXp = xp;
    }

    public DailyEvents(LocalDate date, Map<String, Long> dailyXpByType, Map<String, Long> dailyCountByType, Long totalXp) {
        this.date = date;
        this.dailyXpByType = dailyXpByType;
        this.dailyCountByType = dailyCountByType;
        this.totalXp = totalXp;
    }

    public static DailyEvents create(LocalDate date) {
        return new DailyEvents(date);
    }

    public DailyEvents mergeWith(Event event) {
        final String type = generateType(event.getType());
        final Long xp = event.getXp();

        this.dailyXpByType.merge(type, xp, Long::sum);
        this.dailyCountByType.merge(type, 1L, Long::sum);
        this.totalXp += xp;

        return new DailyEvents(this.date, this.dailyXpByType, this.dailyCountByType, this.totalXp);
    }

    public static DailyEvents from(Event event) {
        return new DailyEvents(
                event.calcStartDay(),
                generateType(event.getType()),
                event.getXp()
        );
    }

    private static String generateType(String type) {
        return Optional.ofNullable(type).orElse("unknown");
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format("%tF | %s | %s | %s | %s | %4d",
                date,
                stats("lesson"),
                stats("practice"),
                stats("test"),
                stats("unknown"),
                totalXp);
    }

    private String stats(String event) {
        Long xp = dailyXpByType.get(event);
        Long count = dailyCountByType.get(event);
        return String.format("%4s(%2s) /%3s",
                xp == null
                        ? "-"
                        : xp,
                xp == null
                        ? "--"
                        : xp / count,
                count == null
                        ? "-"
                        : count);
    }
}
