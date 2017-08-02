/*
 * Copyright (c) 2017 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui.callout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;

public class CalloutFragment extends Fragment implements
        LoaderCallbacks<Drawable>, SwipeRefreshLayout.OnRefreshListener {
    
    private static final String TAG = CalloutFragment.class.getSimpleName();
    private static String mUrl;
    private ViewGroup mRootView;
    private ImageView mImage;
    private static String mCalloutImageName = "calloutImage.jpg";
    private static SwipeRefreshLayout swipeRefreshLayout;
    private Tracker mTracker;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        Bundle args = activity.getIntent().getExtras();
        mUrl = args.getString("url");
        
    	mTracker = ((WsdotApplication) this.getActivity().getApplication()).getDefaultTracker();
        mTracker.setScreenName("/TrafficMap/Callout/" + mUrl);
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tell the framework to try to keep this fragment around
        // during a configuration change.       
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.camera_dialog, null);
        
        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);
        
        mImage = (ImageView) mRootView.findViewById(R.id.image);
        
        return mRootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.        
        getLoaderManager().initLoader(0, null, this);
    }

    public Loader<Drawable> onCreateLoader(int id, Bundle args) {
        return new CalloutImageLoader(getActivity());
    }

    public void onLoadFinished(Loader<Drawable> loader, Drawable data) {
        swipeRefreshLayout.setRefreshing(false);
        mImage.setImageDrawable(data);
    }

    public void onLoaderReset(Loader<Drawable> loader) {
		swipeRefreshLayout.post(new Runnable() {
			public void run() {
				swipeRefreshLayout.setRefreshing(true);
			}
		});
    }

    public static class CalloutImageLoader extends AsyncTaskLoader<Drawable> {
        
        public CalloutImageLoader(Context context) {
            super(context);
        }

        @SuppressLint("WorldReadableFiles")
        @SuppressWarnings("deprecation")
        @Override
        public Drawable loadInBackground() {
            FileOutputStream fos = null;
            Bitmap image = null;
            
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(mUrl).openConnection();
                connection.setRequestProperty("User-agent","Mozilla/4.0");
                connection.connect();
                InputStream input = connection.getInputStream();
                image = BitmapFactory.decodeStream(input);

                if (image == null) {
                    image = BitmapFactory.decodeResource(getContext()
                            .getResources(), R.drawable.image_placeholder);
                }
                
                fos = getContext().openFileOutput(mCalloutImageName, Context.MODE_WORLD_READABLE);
                image.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();            
            } catch (Exception e) {
                Log.e(TAG, "Error retrieving callout image", e);
            } finally {
                if (image == null) {
                    image = BitmapFactory.decodeResource(getContext()
                            .getResources(), R.drawable.camera_offline);
                }
            }
        
            return new BitmapDrawable(image);
        }

        @Override
        public void deliverResult(Drawable data) {
            /**
             * Called when there is new data to deliver to the client. The
             * super class will take care of delivering it; the implementation
             * here just adds a little more logic.
             */ 
            super.deliverResult(data);
        }
        
        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            
			swipeRefreshLayout.post(new Runnable() {
				public void run() {
					swipeRefreshLayout.setRefreshing(true);
				}
			});
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            
            // Attempt to cancel the current load task if possible.
            cancelLoad();           
        }
        
        @Override
        public void onCanceled(Drawable data) {
            super.onCanceled(data);
        }

        @Override
        protected void onReset() {
            super.onReset();
            
            // Ensure the loader is stopped
            onStopLoading();            
        }       
    }
    
    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

}
