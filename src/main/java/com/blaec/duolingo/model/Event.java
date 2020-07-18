package com.blaec.duolingo.model;

import org.json.simple.JSONObject;

import java.time.*;

public class Event {
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = 3600;

    private final LocalDateTime start;
    private final String type;
    private final Long xp;
    private String dif = "";

    public static Event of(JSONObject event) {
        return new Event(event);
    }

    private Event(JSONObject event) {
        Long seconds = (Long) event.get("datetime");
        this.start = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(seconds),
                ZoneId.systemDefault());
        this.type = (String) event.get("event_type");
        this.xp = (Long) event.get("improvement");
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setDif(Duration dif) {
        long seconds = dif.getSeconds();
        int hours = (int) (seconds / SECONDS_PER_HOUR);
        seconds -= hours * SECONDS_PER_HOUR;
        int minutes = (int) (seconds / SECONDS_PER_MINUTE);
        seconds -= minutes * SECONDS_PER_MINUTE;
        this.dif = String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public LocalDate getStartDate() {
        return start.toLocalDate();
    }

    public Long getXp() {
        return xp;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%1$tF %1$tT | %2$8s | %3$-8s %4$-2d", start, dif, type, xp);
    }
}
