/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;

public class CameraImageFragment extends Fragment implements
        LoaderCallbacks<Drawable>,
        SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = CameraImageFragment.class.getSimpleName();
    private static String mUrl;
	private ViewGroup mRootView;
	private ImageView mImage;
	private String mTitle;
	private int mId;
	private static String mCameraName = "cameraImage.jpg";
	private ShareActionProvider shareAction;
	private boolean mIsStarred = false;
	private ContentResolver resolver;
	private static SwipeRefreshLayout swipeRefreshLayout;

	static final private int MENU_ITEM_STAR = Menu.FIRST;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
		setHasOptionsMenu(true);

        Bundle args = getArguments();
        mId = args.getInt("id");
        mTitle = args.getString("title");
        mUrl = args.getString("url");
        mIsStarred = args.getInt("isStarred") != 0;

        if (savedInstanceState != null) {
            mIsStarred = savedInstanceState.getInt("isStarred") != 0;
        }
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_action_provider, menu);

        // Set file with share history to the provider and set the share intent.
        MenuItem menuItem_Share = menu.findItem(R.id.action_share);
        shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem_Share);
        shareAction.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

		MenuItem menuItem_Star = menu.add(0, MENU_ITEM_STAR, menu.size(), R.string.description_star);
		MenuItemCompat.setShowAsAction(menuItem_Star, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

		if (mIsStarred) {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star_on);
		} else {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star);
		}
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case MENU_ITEM_STAR:
			toggleStar(item);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void toggleStar(MenuItem item) {
		resolver = getActivity().getContentResolver();
		Snackbar added_snackbar = Snackbar
				.make(getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

		Snackbar removed_snackbar = Snackbar
				.make(getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

		if (mIsStarred) {
			item.setIcon(R.drawable.ic_menu_star);
			try {
				ContentValues values = new ContentValues();
				values.put(Cameras.CAMERA_IS_STARRED, 0);
				resolver.update(
						Cameras.CONTENT_URI,
						values,
						Cameras.CAMERA_ID + "=?",
						new String[] {Integer.toString(mId)}
						);
				removed_snackbar.show();
				mIsStarred = false;
	    	} catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("CameraImageFragment", "Error: " + e.getMessage());
	    	}
		} else {
			item.setIcon(R.drawable.ic_menu_star_on);
			try {
				ContentValues values = new ContentValues();
				values.put(Cameras.CAMERA_IS_STARRED, 1);
				resolver.update(
						Cameras.CONTENT_URI,
						values,
						Cameras.CAMERA_ID + "=?",
						new String[] {Integer.toString(mId)}
						);

				added_snackbar.show();
				mIsStarred = true;
			} catch (Exception e) {
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("CameraImageFragment", "Error: " + e.getMessage());
	    	}
		}
	}

	private Intent createShareIntent() {
		File f = new File(getActivity().getFilesDir(), mCameraName);
	    ContentValues values = new ContentValues(2);
	    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
	    values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
        Uri uri = getActivity().getContentResolver().insert(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTitle);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mTitle);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        return shareIntent;
	}


	public Loader<Drawable> onCreateLoader(int id, Bundle args) {
		return new CameraImageLoader(getActivity());
	}


	public void onLoadFinished(Loader<Drawable> loader, Drawable data) {
	    swipeRefreshLayout.setRefreshing(false);
		mImage.setImageDrawable(data);
		try {
		    shareAction.setShareIntent(createShareIntent());
		} catch (NullPointerException e) {
		    Log.e(TAG, "createShareIntent() returned NULL: " + e.getStackTrace());
		}
	}

	public void onLoaderReset(Loader<Drawable> loader) {
	    swipeRefreshLayout.setRefreshing(false);
	}

	public static class CameraImageLoader extends AsyncTaskLoader<Drawable> {

		public CameraImageLoader(Context context) {
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
                            .getResources(), R.drawable.camera_offline);
                }

	            fos = getContext().openFileOutput(mCameraName, Context.MODE_WORLD_READABLE);
	            image.compress(Bitmap.CompressFormat.JPEG, 75, fos);
				fos.flush();
				fos.close();
		    } catch (Exception e) {
		        Log.e("CameraImageFragment", "Error retrieving camera image", e);
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mIsStarred){
            outState.putInt("isStarred",  1);
		}else{
            outState.putInt("isStarred",  0);
		}
		super.onSaveInstanceState(outState);
	}
}
