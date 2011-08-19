/*
 * Copyright (c) 2011 Washington State Department of Transportation
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

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

public class CameraVideo extends Activity {
	
    private String path;
    private VideoView mVideoView;
    private ProgressBar mProgress;
    private TextView mLoadingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoview);
        ((TextView)findViewById(R.id.sub_section)).setText("Camera Video");
        mVideoView = (VideoView)findViewById(R.id.surface_view);
        mLoadingMessage = (TextView)findViewById(R.id.loading_message);
        mProgress = (ProgressBar)findViewById(R.id.progress);
        
        mProgress.setVisibility(View.VISIBLE);
        mLoadingMessage.setVisibility(View.VISIBLE);
        
        path = getIntent().getExtras().getString("url");

        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setVideoPath(path);
        mVideoView.requestFocus();       
        mVideoView.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer arg0) {
				mProgress.setVisibility(View.INVISIBLE);
				mLoadingMessage.setVisibility(View.INVISIBLE);
				mVideoView.start();
			}
        });
        
    }
}
