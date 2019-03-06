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
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import androidx.multidex.MultiDexApplication;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import gov.wa.wsdot.android.wsdot.BuildConfig;
import gov.wa.wsdot.android.wsdot.di.AppInjector;
import gov.wa.wsdot.android.wsdot.util.MyNotificationManager;
import io.fabric.sdk.android.Fabric;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app
 */
public class WsdotApplication extends MultiDexApplication implements HasActivityInjector, HasSupportFragmentInjector, HasServiceInjector {

    final String TAG = WsdotApplication.class.getSimpleName();

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidActivityInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidFragmentInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        AppInjector.init(this);

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


    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidActivityInjector;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidFragmentInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }


}