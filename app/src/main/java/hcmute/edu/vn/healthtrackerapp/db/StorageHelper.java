package hcmute.edu.vn.healthtrackerapp.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.healthtrackerapp.model.Session;

public class StorageHelper {
    private static final String PREFS_NAME = "HealthData";
    private SharedPreferences prefs;

    public StorageHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void addSession(String date, Session session) {
        String key = "sessions_" + date;
        String json = prefs.getString(key, null);
        JSONArray arr;
        try {
            arr = json == null ? new JSONArray() : new JSONArray(json);
            JSONObject obj = new JSONObject();
            obj.put("startTime", session.getStartTime());
            obj.put("endTime", session.getEndTime());
            obj.put("steps", session.getSteps());
            obj.put("distance", session.getDistance());
            obj.put("calories", session.getCalories());
            arr.put(obj);
            prefs.edit().putString(key, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Session> getSessionsByDate(String date) {
        List<Session> list = new ArrayList<>();
        String key = "sessions_" + date;
        String json = prefs.getString(key, null);
        if (TextUtils.isEmpty(json)) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                long start = obj.getLong("startTime");
                long end = obj.getLong("endTime");
                int steps = obj.getInt("steps");
                float dist = (float) obj.getDouble("distance");
                float cal = (float) obj.getDouble("calories");
                list.add(new Session(start, end, steps, dist, cal));
            }
            // Sort sessions in descending order by start time
            list.sort((s1, s2) -> Long.compare(s2.getStartTime(), s1.getStartTime()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}