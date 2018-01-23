package gov.wa.wsdot.android.wsdot.util;

import com.crashlytics.android.Crashlytics;

public class MyLogger {
    public static void crashlyticsLog(String category, String action, String label, long value) {
        String msg = String.format("%s|%s|%s|%s", category, action, label, value);
        Crashlytics.log(msg);
    }
}
