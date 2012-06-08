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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

public class CameraImageFragment extends SherlockFragment {

	private String mUrl;
	private ViewGroup mRootView;
	private ProgressBar mLoadingSpinner;
	private ImageView mImage;
	private String mTitle;
	private String mCameraName = "cameraImage.jpg";
	private ShareActionProvider actionProvider;
	private GetCameraImage getCameraImage;
	
	static final private int MENU_ITEM_REFRESH = Menu.FIRST;
	static final private int MENU_ITEM_STAR = Menu.FIRST + 1;
	static final private int MENU_ITEM_REPORT_IMAGE = Menu.FIRST + 2;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		Bundle args = activity.getIntent().getExtras();
		mTitle = args.getString("title");
		String path = args.getString("url");    
		String[] param = path.split(",");
		mUrl = param[0];
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setRetainInstance(true);
		setHasOptionsMenu(true);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.camera_dialog, null);
		
        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        mLoadingSpinner = (ProgressBar) mRootView.findViewById(R.id.loading_spinner);		
        mImage = (ImageView) mRootView.findViewById(R.id.image);
        
		return mRootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getCameraImage = new GetCameraImage();
		getCameraImage.execute();
	}
    
	@Override
	public void onPause() {
		super.onPause();
		
		getCameraImage.cancel(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_action_provider, menu);

        // Set file with share history to the provider and set the share intent.
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        //actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        actionProvider.setShareHistoryFileName(null);
    	
		menu.add(0, MENU_ITEM_REFRESH, menu.size(), R.string.description_refresh)
			.setIcon(R.drawable.ic_menu_refresh)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add(0, MENU_ITEM_STAR, menu.size(), R.string.description_star)
			.setIcon(R.drawable.ic_menu_star)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);        
        
        menu.add(0, MENU_ITEM_REPORT_IMAGE, menu.size(), R.string.description_report_image)
			.setIcon(R.drawable.ic_menu_report_image)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT); 
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case MENU_ITEM_REFRESH:
			mImage.setImageDrawable(null);
			getCameraImage = new GetCameraImage();
			getCameraImage.execute();
			return true;
		case MENU_ITEM_STAR:
			Toast.makeText(getSherlockActivity(), "Starred", Toast.LENGTH_SHORT).show();
			return true;
		case MENU_ITEM_REPORT_IMAGE:
			Toast.makeText(getSherlockActivity(), "Report Image", Toast.LENGTH_SHORT).show();
			return true;			
		}
		
		return super.onOptionsItemSelected(item);
	}    
    
	private Intent createShareIntent() {
		File f = new File(getActivity().getFilesDir(), mCameraName);
	    ContentValues values = new ContentValues(2);
	    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
	    values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
	    Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);		

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTitle);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mTitle);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		
        return shareIntent;
	}	
	
	private class GetCameraImage extends AsyncTask<String, Void, Drawable> {

		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onCancelled(Drawable result) {
			super.onCancelled(result);
		}

		protected void onCancelled() {
	        Toast.makeText(getActivity(), "Cancelled.", Toast.LENGTH_SHORT).show();
	    }
		
		@SuppressWarnings("deprecation")
		protected Drawable doInBackground(String... params) {
	    	FileOutputStream fos = null;
	    	Bitmap image;
	        
	        try {
	        	HttpURLConnection connection = (HttpURLConnection) new URL(mUrl).openConnection();
	        	connection.setRequestProperty("User-agent","Mozilla/4.0");
	        	connection.connect();
	            InputStream input = connection.getInputStream();
	            image = BitmapFactory.decodeStream(input);
	            fos = getActivity().openFileOutput(mCameraName, Context.MODE_WORLD_READABLE);
	            image.compress(Bitmap.CompressFormat.JPEG, 75, fos);
				fos.flush();
				fos.close();            
		    } catch (Exception e) {
		        Log.e("CameraImageFragment", "Error retrieving camera image", e);
		        image = BitmapFactory.decodeResource(getResources(), R.drawable.camera_offline);
		    }
        
		    return new BitmapDrawable(image);
		}
		
		protected void onPostExecute(Drawable result) {
	        if (!isCancelled()) {
				mLoadingSpinner.setVisibility(View.GONE);
				mImage.setImageDrawable(result);
				actionProvider.setShareIntent(createShareIntent());
	        }
		}
	}
}
