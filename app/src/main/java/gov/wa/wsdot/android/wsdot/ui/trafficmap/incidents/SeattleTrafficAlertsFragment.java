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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.incidents;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.SeattleIncidentItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class SeattleTrafficAlertsFragment extends BaseFragment implements
        LoaderCallbacks<ArrayList<SeattleIncidentItem>>,
        SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = SeattleTrafficAlertsFragment.class.getSimpleName();
	private static ArrayList<SeattleIncidentItem> seattleIncidentItems = null;
    private static Adapter mAdapter;
    private static List<Integer> blockingCategory = new ArrayList<Integer>();
    private static List<Integer> constructionCategory = new ArrayList<Integer>();
    private static List<Integer> specialCategory = new ArrayList<Integer>();
	private static View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

    private Typeface tf;
    private Typeface tfb;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
	}    
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {


        tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new Adapter();

        mRecyclerView.setAdapter(mAdapter);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
				R.color.holo_blue_bright,
				R.color.holo_green_light,
				R.color.holo_orange_light,
				R.color.holo_red_light);
        
        mEmptyView = root.findViewById(R.id.empty_list_view);

        
        return root;
    }
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
	
	public Loader<ArrayList<SeattleIncidentItem>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple.
		return new SeattleIncidentItemsLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<SeattleIncidentItem>> loader, ArrayList<SeattleIncidentItem> data) {

        mEmptyView.setVisibility(View.GONE);

		if (!data.isEmpty()) {
			mAdapter.setData(data);
		} else {
		    TextView t = (TextView) mEmptyView;
			t.setText(R.string.no_connection);
            mEmptyView.setVisibility(View.VISIBLE);
		}
		
		swipeRefreshLayout.setRefreshing(false);
	}

	public void onLoaderReset(Loader<ArrayList<SeattleIncidentItem>> loader) {
		mAdapter.setData(null);		
	}
	
	/**
	 * A custom Loader that loads all of the Seattle incident alerts from the data server.
	 */	
    public static class SeattleIncidentItemsLoader extends
            AsyncTaskLoader<ArrayList<SeattleIncidentItem>> {

		public SeattleIncidentItemsLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<SeattleIncidentItem> loadInBackground() {
			seattleIncidentItems = new ArrayList<SeattleIncidentItem>();
			SeattleIncidentItem i = null;
			DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	        
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
				
				int numItems = items.length();
				for (int j=0; j < numItems; j++) {
					JSONObject item = items.getJSONObject(j);
					i = new SeattleIncidentItem();
					i.setTitle(item.getString("title"));
					i.setDescription(item.getString("description"));
					i.setCategory(item.getInt("category"));
					i.setGuid(item.getInt("guid"));
					i.setLastUpdatedTime(dateFormat.format(new Date(Long.parseLong(item
                            .getString("updated").substring(6, 19)))));
					
					seattleIncidentItems.add(i);
				}
				
			} catch (Exception e) {
				Log.e(TAG, "Error in network call", e);
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
			
			swipeRefreshLayout.post(new Runnable() {
				public void run() {
					swipeRefreshLayout.setRefreshing(true);
				}
			});
			mAdapter.mData.clear();
			mAdapter.notifyDataSetChanged();

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

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
        private ArrayList<SeattleIncidentItem> mData = new ArrayList<SeattleIncidentItem>();
        private LayoutInflater mInflater;
        private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
        private Stack<SeattleIncidentItem> blocking = new Stack<SeattleIncidentItem>();
        private Stack<SeattleIncidentItem> construction = new Stack<SeattleIncidentItem>();
        private Stack<SeattleIncidentItem> special = new Stack<SeattleIncidentItem>();
        private Stack<SeattleIncidentItem> closed = new Stack<SeattleIncidentItem>();
        private Stack<SeattleIncidentItem> amberalert = new Stack<SeattleIncidentItem>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = null;

            switch (viewType) {
                case TYPE_ITEM:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.seattle_incident_item, parent, false);
                    return new ItemViewHolder(itemView);
                case TYPE_SEPARATOR:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.list_header, parent, false);
                    return new TitleViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, int position) {

            ItemViewHolder itemholder;
            TitleViewHolder titleholder;

            if (getItemViewType(position) == TYPE_ITEM){
                itemholder = (ItemViewHolder) viewholder;
                itemholder.textView.setText(mData.get(position).getDescription());
                itemholder.updated.setText(ParserUtils.relativeTime(
                        mData.get(position).getLastUpdatedTime(),
                        "MMMM d, yyyy h:mm a", false));
            }else{
                titleholder = (TitleViewHolder) viewholder;
                titleholder.textView.setText(mData.get(position).getDescription());
                if (position == 0){
                    titleholder.divider.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        }

        public void setData(ArrayList<SeattleIncidentItem> data) {
            mData.clear();
            if (data != null) {
                int size = data.size();
                for (int i=0; i < size; i++) {
                    // Check if Traffic Management Center is closed
                    if (data.get(i).getCategory().equals(27)) {
                        //closed.push(data.get(i).getDescription());
                        closed.push(data.get(i));
                        break; // TSMC is closed so stop here
                    }
                    // Check if there is an active amber alert
                    else if (data.get(i).getCategory().equals(24)) {
                        //amberalert.push(data.get(i).getDescription());
                        amberalert.push(data.get(i));
                    }
                    else if (blockingCategory.contains(data.get(i).getCategory())) {
                        //blocking.push(data.get(i).getDescription());
                        blocking.push(data.get(i));
                    }
                    else if (constructionCategory.contains(data.get(i).getCategory())) {
                        //construction.push(data.get(i).getDescription());
                        construction.push(data.get(i));
                    }
                    else if (specialCategory.contains(data.get(i).getCategory())) {
                        //special.push(data.get(i).getDescription());
                        special.push(data.get(i));
                    }
                }

                if (amberalert != null && amberalert.size() != 0) {
                    mAdapter.addSeparatorItem(new SeattleIncidentItem("Amber Alerts"));
                    while (!amberalert.empty()) {
                        mAdapter.addItem(amberalert.pop());
                    }
                }
                if (closed != null && closed.size() == 0) {
                    mAdapter.addSeparatorItem(new SeattleIncidentItem("Blocking Incidents"));
                    if (blocking.empty()) {
                        mAdapter.addItem(new SeattleIncidentItem("None reported"));
                    } else {
                        while (!blocking.empty()) {
                            mAdapter.addItem(blocking.pop());
                        }
                    }
                    mAdapter.addSeparatorItem(new SeattleIncidentItem("Construction Closures"));
                    if (construction.empty()) {
                        mAdapter.addItem(new SeattleIncidentItem("None reported"));
                    } else {
                        while (!construction.empty()) {
                            mAdapter.addItem(construction.pop());
                        }
                    }
                    mAdapter.addSeparatorItem(new SeattleIncidentItem("Special Events"));
                    if (special.empty()) {
                        mAdapter.addItem(new SeattleIncidentItem("None reported"));
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


        public void addItem(final SeattleIncidentItem item) {
            mData.add(item);
            notifyDataSetChanged();
        }

        public void addSeparatorItem(final SeattleIncidentItem item) {
            mData.add(item);
            // save separator position
            mSeparatorsSet.add(mData.size() - 1);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView textView;
        protected TextView updated;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.description);
            updated = (TextView) itemView.findViewById(R.id.last_updated);
            textView.setTypeface(tf);
            updated.setTypeface(tf);
        }
    }
    public class TitleViewHolder extends RecyclerView.ViewHolder {
        protected TextView textView;
        protected LinearLayout divider;

        public TitleViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.list_header_title);
            textView.setTypeface(tfb);
            divider = (LinearLayout) itemView.findViewById(R.id.divider);
        }
    }

    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);        
    }

}
