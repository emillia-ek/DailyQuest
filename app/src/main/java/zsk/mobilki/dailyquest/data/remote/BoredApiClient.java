package zsk.mobilki.dailyquest.data.remote;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoredApiClient {

    private static final String RANDOM_ACTIVITY_URL = "https://bored-api.appbrewery.com/random";

    public interface Callback {
        void onSuccess(String activityTitle);
        void onFailure(Exception e);
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private static final String[] FALLBACK_QUESTS = {
            "Do 20 squats and stretch for 5 minutes",
            "Drink at least 2 liters of water today",
            "Read 10 pages of a book",
            "Tidy your desk or one drawer",
            "Write down 3 things you are grateful for today",
            "Go for a 20-minute walk outside",
            "Stay off social media for the next 2 hours",
            "Do 15 push-ups or a 30-second plank",
            "Clean your phone screen and computer keyboard",
            "Call an old friend or a family member",
            "Learn 5 new words in another language",
            "Organize your email inbox and delete spam",
            "Meditate quietly for 5 minutes",
            "Draw a quick sketch of something near you",
            "Listen to one educational podcast episode",
            "Plan your goals and tasks for the next week",
            "Prepare a healthy meal with vegetables",
            "Do 10 minutes of intense cardio",
            "Clean up your phone photo gallery",
            "Spend 15 minutes learning something new online"
    };

    public void fetchDailyQuestOrFallback(Callback callback) {
        executorService.execute(() -> {
            try {
                URL url = new URL(RANDOM_ACTIVITY_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(4000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject json = new JSONObject(response.toString());
                    String activity = json.optString("activity", "");
                    
                    if (!activity.isEmpty()) {
                        mainThreadHandler.post(() -> callback.onSuccess(activity));
                        return;
                    }
                }
                throw new Exception("API response not OK or empty");

            } catch (Exception e) {
                mainThreadHandler.post(() -> {
                    String fallbackQuest = getRandomFallbackQuest();
                    callback.onSuccess(fallbackQuest);
                });
            }
        });
    }

    private String getRandomFallbackQuest() {
        int index = new Random().nextInt(FALLBACK_QUESTS.length);
        return FALLBACK_QUESTS[index];
    }

}
