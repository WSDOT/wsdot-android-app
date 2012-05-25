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

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

public class CameraVideo extends SherlockActivity {
	
    private String path;
    private Uri mUriPath;
    private String mTitle;
    private VideoView mVideoView;
	private ProgressBar mLoadingSpinner;
	private View mLayout;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mTitle = getIntent().getExtras().getString("title");
        path = getIntent().getExtras().getString("url");
        
        getSupportActionBar().setTitle(mTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.videoview);
        mLayout = findViewById(R.id.layout);
        mLayout.setBackgroundColor(Color.BLACK);
        mVideoView = (VideoView)findViewById(R.id.surface_view);

        mLoadingSpinner = (ProgressBar) findViewById(R.id.progress);
        mLoadingSpinner.setVisibility(View.VISIBLE);
        
        mVideoView.setMediaController(new MediaController(this));
        mUriPath = Uri.parse(path);
        mVideoView.setVideoURI(mUriPath);
        mVideoView.requestFocus();
        
        mVideoView.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer arg0) {
				mLoadingSpinner.setVisibility(View.GONE);
				mVideoView.start();
			}
        });
        
        mVideoView.setOnErrorListener(new OnErrorListener() {
        	public boolean onError(MediaPlayer mp, int what, int extra) {
        		Toast.makeText(CameraVideo.this, "Error occured", 500).show();
 				return false;
 			}
 		});        
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);

        // Set file with share history to the provider and set the share intent.
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        actionProvider.setShareIntent(createShareIntent());
    	
    	return super.onCreateOptionsMenu(menu);
	}
    
	private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("video/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTitle);
        
        return shareIntent;
	}    
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	    	return true;
		}
		
		return super.onOptionsItemSelected(item);
	}   
}
