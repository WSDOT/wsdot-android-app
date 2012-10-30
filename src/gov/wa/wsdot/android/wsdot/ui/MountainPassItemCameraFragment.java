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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.ui.widget.ResizeableImageView;
import gov.wa.wsdot.android.wsdot.util.ImageManager;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class MountainPassItemCameraFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<CameraItem>> {
	
	@SuppressWarnings("unused")
	private static final String DEBUG_TAG = "MountainPassItemPhotos";
    private static ArrayList<CameraItem> bitmapImages;
	private View mEmptyView;
    private static String camerasArray;
    private static CameraImageAdapter mAdapter;
	private static View mLoadingSpinner;
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		Bundle args = activity.getIntent().getExtras();
		camerasArray = args.getString("Cameras");
	}    
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);
		
        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        mEmptyView = root.findViewById( R.id.empty_list_view );
		
		return root;
	}    
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new CameraImageAdapter(getActivity());
		setListAdapter(mAdapter);
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.		
		getLoaderManager().initLoader(0, null, this);
	}

	public Loader<ArrayList<CameraItem>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple.
		return new CameraImagesLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<CameraItem>> loader, ArrayList<CameraItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);

		if (!data.isEmpty()) {
			mAdapter.setData(data);
		} else {
		    TextView t = (TextView) mEmptyView;
			t.setText(R.string.no_connection);
			getListView().setEmptyView(mEmptyView);
		}
	}

	public void onLoaderReset(Loader<ArrayList<CameraItem>> loader) {
		mAdapter.setData(null);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), CameraActivity.class);
		b.putInt("id", bitmapImages.get(position).getCameraId());
		intent.putExtras(b);
		
		startActivity(intent);
	}

	/**
	 * A custom Loader that loads all of the camera images for this mountain pass.
	 */		
	public static class CameraImagesLoader extends AsyncTaskLoader<ArrayList<CameraItem>> {
		
		public CameraImagesLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<CameraItem> loadInBackground() {
			bitmapImages  = new ArrayList<CameraItem>();
	        JSONArray cameras;
	        CameraItem c = null;

	        try {
				cameras = new JSONArray(camerasArray);
				int numCameras = cameras.length();
				for (int k=0; k < numCameras; k++) {
					JSONObject camera = cameras.getJSONObject(k);
					c = new CameraItem();
					c.setImageUrl(camera.getString("url"));
					c.setCameraId(camera.getInt("id"));
					
					bitmapImages.add(c);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return bitmapImages;
		}

		/**
	     * Called when there is new data to deliver to the client. The
	     * super class will take care of delivering it; the implementation
	     * here just adds a little more logic.
	     */
		@Override
		public void deliverResult(ArrayList<CameraItem> data) {
			super.deliverResult(data);
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();

			mAdapter.clear();
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
		public void onCanceled(ArrayList<CameraItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
	        // Ensure the loader is stopped
	        onStopLoading();			
		}
		
	}

	private class CameraImageAdapter extends ArrayAdapter<CameraItem> {
		private final LayoutInflater mInflater;
		private ImageManager imageManager;
		
		public CameraImageAdapter(Context context) {
			super(context, R.layout.list_item_resizeable_image);
			
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			imageManager = new ImageManager(context, 5 * DateUtils.MINUTE_IN_MILLIS); // Cache for 5 minutes.
		}
		
        public void setData(ArrayList<CameraItem> data) {
            clear();
            if (data != null) {
                //addAll(data); // Only in API level 11
                notifyDataSetChanged();
                int size = data.size();
                for (int i=0; i < size; i++) {
                	add(data.get(i));
                }
                notifyDataSetChanged();                
            }
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	ViewHolder holder = null;
        	
        	if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.list_item_resizeable_image, null);
	            holder = new ViewHolder();
	            holder.image = (ResizeableImageView) convertView.findViewById(R.id.image);
	            
	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        CameraItem item = getItem(position);
	        
        	holder.image.setTag(item.getImageUrl());
        	imageManager.displayImage(item.getImageUrl(), getActivity(), holder.image);
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public ImageView image;
	}        
   
}
