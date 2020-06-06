package com.blaec.duolingo.jsonsimple;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ReadJSONExample {
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = 3600;

    public static void main(String[] args) {
        //https://www.duolingo.com/users/etin682286
        //https://www.duolingo.com/users/EIrV12
        //https://www.duolingo.com/users/Jamie371239
        //https://www.duolingo.com/users/Abe65174
        //https://www.duolingo.com/users/augusto619478
        //https://www.duolingo.com/users/_blaec_
        //https://www.duolingo.com/users/AditiShriv
        //https://www.duolingo.com/users/Parisotto.Net
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("files/me.json")) {
            Object obj = jsonParser.parse(reader);
            JSONArray duolingoList = new JSONArray();
            duolingoList.add(obj);
            JSONArray calendarList = (JSONArray) ((JSONObject) duolingoList.get(0)).get("calendar");

            Map<Integer, Attempt> attemptMap = new HashMap<>();
            int count = 0;
            for (Object calendar : calendarList) {
                Attempt attempt = Attempt.of((JSONObject) calendar);
                if (attemptMap.size() > 0) {
                    Duration duration = Duration.between(
                            attemptMap.get(count - 1).getStart(),
                            attempt.getStart());
                    attempt.setDif(duration);
                }
                attemptMap.put(count++, attempt);
            }
            attemptMap.values().forEach(System.out::println);

            Map<LocalDate, DailyAttempt> dailyAttemptMap = new TreeMap<>();
            for (Attempt attempt : attemptMap.values()) {
                if (dailyAttemptMap.containsKey(attempt.getStartDate())) {
                    DailyAttempt oldDailyAttempt = dailyAttemptMap.get(attempt.getStartDate());
                    oldDailyAttempt.modifyOld(attempt);
                    dailyAttemptMap.replace(attempt.getStartDate(), oldDailyAttempt);
                } else {
                    DailyAttempt dailyAttempt = DailyAttempt.ofNew(attempt);
                    dailyAttemptMap.put(attempt.getStartDate(), dailyAttempt);
                }

            }
            dailyAttemptMap.values().forEach(System.out::println);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static class Attempt {
        private final LocalDateTime start;
        private final String event;
        private final Long xp;
        private String dif = "";

        public static Attempt of(JSONObject lesson) {
            return new Attempt(lesson);
        }

        private Attempt(JSONObject lesson) {
            Long seconds = (Long) lesson.get("datetime");
            this.start = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(seconds),
                    ZoneId.systemDefault());
            this.event = (String) lesson.get("event_type");
            this.xp = (Long) lesson.get("improvement");
        }

        public LocalDateTime getStart() {
            return start;
        }

        public void setDif(Duration dif) {
            long secondOfDay = dif.getSeconds();
            int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
            secondOfDay -= hours * SECONDS_PER_HOUR;
            int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
            secondOfDay -= minutes * SECONDS_PER_MINUTE;
            this.dif = String.format("%02d:%02d:%02d", hours, minutes, secondOfDay);
        }

        public LocalDate getStartDate() {
            return start.toLocalDate();
        }

        public Long getXp() {
            return xp;
        }

        public String getEvent() {
            return event;
        }

        @Override
        public String toString() {
            return String.format("%tF %tT | %8s | %-8s %-2d", start, start, dif, event, xp);
        }
    }

    private static class DailyAttempt {
        private final LocalDate date;
        private final Map<String, Long> eventXP;
        private final Map<String, Long> eventCount;
        private Long total;

        private DailyAttempt(LocalDate date, String event, Long xp) {
            this.date = date;
            this.eventXP = new HashMap<>();
            this.eventXP.put(event, xp);
            this.eventCount = new HashMap<>();
            this.eventCount.put(event, 1L);
            this.total = xp;
        }

        public void modifyOld(Attempt attempt) {
            String event = attempt.getEvent();
            Long xp = attempt.getXp();
            if (this.eventXP.containsKey(event)) {
                this.eventXP.replace(event, this.eventXP.get(event) + xp);
            } else {
                this.eventXP.put(event, xp);
            }
            if (this.eventCount.containsKey(event)) {
                this.eventCount.replace(event, this.eventCount.get(event) + 1);
            } else {
                this.eventCount.put(event, 1L);
            }
            this.total += attempt.getXp();
        }

        public static DailyAttempt ofNew(Attempt attempt) {
            return new DailyAttempt(attempt.getStartDate(), attempt.getEvent(), attempt.getXp());
        }

        @Override
        public String toString() {
            return String.format("%tF | %s | %s | %s | %4d", date, stats("lesson"), stats("practice"), stats("test"), total);
        }

        private String stats(String event) {
            Long xp = eventXP.get(event);
            Long count = eventCount.get(event);
            return String.format("%4s(%2s) /%3s", xp == null ? "-" : xp, xp == null ? "--" : xp / count, count == null ? "-" : count);
        }
    }
}
