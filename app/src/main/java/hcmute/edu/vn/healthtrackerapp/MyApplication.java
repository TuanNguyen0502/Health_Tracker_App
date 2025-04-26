package hcmute.edu.vn.healthtrackerapp;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dd1jxbzf1");
        config.put("api_key", "231995763463355");
        config.put("api_secret", "JwhusLwA3raMTU8diso2Z_PhkR0");

        MediaManager.init(this, config);
    }
}
