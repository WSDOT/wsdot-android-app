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
import gov.wa.wsdot.android.wsdot.shared.VideoItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

public class VideoFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<VideoItem>> {

	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private static final String DEBUG_TAG = "Video";
	private static ArrayList<VideoItem> videoItems = null;
	private static VideoItemAdapter mAdapter;
	private static View mLoadingSpinner;
	ActionMode mActionMode;
	private View mEmptyView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
		setHasOptionsMenu(true);        
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/News & Social Media/Video");
    }
	
    @SuppressWarnings("deprecation")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

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
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				String videoId = videoItems.get(position).getId();
				String url = "http://www.youtube.com/watch?v=" + videoId;				
				mActionMode = getSherlockActivity().startActionMode(new ActionModeCallback(url));
				
				return true;
			}
			
		});
		
		mAdapter = new VideoItemAdapter(getActivity());
		setListAdapter(mAdapter);
		
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);		
	}

	private final class ActionModeCallback implements ActionMode.Callback {
		private String mText;
		
		public ActionModeCallback(String text) {
			this.mText = text;
		}

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.share_action_provider, menu);
	        // Set file with share history to the provider and set the share intent.
	        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
	        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
	        //actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
	        actionProvider.setShareHistoryFileName(null);
	        // Note that you can set/change the intent any time,
	        // say when the user has selected an image.
	        actionProvider.setShareIntent(createShareIntent(mText));
			
	        return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			mode.finish();
			return true;
		}

		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
		
	}

	private Intent createShareIntent(String mText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mText);
        
        return shareIntent;
	}	
	
    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
    	inflater.inflate(R.menu.refresh, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_refresh:
			getLoaderManager().restartLoader(0, null, this);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public Loader<ArrayList<VideoItem>> onCreateLoader(int id, Bundle args) {
		return new VideoItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<VideoItem>> loader, ArrayList<VideoItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);

		if (!data.isEmpty()) {
			mAdapter.setData(data);
		} else {
		    TextView t = (TextView) mEmptyView;
			t.setText(R.string.no_connection);
			getListView().setEmptyView(mEmptyView);
		}
	}

	public void onLoaderReset(Loader<ArrayList<VideoItem>> loader) {
		mAdapter.setData(null);
	}
	
	/**
	 * A custom Loader that loads the YouTube videos from the server.
	 */	
	public static class VideoItemsLoader extends AsyncTaskLoader<ArrayList<VideoItem>> {

		public VideoItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<VideoItem> loadInBackground() {
			videoItems = new ArrayList<VideoItem>();
			VideoItem i = null;
			
			try {
				URL url = new URL("http://gdata.youtube.com/feeds/api/users/wsdot/uploads?v=2&alt=jsonc&max-results=10");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject data = obj.getJSONObject("data");			
				JSONArray items = data.getJSONArray("items");
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					JSONObject thumbnail = item.getJSONObject("thumbnail");
					i = new VideoItem();
					i.setId(item.getString("id"));
					i.setTitle(item.getString("title"));
					i.setDescription(item.getString("description"));
					i.setViewCount(item.getString("viewCount"));
					
					BufferedInputStream ins = new BufferedInputStream(new URL(thumbnail.getString("sqDefault")).openStream(), IO_BUFFER_SIZE);
                    final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                    BufferedOutputStream out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
                    copy(ins, out);
                    out.flush();
                    final byte[] rawData = dataStream.toByteArray();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(rawData, 0, rawData.length);                        

                    @SuppressWarnings("deprecation")
					final Drawable image = new BitmapDrawable(bitmap);
                    i.setThumbNail(image);
					
					videoItems.add(i);
				}				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			
			return videoItems;
		}

		@Override
		public void deliverResult(ArrayList<VideoItem> data) {
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
		public void onCanceled(ArrayList<VideoItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
	        // Ensure the loader is stopped
	        onStopLoading();
		}
		
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String videoId = videoItems.get(position).getId();
		String url = "http://www.youtube.com/watch?v=" + videoId;
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}
	
	private class VideoItemAdapter extends ArrayAdapter<VideoItem> {
		private final LayoutInflater mInflater;
		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");		

        public VideoItemAdapter(Context context) {
        	super(context, R.layout.video_row);
        	mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<VideoItem> data) {
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
	        ViewHolder holder = null;
        	
        	if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.video_row, null);
	            holder = new ViewHolder();
	            holder.title = (TextView) convertView.findViewById(R.id.title);
	            holder.title.setTypeface(tfb);
	            holder.description = (TextView) convertView.findViewById(R.id.description);
	            holder.description.setTypeface(tf);
	            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
	            
	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        VideoItem item = getItem(position);

	        holder.title.setText(item.getTitle());
        	holder.description.setText(item.getDescription());
        	holder.icon.setImageDrawable(item.getThumbNail());	        
	        
        	return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView title;
		public TextView description;
		public ImageView icon;
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
