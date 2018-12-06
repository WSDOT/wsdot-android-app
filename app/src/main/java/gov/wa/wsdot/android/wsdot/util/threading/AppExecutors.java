package gov.wa.wsdot.android.wsdot.util.threading;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
@Singleton
public class AppExecutors {

    private final Executor diskIO;

    private final Executor networkIO;

    private final Executor mainThread;

    private final Executor taskIO;

    public AppExecutors(Executor diskIO, Executor networkIO, Executor taskIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.taskIO = taskIO;
        this.mainThread = mainThread;
    }

    @Inject
    public AppExecutors() {
        this(Executors.newSingleThreadExecutor(new MyBackgroundThreadFactory()),
                Executors.newSingleThreadExecutor(new MyBackgroundThreadFactory()),
                Executors.newSingleThreadExecutor(new MyBackgroundThreadFactory()),
                new MainThreadExecutor());
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor taskIO() { return taskIO; }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}