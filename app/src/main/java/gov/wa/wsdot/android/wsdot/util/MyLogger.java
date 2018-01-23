package gov.wa.wsdot.android.wsdot.util;

import com.crashlytics.android.Crashlytics;

import gov.wa.wsdot.android.wsdot.BuildConfig;

public class MyLogger {
    public static void crashlyticsLog(String category, String action, String label, long value) {
        String msg = String.format("%s|%s|%s|%s", category, action, label, value);
        if (BuildConfig.DEBUG) {
            Crashlytics.log(1, "Crashlytics logging", msg);
        } else {
            Crashlytics.log(msg);
        }
    }
}
