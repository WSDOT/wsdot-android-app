/*
 * Copyright (c) 2012 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockFragment;

public class CameraVideoFragment extends SherlockFragment {
	
	private String mVideoPath;
    private VideoView mVideoView;
	private ProgressBar mLoadingSpinner;
	private ViewGroup mRootView;
	private String mCameraName = "cameraVideo.mp4";
	
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Bundle args = activity.getIntent().getExtras();
		String url = args.getString("url");
		
		int slashIndex = url.lastIndexOf("/");
		int dotIndex = url.lastIndexOf(".");
		String cameraName = url.substring(slashIndex + 1, dotIndex);
		
		mVideoPath = "http://images.wsdot.wa.gov/nwvideo/" + cameraName + ".mp4";
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.videoview, null);
		
        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        mLoadingSpinner = (ProgressBar) mRootView.findViewById(R.id.loading_spinner);		
        mVideoView = (VideoView) mRootView.findViewById(R.id.surface_view);
        
		return mRootView;
	}	
	
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		new GetCameraVideo().execute();
	}

	private class GetCameraVideo extends AsyncTask<Void, Void, Boolean> {

		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}

	    protected void onCancelled() {
	        Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		protected Boolean doInBackground(Void... params) {
			return loadVideoFromNetwork(mVideoPath);
		}
		
		protected void onPostExecute(Boolean result) {
			mLoadingSpinner.setVisibility(View.GONE);
			
			if (result) {
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
		        		Toast.makeText(getActivity(), "Error occured", 500).show();
		 				return false;
		 			}
		 		});
			}
			
		}
	}
	
    private boolean loadVideoFromNetwork(String url) {
    	FileOutputStream fos = null;
        
        try {
        	HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        	connection.setRequestProperty("User-agent","Mozilla/4.0");
        	connection.connect();
            InputStream inputStream = connection.getInputStream();
            
            fos = getActivity().openFileOutput(mCameraName, Context.MODE_WORLD_READABLE);
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
}
