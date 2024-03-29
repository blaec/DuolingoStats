package com.blaec.duolingo.jsonsimple;

import com.blaec.duolingo.enums.Types;
import com.blaec.duolingo.model.DailyEvents;
import com.blaec.duolingo.model.Event;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ShowStats {
    //https://www.duolingo.com/users/etin682286
    //https://www.duolingo.com/users/EIrV12
    //https://www.duolingo.com/users/Jamie371239
    //https://www.duolingo.com/users/Abe65174
    //https://www.duolingo.com/users/augusto619478
    //https://www.duolingo.com/users/_blaec_
    //https://www.duolingo.com/users/AditiShriv
    //https://www.duolingo.com/users/Parisotto.Net

    // TODO add logging
    // TODO make executable jar - run with cmd command java -jar <jarname>.jar
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();

        // TODO get data from web and save to file
        // TODO get file by request
        try (FileReader reader = new FileReader(".files/sandrine442218.json")) {
//        try (FileReader reader = new FileReader(".files/me.json")) {
            JSONArray duolingoList = new JSONArray();
            duolingoList.add(jsonParser.parse(reader));
            JSONArray calendarList = (JSONArray) ((JSONObject) duolingoList.get(0)).get("calendar");

            // Parse all events and print them
            // TODO try to write via streams or enclose with Callable
            LinkedList<Event> eventList = new LinkedList<>();
            for (Object calendar : calendarList) {
                Event event = Event.of((JSONObject) calendar);
                Duration dif = eventList.isEmpty()
                        ? Duration.ZERO
                        : Duration.between(eventList.getLast().getStart(), event.getStart());
                eventList.add(event.updateDuration(dif));
            }
            StringBuilder result = new StringBuilder(String.format("%-10s %-8s | %-8s | %-8s %-2s%n", "Date", "Time", "Dif", "Event", "XP"));
            eventList.forEach(e -> result.append(e.toString()).append("\n"));

            // Combine all events by day and print daily stats
            Map<LocalDate, DailyEvents> dailyEventsMap = new TreeMap<>();
            for (Event event : eventList) {
                LocalDate startDate = event.calcStartDay();
                DailyEvents merged = dailyEventsMap
                        .getOrDefault(startDate, DailyEvents.empty(startDate))
                        .mergeWith(event);
                dailyEventsMap.putIfAbsent(startDate, merged);
            }
            result.append(String.format("%-10s | %-13s | %-13s | %-13s | %-13s | %-4s%n",
                    "Date", Types.lesson, Types.practice, Types.test, Types.unknown, "total"));
            dailyEventsMap.values().forEach(d -> result.append(d.toString()).append("\n"));
            System.out.println(result);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }


}
