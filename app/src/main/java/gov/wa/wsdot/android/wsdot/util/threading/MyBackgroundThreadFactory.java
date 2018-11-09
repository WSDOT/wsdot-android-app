package gov.wa.wsdot.android.wsdot.util.threading;

import java.util.concurrent.ThreadFactory;

public class MyBackgroundThreadFactory implements ThreadFactory {
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    }
}