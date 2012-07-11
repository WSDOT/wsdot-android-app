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

import gov.wa.wsdot.android.wsdot.shared.SeattleIncidentItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class SeattleTrafficAlertsFragment extends SherlockListFragment
	implements LoaderCallbacks<ArrayList<SeattleIncidentItem>> {

	private static final String DEBUG_TAG = "SeattleIncidents";
	private static ArrayList<SeattleIncidentItem> seattleIncidentItems = null;
    private static MyCustomAdapter mAdapter;
    private static View mLoadingSpinner;
    private static List<Integer> blockingCategory = new ArrayList<Integer>();
    private static List<Integer> constructionCategory = new ArrayList<Integer>();
    private static List<Integer> specialCategory = new ArrayList<Integer>();    

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
        setHasOptionsMenu(true);
        
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Traffic Map/Seattle/Seattle Alerts");
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

        return root;
    }
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new MyCustomAdapter(getActivity());
		setListAdapter(mAdapter);
		buildCategories();
		
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.        
        getLoaderManager().initLoader(0, null, this);
	}
	
	private void buildCategories() {
        blockingCategory.add(0); // Traffic conditions
        blockingCategory.add(4); // Incident
        blockingCategory.add(5); // Collision
        blockingCategory.add(6); // Disabled vehicle
        blockingCategory.add(10); // Water over roadway
        blockingCategory.add(11); // Obstruction
        blockingCategory.add(30); // Fallen tree
        
        constructionCategory.add(7); // Closures
        constructionCategory.add(8); // Road work
        constructionCategory.add(9); // Maintenance

        specialCategory.add(2); // Winter driving restriction
        specialCategory.add(12); // Sporting event
        specialCategory.add(13); // Seahawks game
        specialCategory.add(28); // Sounders game
        specialCategory.add(14); // Mariners game
        specialCategory.add(15); // Special event
        specialCategory.add(16); // Restriction
        specialCategory.add(17); // Flammable restriction
        specialCategory.add(29); // Huskies game		
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
	
	public Loader<ArrayList<SeattleIncidentItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new SeattleIncidentItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<SeattleIncidentItem>> loader, ArrayList<SeattleIncidentItem> data) {
		mLoadingSpinner.setVisibility(View.GONE);
		mAdapter.setData(data);		
	}

	public void onLoaderReset(Loader<ArrayList<SeattleIncidentItem>> loader) {
		mAdapter.setData(null);		
	}
	
	/**
	 * A custom Loader that loads all of the Seattle incident alerts from the data server.
	 */	
	public static class SeattleIncidentItemsLoader extends AsyncTaskLoader<ArrayList<SeattleIncidentItem>> {

		public SeattleIncidentItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<SeattleIncidentItem> loadInBackground() {
			seattleIncidentItems = new ArrayList<SeattleIncidentItem>();
			SeattleIncidentItem i = null;
	        
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/SeattleIncidents.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;

				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("incidents");
				JSONArray items = result.getJSONArray("items");
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					i = new SeattleIncidentItem();
					i.setTitle(item.getString("title"));
					i.setDescription(item.getString("description"));
					i.setCategory(item.getInt("category"));
					i.setGuid(item.getInt("guid"));
					
					seattleIncidentItems.add(i);
				}
				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			
			return seattleIncidentItems;
		}
		
		@Override
		public void deliverResult(ArrayList<SeattleIncidentItem> data) {
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
			
			mAdapter.mData.clear();
			mAdapter.notifyDataSetChanged();
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
		public void onCanceled(ArrayList<SeattleIncidentItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
	        // Ensure the loader is stopped
	        onStopLoading();
		}		
		
	}
	
    private class MyCustomAdapter extends BaseAdapter {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
        private ArrayList<String> mData = new ArrayList<String>();
        private LayoutInflater mInflater;
        private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		private Stack<String> blocking = new Stack<String>();
    	private Stack<String> construction = new Stack<String>();
    	private Stack<String> special = new Stack<String>();
    	private Stack<String> closed = new Stack<String>();
    	private Stack<String> amberalert = new Stack<String>();
        
        public MyCustomAdapter(Context context) {
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        public void setData(ArrayList<SeattleIncidentItem> data) {
    		mData.clear();
    		if (data != null) {
                for (int i=0; i < data.size(); i++) {
                	// Check if Traffic Management Center is closed
					if (data.get(i).getCategory().equals(27)) {
						closed.push(data.get(i).getDescription());
						break; // TSMC is closed so stop here
					}
					// Check if there is an active amber alert
					else if (data.get(i).getCategory().equals(24)) {
						amberalert.push(data.get(i).getDescription());
					}
					else if (blockingCategory.contains(data.get(i).getCategory())) {
						blocking.push(data.get(i).getDescription());
					}
	                else if (constructionCategory.contains(data.get(i).getCategory())) {
	                    construction.push(data.get(i).getDescription());
	                }
	                else if (specialCategory.contains(data.get(i).getCategory())) {
	                    special.push(data.get(i).getDescription());
	                }
                }
    			
	        	if (amberalert != null && amberalert.size() != 0) {
	    			mAdapter.addSeparatorItem("Amber Alert");
	    			while (!amberalert.empty()) {
	    				mAdapter.addItem(amberalert.pop());
	    			}
	    		}
	    		if (closed != null && closed.size() == 0) {
	    			mAdapter.addSeparatorItem("Blocking Incidents");				
	    			if (blocking.empty()) {
	    				mAdapter.addItem("None reported");
	    			} else {
	    				while (!blocking.empty()) {
	    					mAdapter.addItem(blocking.pop());
	    				}					
	    			}
	    			mAdapter.addSeparatorItem("Construction Closures");
	    			if (construction.empty()) {
	    				mAdapter.addItem("None reported");
	    			} else {
	    				while (!construction.empty()) {
	    					mAdapter.addItem(construction.pop());
	    				}					
	    			}
	    			mAdapter.addSeparatorItem("Special Events");
	    			if (special.empty()) {
	    				mAdapter.addItem("None reported");
	    			} else {
	    				while (!special.empty()) {
	    					mAdapter.addItem(special.pop());
	    				}					
	    			}
	    		} else {
	    			mAdapter.addItem(closed.pop());
	    		}
	    		mAdapter.notifyDataSetChanged();
    		}
        }        
        
        public void addItem(final String item) {
            mData.add(item);
            notifyDataSetChanged();
        }
 
        public void addSeparatorItem(final String item) {
            mData.add(item);
            // save separator position
            mSeparatorsSet.add(mData.size() - 1);
            notifyDataSetChanged();
        }
        
        @Override
        public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        }
        
        @SuppressWarnings("unused")
		public boolean areAllItemsSelectable() {
        	return false;
        } 
 
        public boolean isEnabled(int position) {  
        	return false;  
        }          
        
        @Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
 
        public int getCount() {
            return mData.size();
        }
 
        public String getItem(int position) {
            return mData.get(position);
        }
 
        public long getItemId(int position) {
            return position;
        }
 
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(R.layout.seattle_incident_item, null);
                        holder.textView = (TextView)convertView.findViewById(R.id.description);
                        holder.textView.setTypeface(tf);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.list_header, null);
                        holder.textView = (TextView)convertView.findViewById(R.id.list_header_title);
                        holder.textView.setTypeface(tfb);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            
            holder.textView.setText(mData.get(position));
            return convertView;
        }
 
    }
 
    public static class ViewHolder {
        public TextView textView;
    }

}
