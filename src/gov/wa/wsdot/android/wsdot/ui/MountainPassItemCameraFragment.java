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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class MountainPassItemCameraFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<Drawable>> {
	
	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private static final String DEBUG_TAG = "MountainPassItemPhotos";
	private static List<CameraItem> remoteImages;
    private static ArrayList<Drawable> bitmapImages;
    private ViewGroup mRootView;
    private static String camerasArray;
    private static CameraImageAdapter mAdapter;
	private static View mLoadingSpinner;
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		Bundle args = activity.getIntent().getExtras();
		camerasArray = args.getString("Cameras");
	    
	    remoteImages = new ArrayList<CameraItem>();
	    bitmapImages  = new ArrayList<Drawable>();
	}    
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);
		
        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        mLoadingSpinner = mRootView.findViewById(R.id.loading_spinner);
		
		return mRootView;
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

	public Loader<ArrayList<Drawable>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple.
		return new CameraImagesLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<Drawable>> loader,
			ArrayList<Drawable> data) {

		mLoadingSpinner.setVisibility(View.GONE);
		mAdapter.setData(data);	
	}

	public void onLoaderReset(Loader<ArrayList<Drawable>> loader) {
		bitmapImages.clear();
		mAdapter.setData(null);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), CameraActivity.class);
		b.putInt("id", remoteImages.get(position).getCameraId());
		intent.putExtras(b);
		
		startActivity(intent);
	}

	/**
	 * A custom Loader that loads all of the camera images for this mountain pass.
	 */		
	public static class CameraImagesLoader extends AsyncTaskLoader<ArrayList<Drawable>> {
		private Context mContext;
		
		public CameraImagesLoader(Context context) {
			super(context);
			
			this.mContext = context;
		}

		@Override
		public ArrayList<Drawable> loadInBackground() {
		   	BufferedInputStream in;
	        BufferedOutputStream out;
	        JSONArray cameras;
	        CameraItem c = null;

	        try {
				cameras = new JSONArray(camerasArray);
				for (int k=0; k < cameras.length(); k++) {
					JSONObject camera = cameras.getJSONObject(k);
					c = new CameraItem();
					c.setImageUrl(camera.getString("url"));
					c.setCameraId(camera.getInt("id"));
					remoteImages.add(c);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
	        
	    	for (int i=0; i < remoteImages.size(); i++) {
	    		Bitmap bitmap = null;
	            try {
	                in = new BufferedInputStream(new URL(remoteImages.get(i).getImageUrl()).openStream(), IO_BUFFER_SIZE);
	                final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
	                out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
	                copy(in, out);
	                out.flush();
	                final byte[] data = dataStream.toByteArray();
	                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
	    	    } catch (Exception e) {
	    	        Log.e(DEBUG_TAG, "Error retrieving camera images", e);
	    	        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.camera_offline);
	    	    } finally {
	    	    	@SuppressWarnings("deprecation")
	    	    	final Drawable image = new BitmapDrawable(bitmap);
	    	    	bitmapImages.add(image);
	    	    }
	    	}

			return bitmapImages;
		}

		/**
	     * Called when there is new data to deliver to the client. The
	     * super class will take care of delivering it; the implementation
	     * here just adds a little more logic.
	     */
		@Override
		public void deliverResult(ArrayList<Drawable> data) {
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
		public void onCanceled(ArrayList<Drawable> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
	        // Ensure the loader is stopped
	        onStopLoading();			
		}
		
	}

	private class CameraImageAdapter extends ArrayAdapter<Drawable> {
		private final LayoutInflater mInflater;
		
		public CameraImageAdapter(Context context) {
			super(context, R.layout.list_item_resizeable_image);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
        public void setData(ArrayList<Drawable> data) {
            clear();
            if (data != null) {
                //addAll(data); // Only in API level 11
                notifyDataSetChanged();
                for (int i=0; i < data.size(); i++) {
                	add(data.get(i));
                }
                notifyDataSetChanged();                
            }
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.list_item_resizeable_image, null);
	        }
	        
	        Drawable item = getItem(position);
	        
	        if (item != null) {
	        	ResizeableImageView iv = (ResizeableImageView) convertView.findViewById(R.id.image);
	        	iv.setImageDrawable(item.getCurrent());
	        }
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public ImageView iv;
	}        
	
    /**
     * Copy the content of the input stream into the output stream, using a
     * temporary byte array buffer whose size is defined by
     * {@link #IO_BUFFER_SIZE}.
     * 
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     * @throws IOException If any error occurs during the copy.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }
   
}
