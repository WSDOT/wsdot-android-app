package gov.wa.wsdot.android.wsdot.database.borderwaits;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.lang.ref.WeakReference;

public class BorderWaitRepository {

    private BorderWaitDataSource mBorderWaitDataSource;

    private BorderWaitEntity[] mCachedBorderWaits;

    public BorderWaitRepository(BorderWaitDataSource userDataSource) {
        mBorderWaitDataSource = userDataSource;
    }

    public void getBorderWaitsFor(final String direction, LoadBorderWaitsCallback callback) {
        final WeakReference<LoadBorderWaitsCallback> loadBorderWaitsCallback = new WeakReference<>(callback);



        AsyncTask.execute(new Runnable() {
            public void run() {

                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);

                final BorderWaitEntity[] borderWaits = mBorderWaitDataSource.getBorderWaitsFor(direction);

                // notify on the main thread
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        final LoadBorderWaitsCallback borderWaitsCallback = loadBorderWaitsCallback.get();
                        if (borderWaitsCallback == null) {
                            Log.e("4", "callback null");
                            return;
                        }
                        if (borderWaits == null) {
                            Log.e("4", "no data...?");
                            borderWaitsCallback.onDataNotAvailable();
                        } else {
                            Log.e("4", "success!");
                            mCachedBorderWaits = borderWaits;
                            borderWaitsCallback.onBorderWaitsLoaded(mCachedBorderWaits);
                        }
                    }
                };
                mainHandler.post(myRunnable);
            }
        });
    }

    public void getStarredBorderWaits(LoadBorderWaitsCallback callback) {
        final WeakReference<LoadBorderWaitsCallback> loadBorderWaitsCallback = new WeakReference<>(callback);

        new Runnable() {
            public void run() {

                final BorderWaitEntity[] borderWaits = mBorderWaitDataSource.getStarredBorderWaits();

                // notify on the main thread
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        final LoadBorderWaitsCallback borderWaitsCallback = loadBorderWaitsCallback.get();
                        if (borderWaitsCallback == null) {
                            return;
                        }
                        if (borderWaits == null) {
                            borderWaitsCallback.onDataNotAvailable();
                        } else {
                            mCachedBorderWaits = borderWaits;
                            borderWaitsCallback.onBorderWaitsLoaded(mCachedBorderWaits);
                        }
                    }
                };
                mainHandler.post(myRunnable);
            }
        };
    }
}
