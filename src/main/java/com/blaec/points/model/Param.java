package com.blaec.points.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.blaec.points.EntryPoint.SLEEP_SECONDS;

public class Param {
    private final String link;
    private final String lang;
    private final String mode;
    private final long startTime;
    private static int currentStory;
    private static List<Integer> storiesToSkip = new ArrayList<>();

    private Param(String link, String lang, String mode, long startTime) {
        this.link = link;
        this.lang = lang;
        this.mode = mode;
        this.startTime = startTime;
    }

    public static Param create(List<Story> stories) {
        String mode = ThreadLocalRandom.current().nextBoolean()
                ? "READ"
                : "CONVERSATION";
        int leftAttempts = 10;
        do {
            leftAttempts--;
            currentStory = ThreadLocalRandom.current().nextInt(0, stories.size());
        } while (storiesToSkip.contains(currentStory) || leftAttempts > 0);
        Story param = stories.get(currentStory);

        return new Param(param.getLink(), param.getLanguage(), mode, evaluateStartTime());
    }

    private static long evaluateStartTime() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(SLEEP_SECONDS);
        ZonedDateTime zdt = ZonedDateTime.of(now, ZoneId.systemDefault());

        return zdt.toInstant().toEpochMilli() / 1000;
    }

    public static void skipStory(Param param) {
        storiesToSkip.add(currentStory);
        System.out.printf("Skipped story: %s%n", param);
    }

    public String getLink() {
        return link;
    }

    public String getLang() {
        return lang;
    }

    public String getMode() {
        return mode;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return String.format("%-40s | %-12s", link, mode);
    }
}
