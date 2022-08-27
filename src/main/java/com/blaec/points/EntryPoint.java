package com.blaec.points;


import com.blaec.points.model.Lesson;
import com.blaec.points.model.Param;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.blaec.points.utils.Data.portuguese;

public class EntryPoint {
    private final static List<Lesson> lessons = new ArrayList<>() {{
        addAll(portuguese);
//            addAll(french);
    }};
    private final static List<Integer> pointHistory = new ArrayList<>();
    private final static List<Long> pauseHistory = new ArrayList<>();
    public static int limit = 10;
    public static final int SLEEP_SECONDS = 120;
    public static final int SLEEP_SHIFT_SECONDS = 60;

    public static void main(String[] args) throws Exception {
        JSONParser jsonParser = new JSONParser();
        int count = 0;

        while (limit > 0) {
            Param param = Param.create(lessons);
            Response response = getResponse(param);
            int awardedXp = extractAwardedXp(jsonParser, response);
            limit = limit - awardedXp;
            count++;

            long timeShift = ThreadLocalRandom.current().nextLong(0L, TimeUnit.SECONDS.toMillis(SLEEP_SHIFT_SECONDS));
            long sleepTime = TimeUnit.SECONDS.toMillis(SLEEP_SECONDS) + timeShift;

            pointHistory.add(awardedXp);
            OptionalDouble averagePoint = pointHistory.stream().mapToDouble(a -> a).average();
            double avg = averagePoint.isPresent() ? averagePoint.getAsDouble() : 0;
            double leftAttempts = limit / avg;

            pauseHistory.add(sleepTime);
            OptionalDouble averageTime = pauseHistory.stream().mapToDouble(a -> a).average();
            double avgTime = averageTime.isPresent() ? averageTime.getAsDouble() : 0;
                LocalDateTime eta = LocalDateTime.now().plus(Double.valueOf(avgTime * leftAttempts).longValue(), ChronoUnit.MILLIS);


            System.out.printf("#%1$3d > %2$d | eta: [%3$.1f] %4$tF %4$tT | %5$s | awarded: %6$2d | left: %7$4d | pause for %8$4ds.%n",
                    count, response.code(), leftAttempts, eta, param, awardedXp, limit, sleepTime / 1000);
            Thread.sleep(sleepTime);
        }
    }

    @NotNull
    private static Response getResponse(Param param) throws IOException {
        final OkHttpClient client = new OkHttpClient().newBuilder().build();
        final MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        final String content = String.format("{\"awardXp\":true,\"completedBonusChallenge\":true,\"fromLanguage\":\"en\",\"illustrationFormat\":\"svg\",\"isV2Story\":false,\"learningLanguage\":\"%s\",\"masterVersion\":false,\"maxScore\":6,\"mode\":\"%s\",\"numHintsUsed\":0,\"score\":6,\"startTime\":%d,\"supportedPartCompleteSubslideTypes\":\"story-complete,part-complete,next-part-unlocked\"}",
                param.getLang(), param.getMode(), param.getStartTime());
        final RequestBody body = RequestBody.create(mediaType, content);
        final Request request = new Request.Builder()
                .url(String.format("https://stories.duolingo.com/api2/stories/%s/complete", param.getLink()))
                .method("POST", body)
                .addHeader("authority", "stories.duolingo.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("accept-language", "en-US,en;q=0.9,ru;q=0.8,he;q=0.7,uk;q=0.6,und;q=0.5,la;q=0.4,da;q=0.3,es;q=0.2")
                .addHeader("authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjYzMDcyMDAwMDAsImlhdCI6MCwic3ViIjo0NjU1MjM1MDF9.UGfC_5Zv3m1fFZaSia5dh3u3dPDyGegPs2Ziw-eS4Y4")
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/json;charset=UTF-8")
                .addHeader("cookie", "lang=en; OptanonConsent=isGpcEnabled=0&datestamp=Sat+Jun+04+2022+23%3A23%3A24+GMT%2B0300+(Israel+Daylight+Time)&version=6.16.0&isIABGlobal=false&consentId=b2595719-beb7-417d-b1a1-ae76ea789238&interactionCount=0&landingPath=https%3A%2F%2Fwww.duolingo.com%2F&groups=C0001%3A1%2CC0002%3A1%2CC0003%3A1%2CC0004%3A1&hosts=H3%3A1%2CH14%3A1%2CH11%3A1%2CH1%3A1%2CH15%3A1%2CH6%3A1%2CH22%3A1%2CH2%3A1%2CH7%3A1%2CH16%3A1%2CH9%3A1%2CH18%3A1%2CH10%3A1%2CH12%3A1%2CH13%3A1; tsl=1654374204985; lu=https://www.duolingo.com/; initial_referrer=$direct; lr=; lp=splash; _gcl_au=1.1.1975766100.1654374206; _ga=GA1.2.1875234248.1654374206; _gid=GA1.2.1412002740.1654374206; __adal_id=cb03693f-84a1-451d-bf7a-a56fc1d9bba4.1654374206.1.1654374206.1654374206.6b5a33f2-0364-47b1-a6d7-780351a36d33; __adal_ses=*; __adal_ca=so%3Ddirect%26me%3Dnone%26ca%3Ddirect%26co%3D%28not%2520set%29%26ke%3D%28not%2520set%29; __adal_cw=1654374206044; jwt_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjYzMDcyMDAwMDAsImlhdCI6MCwic3ViIjo0NjU1MjM1MDF9.UGfC_5Zv3m1fFZaSia5dh3u3dPDyGegPs2Ziw-eS4Y4; csrf_token=Ijc0Y2QxOGIzZTE4NTQwM2NhNGExMjY3MmVkOWY0ZDcxIg==; logged_out_uuid=465523501; logged_in=true")
                .addHeader("origin", "https://www.duolingo.com")
                .addHeader("pragma", "no-cache")
                .addHeader("referer", "https://www.duolingo.com/")
                .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"102\", \"Google Chrome\";v=\"102\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-site")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
                .addHeader("x-amzn-trace-id", "User=465523501")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .build();

        return client.newCall(request).execute();
    }

    private static int extractAwardedXp(JSONParser jsonParser, Response response) throws IOException, ParseException {
        final ResponseBody responseBody = response.body();
        final BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        final Buffer buffer = source.buffer();
        final String result = buffer.clone().readString(StandardCharsets.UTF_8);
        final JSONArray resultArray = new JSONArray();
        resultArray.add(jsonParser.parse(result));

        return Integer.parseInt(String.valueOf(((JSONObject) resultArray.get(0)).get("awardedXp")));
    }
}