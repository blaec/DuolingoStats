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

    private DailyEvents(LocalDate date, String event, Long xp) {
        this.date = date;
        this.dailyXpByType = new HashMap<>();
        this.dailyXpByType.put(event, xp);
        this.dailyCountByType = new HashMap<>();
        this.dailyCountByType.put(event, 1L);
        this.totalXp = xp;
    }

    // TODO probably merge will work here
    public DailyEvents mergeWith(Event event) {
        String type = Optional.ofNullable(event.getType()).orElse("unknown");
        Long xp = event.getXp();
        if (this.dailyXpByType.containsKey(type)) {
            this.dailyXpByType.replace(type, this.dailyXpByType.get(type) + xp);
        } else {
            this.dailyXpByType.put(type, xp);
        }
        if (this.dailyCountByType.containsKey(type)) {
            this.dailyCountByType.replace(type, this.dailyCountByType.get(type) + 1);
        } else {
            this.dailyCountByType.put(type, 1L);
        }
        this.totalXp += event.getXp();
        return this;
    }

    public static DailyEvents ofNew(Event event) {
        return new DailyEvents(event.getStartDate(), event.getType(), event.getXp());
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
