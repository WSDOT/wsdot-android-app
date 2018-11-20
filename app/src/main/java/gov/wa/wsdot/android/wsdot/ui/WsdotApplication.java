/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.wa.wsdot.android.wsdot.ui;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import gov.wa.wsdot.android.wsdot.BuildConfig;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.AppInjector;
import gov.wa.wsdot.android.wsdot.util.MyNotificationManager;
import gov.wa.wsdot.android.wsdot.util.Utils;
import io.fabric.sdk.android.Fabric;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class WsdotApplication extends Application implements HasActivityInjector, HasSupportFragmentInjector, HasServiceInjector {

    final String TAG = WsdotApplication.class.getSimpleName();

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidActivityInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidFragmentInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        AppInjector.init(this);

        checkForEvent();

        if (BuildConfig.DEBUG) {
            Log.d(WsdotApplication.class.getSimpleName(), "init crashlytics in debug mode");
            final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true) // Enables Crashlytics debugger
                .build();
            Fabric.with(fabric);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MyNotificationManager myNotificationManager = new MyNotificationManager(getApplicationContext());
            myNotificationManager.createMainNotificationChannels();
        }

        //reset driver alert message on app startup.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("KEY_SEEN_DRIVER_ALERT", false);
        editor.apply();

    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidActivityInjector;
    }

    @Override
    public AndroidInjector<android.support.v4.app.Fragment> supportFragmentInjector() {
        return dispatchingAndroidFragmentInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }

    /**
     * checks if we have an active event. If we are in the event date range set event_is_active
     * to true and update theme if necessary.
     */
    private void checkForEvent(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String startDateString = sharedPref.getString(getString(R.string.event_start_date), "1997-01-01");
        String endDateString = sharedPref.getString(getString(R.string.event_end_date), "1997-01-01");
        String dateFormat = "yyyy-MM-dd";

        SharedPreferences.Editor editor = sharedPref.edit();

        if (Utils.currentDateInRange(startDateString, endDateString, dateFormat)) {
          int event_theme_id = sharedPref.getInt(getString(R.string.event_theme_key), 0);
          editor.putInt(getString(R.string.event_theme_key), event_theme_id);
          editor.putBoolean(getString(R.string.event_is_active), true);
          editor.commit();
        } else {
          editor.putBoolean(getString(R.string.event_is_active), false);
          editor.commit();
        }
    }
}