package com.blaec.duolingo.model;

import org.json.simple.JSONObject;

import java.time.*;

public class Event {
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = 3600;

    private final LocalDateTime start;
    private final String type;
    private final long xp;
    private String duration = "";

    private Event(LocalDateTime start, String type, long xp) {
        this.start = start;
        this.type = type;
        this.xp = xp;
    }

    private Event(LocalDateTime start, String type, long xp, String dif) {
        this(start, type, xp);
        this.duration = dif;
    }

    public static Event of(JSONObject event) {
        Long seconds = (Long) event.get("datetime");
        LocalDateTime start = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(seconds),
                ZoneId.systemDefault());
        String type = (String) event.get("event_type");
        long xp = (Long) event.get("improvement");

        return new Event(start, type, xp);
    }

    public Event updateDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = (seconds / SECONDS_PER_HOUR);
        seconds -= hours * SECONDS_PER_HOUR;
        long minutes = (seconds / SECONDS_PER_MINUTE);
        seconds -= minutes * SECONDS_PER_MINUTE;

        return new Event(this.start, this.type, this.xp, String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    public LocalDateTime getStart() {
        return start;
    }

    public Long getXp() {
        return xp;
    }

    public String getType() {
        return type;
    }

    public LocalDate calcStartDay() {
        return start.toLocalDate();
    }

    @Override
    public String toString() {
        return String.format("%1$tF %1$tT | %2$8s | %3$-8s %4$-2d", start, duration, type, xp);
    }
}
