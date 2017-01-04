package com.kksionek.queuedroid;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.kksionek.queuedroid.model.Settings;

public class QueueApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (Settings.isFacebookEnabled(getBaseContext())) {
            FacebookSdk.sdkInitialize(getApplicationContext());
            AppEventsLogger.activateApp(this);
        }
    }
}
