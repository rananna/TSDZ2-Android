package casainho.ebike.opensource_ebike_wireless;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import casainho.ebike.opensource_ebike_wireless.data.LogManager;

public class MyApp extends Application {
    private static MyApp instance;
    private static LogManager mLogManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mLogManager = LogManager.initLogs();
    }

    public static MyApp getInstance() {
        return instance;
    }

    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(instance);
    }

    public static LogManager getLogManager() {
        return mLogManager;
    }
}
