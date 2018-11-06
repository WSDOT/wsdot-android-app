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

package gov.wa.wsdot.android.wsdot.ui.camera;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;

public class CameraVideoFragment extends Fragment implements
        LoaderCallbacks<Boolean> {
	
	private static String mVideoPath;
    private VideoView mVideoView;
	private static ProgressBar mLoadingSpinner;
	private ViewGroup mRootView;
	private static String mCameraName = "cameraVideo.mp4";
	
    @Override
	public void onAttach(Context context) {
		super.onAttach(context);

		Bundle args = getArguments();
		String url = args.getString("url");
		
		int slashIndex = url.lastIndexOf("/");
		int dotIndex = url.lastIndexOf(".");
		String cameraName = url.substring(slashIndex + 1, dotIndex);
		
		mVideoPath = APIEndPoints.CAMERA_VIDEOS + cameraName + ".mp4";
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
		mRootView = (ViewGroup) inflater.inflate(R.layout.videoview, null);
		
        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = (ProgressBar) mRootView.findViewById(R.id.progress_bar);
        mVideoView = (VideoView) mRootView.findViewById(R.id.surface_view);
        
		return mRootView;
	}	
	
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);
	}

	public Loader<Boolean> onCreateLoader(int id, Bundle args) {
		return new CameraVideoLoader(getActivity());
	}

	public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
		mLoadingSpinner.setVisibility(View.GONE);
		
		if (data) {
			File f = new File(getActivity().getFilesDir(), mCameraName);
			
			mVideoView.setMediaController(new MediaController(getActivity()));
			mVideoView.setVideoURI(Uri.fromFile(f));
			mVideoView.requestFocus();
			
	        mVideoView.setOnPreparedListener(new OnPreparedListener() {
				public void onPrepared(MediaPlayer arg0) {
					mLoadingSpinner.setVisibility(View.GONE);
					mVideoView.start();
				}
	        });
	        
	        mVideoView.setOnErrorListener(new OnErrorListener() {
	        	public boolean onError(MediaPlayer mp, int what, int extra) {
	        		Toast.makeText(getActivity(), "Error occured", Toast.LENGTH_SHORT).show();
	 				return false;
	 			}
	 		});
		}		
	}

	public void onLoaderReset(Loader<Boolean> loader) {
		mLoadingSpinner.setVisibility(View.GONE);		
	}
	
	public static class CameraVideoLoader extends AsyncTaskLoader<Boolean> {
		private Context mContext;
		
		public CameraVideoLoader(Context context) {
			super(context);
			
			this.mContext = context;
		}

		@Override
		public Boolean loadInBackground() {
	    	FileOutputStream fos = null;
	        
	        try {
	        	HttpURLConnection connection = (HttpURLConnection) new URL(mVideoPath).openConnection();
	        	connection.setRequestProperty("User-agent","Mozilla/4.0");
	        	connection.connect();
	            InputStream inputStream = connection.getInputStream();
	            
	            fos = mContext.openFileOutput(mCameraName, Context.MODE_WORLD_READABLE);
	            byte[] buffer = new byte[1024];
	            int bufferLength = 0; //used to store a temporary size of the buffer

	            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
	                fos.write(buffer, 0, bufferLength);
	            }

	        } catch (Exception e) {
		        Log.e("CameraVideo", "Error retrieving camera video", e);
		        
		        return false;
		    }
	        
	        try {
				fos.flush();
				fos.close();
			} catch (IOException e) {
				Log.e("CameraVideoFragment", e.toString(), e);
			}         
	        
			return true;	    

		}

		@Override
		public void deliverResult(Boolean data) {
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
			
			mLoadingSpinner.setVisibility(View.VISIBLE);
			forceLoad();
		}

		@Override
		protected void onStopLoading() {
			super.onStopLoading();
			
	        // Attempt to cancel the current load task if possible.
	        cancelLoad();			
		}
		
		@Override
		public void onCanceled(Boolean data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
	        // Ensure the loader is stopped
	        onStopLoading();			
		}		

		
	}
}
