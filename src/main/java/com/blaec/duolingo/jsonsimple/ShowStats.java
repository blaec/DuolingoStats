package com.blaec.duolingo.jsonsimple;

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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();

        // TODO get data from web and save to file
        // TODO get file by request
        try (FileReader reader = new FileReader(".files/Anja217644.json")) {
            JSONArray duolingoList = new JSONArray();
            duolingoList.add(jsonParser.parse(reader));
            JSONArray calendarList = (JSONArray) ((JSONObject) duolingoList.get(0)).get("calendar");

            // Parse all events and print them
            // TODO try to write via streams or enclose with Callable
            Map<Integer, Event> eventMap = new HashMap<>();
            int count = 0;
            for (Object calendar : calendarList) {
                Event event = Event.of((JSONObject) calendar);
                if (eventMap.size() > 0) {
                    event.setDif(Duration.between(eventMap.get(count - 1).getStart(), event.getStart()));
                }
                eventMap.put(count++, event);
            }
            System.out.println(String.format("%-10s %-8s | %-8s | %-8s %-2s", "Date", "Time", "Dif", "Event", "XP"));
            eventMap.values().forEach(System.out::println);

            // Combine all events by day and print daily stats
            Map<LocalDate, DailyEvents> dailyEventsMap = new TreeMap<>();
            // TODO try to simplify with computeIfPresent or with streams
//            eventMap.values().stream()
////                    .map(DailyEvents::ofNew)
//                    .collect(Collectors.toMap(
//                            DailyEvents::getDate,
//                            event -> new DailyEvents(event),
//                            DailyEvents::mergeWith));
//
//
////                    .collect(
////                            Collectors.groupingBy(DailyEvents::getDate),
////                            Collectors.mapping(DailyEvents::mergeWith())
////                    );
            for (Event event : eventMap.values()) {
                if (!dailyEventsMap.containsKey(event.getStartDate())) {
                    DailyEvents dailyEvents = DailyEvents.ofNew(event);
                    dailyEventsMap.put(event.getStartDate(), dailyEvents);
                } else {
                    DailyEvents mergedDailyEvents = dailyEventsMap.get(event.getStartDate());
                    mergedDailyEvents.mergeWith(event);
                    dailyEventsMap.replace(event.getStartDate(), mergedDailyEvents);
                }
            }
            System.out.println(String.format("%-10s | %-13s | %-13s | %-13s | %-4s", "Date", "lesson", "practice", "test", "total"));
            dailyEventsMap.values().forEach(System.out::println);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }


}
