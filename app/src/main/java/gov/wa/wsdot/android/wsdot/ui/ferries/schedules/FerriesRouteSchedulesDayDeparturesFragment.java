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

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesTerminalSailingSpace;
import gov.wa.wsdot.android.wsdot.service.FerriesTerminalSailingSpaceSyncService;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class FerriesRouteSchedulesDayDeparturesFragment extends BaseListFragment
        implements LoaderCallbacks<ArrayList<FerriesScheduleTimesItem>>,
        AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FerriesRouteSchedulesDayDeparturesFragment.class.getSimpleName();
	private static FerriesTerminalItem terminalItem;
	private static ArrayList<FerriesAnnotationsItem> annotations;
	private static ArrayList<FerriesScheduleTimesItem> times;
	private static DepartureTimesAdapter adapter;
	private View mHeaderView;
	private Typeface tf;
	private Typeface tfb;
	private static SwipeRefreshLayout swipeRefreshLayout;
	private FerriesTerminalSyncReceiver ferriesTerminalSyncReceiver;
    private View mEmptyView;
	private static LoaderCallbacks<Cursor> ferriesTerminalSyncCallbacks;
	private Spinner daySpinner;
    private static ArrayList<FerriesScheduleDateItem> mScheduleDateItems;
    private static ArrayList<String> mDaysOfWeek;
	private static int mPosition;
	
	private static final int FERRIES_DEPARTURES_LOADER_ID = 0;
	private static final int FERRIES_VEHICLE_SPACE_LOADER_ID = 1;	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
        DateFormat dateFormat = new SimpleDateFormat("EEEE");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		
		Bundle args = activity.getIntent().getExtras();
		
        mPosition = args.getInt("position");
        mScheduleDateItems = (ArrayList<FerriesScheduleDateItem>) args.getSerializable("scheduleDateItems");
        terminalItem = mScheduleDateItems.get(0).getFerriesTerminalItem().get(mPosition);
        mDaysOfWeek = new ArrayList<String>();
        
        int numDates = mScheduleDateItems.size();
        for (int i = 0; i < numDates; i++) {
            mDaysOfWeek.add(dateFormat.format(new Date(
                    Long.parseLong(mScheduleDateItems.get(i).getDate()))));
        }

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
		setRetainInstance(true);
		
		ferriesTerminalSyncCallbacks = new LoaderCallbacks<Cursor>() {

            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String[] projection = {
                        FerriesTerminalSailingSpace._ID,
                        FerriesTerminalSailingSpace.TERMINAL_ID,
                        FerriesTerminalSailingSpace.TERMINAL_NAME,
                        FerriesTerminalSailingSpace.TERMINAL_ABBREV,
                        FerriesTerminalSailingSpace.TERMINAL_DEPARTING_SPACES,
                        FerriesTerminalSailingSpace.TERMINAL_LAST_UPDATED,
                        FerriesTerminalSailingSpace.TERMINAL_IS_STARRED
                };
                
                CursorLoader cursorLoader = new FerriesTerminalLoader(getActivity(),
                        FerriesTerminalSailingSpace.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null
                        );
                
                return cursorLoader;
            }

            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                
                if (cursor != null && cursor.moveToFirst()) {
                    // Update existing FerriesScheduleTimesItem (times)
                    do {
                        int departingTerminalID = cursor.getInt(cursor.getColumnIndex(FerriesTerminalSailingSpace.TERMINAL_ID));
                        if (departingTerminalID != terminalItem.getDepartingTerminalID()) {
                            continue;
                        }
                        try {
                            JSONArray departingSpaces = new JSONArray(cursor.getString(cursor.getColumnIndex(FerriesTerminalSailingSpace.TERMINAL_DEPARTING_SPACES)));
                            for (int i=0; i < departingSpaces.length(); i++) {
                                JSONObject spaces = departingSpaces.getJSONObject(i);
                                String departure = dateFormat.format(new Date(Long.parseLong(spaces.getString("Departure").substring(6, 19))));
                                JSONArray spaceForArrivalTerminals = spaces.getJSONArray("SpaceForArrivalTerminals");
                                for (int j=0; j < spaceForArrivalTerminals.length(); j++) {
                                    JSONObject terminals = spaceForArrivalTerminals.getJSONObject(j);
                                    if (terminals.getInt("TerminalID") != terminalItem.getArrivingTerminalID()) {
                                        continue;
                                    } else {
                                        int driveUpSpaceCount = terminals.getInt("DriveUpSpaceCount");
                                        int maxSpaceCount = terminals.getInt("MaxSpaceCount");
                                        for (FerriesScheduleTimesItem time: times) {
                                            if (dateFormat.format(new Date(Long.parseLong(time.getDepartingTime()))).equals(departure)) {
                                                time.setDriveUpSpaceCount(driveUpSpaceCount);
                                                time.setMaxSpaceCount(maxSpaceCount);
                                                time.setLastUpdated(cursor.getString(cursor.getColumnIndex(FerriesTerminalSailingSpace.TERMINAL_LAST_UPDATED)));
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } while (cursor.moveToNext());
                }

                swipeRefreshLayout.setRefreshing(false);
                adapter.clear();
                adapter.setData(times);
            }

            public void onLoaderReset(Loader<Cursor> loader) {
                swipeRefreshLayout.setRefreshing(false);                 
            }
		};
	}

    public static class FerriesTerminalLoader extends CursorLoader {

        public FerriesTerminalLoader(Context context, Uri uri,
                String[] projection, String selection, String[] selectionArgs,
                String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
        }
        
    }
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner_swipe_refresh, null);

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
        
        mHeaderView = inflater.inflate(R.layout.list_item_departure_times_header, null);
        TextView departing_title = (TextView) mHeaderView.findViewById(R.id.departing_title);
        departing_title.setTypeface(tfb);
        TextView arriving_title = (TextView) mHeaderView.findViewById(R.id.arriving_title);
        arriving_title.setTypeface(tfb);
        
        mEmptyView = root.findViewById(R.id.empty_list_view);
        
        ArrayAdapter<String> dayOfWeekArrayAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, mDaysOfWeek);
        
        dayOfWeekArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner = (Spinner) root.findViewById(R.id.day_spinner);
        daySpinner.setAdapter(dayOfWeekArrayAdapter);
        daySpinner.setOnItemSelectedListener(this);
        
        enableAds(root);
        
        return root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		setListAdapter(null);
	}
	

    @Override
    public void onPause() {
        super.onPause();
        
        getActivity().unregisterReceiver(ferriesTerminalSyncReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        IntentFilter filter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.FERRIES_TERMINAL_SAILING_SPACE_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        ferriesTerminalSyncReceiver = new FerriesTerminalSyncReceiver();
        getActivity().registerReceiver(ferriesTerminalSyncReceiver, filter);
    }	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		
		if (adapter == null) {
			adapter = new DepartureTimesAdapter(getActivity());
		}
		this.getListView().addHeaderView(mHeaderView);
		setListAdapter(adapter);
        
		// Prepare the loaders. Either re-connect with an existing one, or start new ones.
        getLoaderManager().initLoader(FERRIES_DEPARTURES_LOADER_ID, null, this);
        getLoaderManager().initLoader(FERRIES_VEHICLE_SPACE_LOADER_ID, null, ferriesTerminalSyncCallbacks);
        
        TextView t = (TextView) mEmptyView;
        t.setText(R.string.no_day_departures);
        getListView().setEmptyView(mEmptyView);
	}

	public Loader<ArrayList<FerriesScheduleTimesItem>> onCreateLoader(int id,
			Bundle args) {
		
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new DepartureTimesLoader(getActivity());
	}

	public void onLoadFinished(
			Loader<ArrayList<FerriesScheduleTimesItem>> loader,
			ArrayList<FerriesScheduleTimesItem> data) {
		
        Intent intent = new Intent(getActivity(), FerriesTerminalSailingSpaceSyncService.class);
        getActivity().startService(intent);
	    
	    swipeRefreshLayout.setRefreshing(false);
		adapter.setData(data);
	}

	public void onLoaderReset(Loader<ArrayList<FerriesScheduleTimesItem>> loader) {
	    swipeRefreshLayout.setRefreshing(false);
	    adapter.setData(null);
	}

	public static class DepartureTimesLoader extends AsyncTaskLoader<ArrayList<FerriesScheduleTimesItem>> {

		public DepartureTimesLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<FerriesScheduleTimesItem> loadInBackground() {
			int numAnnotations = terminalItem.getAnnotations().size();
	    	int numTimes = terminalItem.getScheduleTimes().size();
	    	annotations = new ArrayList<FerriesAnnotationsItem>();
	    	times = new ArrayList<FerriesScheduleTimesItem>();
			
	    	try {
	    		for (int i=0; i < numAnnotations; i++) {
	    			FerriesAnnotationsItem annotationItem = new FerriesAnnotationsItem();
	    			annotationItem.setAnnotation(terminalItem.getAnnotations().get(i).getAnnotation());
	    			annotations.add(annotationItem);
	    		}
	    		
				for (int i=0; i < numTimes; i++) {
					FerriesScheduleTimesItem timesItem = new FerriesScheduleTimesItem();
					timesItem.setDepartingTime(terminalItem.getScheduleTimes().get(i).getDepartingTime());
					timesItem.setArrivingTime(terminalItem.getScheduleTimes().get(i).getArrivingTime());
					
					int numIndexes = terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().size();
					for (int j=0; j < numIndexes; j++) {
						FerriesAnnotationIndexesItem index = new FerriesAnnotationIndexesItem();
						index.setIndex(terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().get(j).getIndex());
						timesItem.setAnnotationIndexes(index);
					}
					
					times.add(timesItem);
				}
			} catch (Exception e) {
				Log.e(TAG, "Error adding terminal departure times", e);
			}
	    	
			return times;
		}

		@Override
		public void deliverResult(ArrayList<FerriesScheduleTimesItem> data) {
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
			
			adapter.clear();
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
		public void onCanceled(ArrayList<FerriesScheduleTimesItem> data) {
			super.onCanceled(data);
		}

		@Override
		protected void onReset() {
			super.onReset();
			
			// Ensure the loader is stopped
			onStopLoading();
		}
		
	}
	
	private class DepartureTimesAdapter extends ArrayAdapter<FerriesScheduleTimesItem> {
		private final LayoutInflater mInflater;

        public DepartureTimesAdapter(Context context) {
	        super(context, R.layout.list_item_departure_times);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @SuppressWarnings("unused")
		public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }        
        
        public void setData(ArrayList<FerriesScheduleTimesItem> data) {
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
	        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
	        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
	        
	        ViewHolder holder;
	        
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.list_item_departure_times, null);
	            holder = new ViewHolder();
	            holder.departing = (TextView) convertView.findViewById(R.id.departing);
	            holder.departing.setTypeface(tfb);
	            holder.arriving = (TextView) convertView.findViewById(R.id.arriving);
	            holder.arriving.setTypeface(tfb);
	            holder.annotation = (TextView) convertView.findViewById(R.id.annotation);
	            holder.annotation.setTypeface(tf);
                holder.vehicleSpaceGroup = (RelativeLayout) convertView.findViewById(R.id.driveUpProgressBarGroup);
                holder.driveUpProgressBar = (ProgressBar) convertView.findViewById(R.id.driveUpProgressBar);
                holder.driveUpSpaceCount = (TextView) convertView.findViewById(R.id.driveUpSpaceCount);
	            holder.driveUpSpaceCount.setTypeface(tf);
                holder.driveUpSpaces = (TextView) convertView.findViewById(R.id.driveUpSpaces);
                holder.driveUpSpaces.setTypeface(tf);           
                holder.driveUpSpacesDisclaimer = (TextView) convertView.findViewById(R.id.driveUpSpacesDisclaimer);
                holder.driveUpSpacesDisclaimer.setTypeface(tf);
                holder.updated = (TextView) convertView.findViewById(R.id.updated);
                holder.updated.setTypeface(tf);

	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        FerriesScheduleTimesItem item = getItem(position);
	        String annotation = "";

	        int numIndexes = item.getAnnotationIndexes().size();
	        for (int i=0; i < numIndexes; i++) {
	        	FerriesAnnotationsItem p = annotations.get(item.getAnnotationIndexes().get(i).getIndex());
	        	annotation += p.getAnnotation();
	        }
	        
	        if (annotation.equals("")) {
	            holder.annotation.setVisibility(View.GONE);
	        } else {
	            holder.annotation.setVisibility(View.VISIBLE);
	        }
	        
        	holder.departing.setText(dateFormat.format(new Date(Long.parseLong(item.getDepartingTime()))));
        	
        	if (!item.getArrivingTime().equals("N/A")) {
        		holder.arriving.setText(dateFormat.format(new Date(Long.parseLong(item.getArrivingTime()))));
        	}

       		holder.annotation.setText(android.text.Html.fromHtml(annotation));

       		if (item.getDriveUpSpaceCount() != -1) {
                holder.vehicleSpaceGroup.setVisibility(View.VISIBLE);
                holder.driveUpProgressBar.setMax(item.getMaxSpaceCount());
                holder.driveUpProgressBar.setProgress(item.getMaxSpaceCount() - item.getDriveUpSpaceCount());
                holder.driveUpProgressBar.setSecondaryProgress(item.getMaxSpaceCount());
                holder.driveUpSpaceCount.setVisibility(View.VISIBLE);
                holder.driveUpSpaceCount.setText(Integer.toString(item.getDriveUpSpaceCount()));
                holder.driveUpSpaces.setVisibility(View.VISIBLE);
                holder.driveUpSpacesDisclaimer.setVisibility(View.VISIBLE);
       		    holder.updated.setVisibility(View.VISIBLE);
                holder.updated.setText(ParserUtils.relativeTime(item.getLastUpdated(), "MMMM d, yyyy h:mm a", false));
       		} else {
                holder.vehicleSpaceGroup.setVisibility(View.GONE);
                holder.driveUpSpaceCount.setVisibility(View.GONE);
                holder.driveUpSpaces.setVisibility(View.GONE);
                holder.driveUpSpacesDisclaimer.setVisibility(View.GONE);
       		    holder.updated.setVisibility(View.GONE);
        		}
	        
	        return convertView;
        }
        
    	private class ViewHolder {
    		TextView departing;
    		TextView arriving;
    		TextView annotation;
            RelativeLayout vehicleSpaceGroup;
            ProgressBar driveUpProgressBar;
    		TextView driveUpSpaceCount;
    		TextView driveUpSpaces;
    		TextView driveUpSpacesDisclaimer;
    		TextView updated;
    	}
	}

    public void onRefresh() {
		swipeRefreshLayout.post(new Runnable() {
			public void run() {
				swipeRefreshLayout.setRefreshing(true);
			}
		});
        Intent intent = new Intent(getActivity(), FerriesTerminalSailingSpaceSyncService.class);
        getActivity().startService(intent); 
    }
    
    public class FerriesTerminalSyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            if (responseString != null) {
                if (responseString.equals("OK")) {
                    getLoaderManager().restartLoader(
                            FERRIES_VEHICLE_SPACE_LOADER_ID, null,
                            ferriesTerminalSyncCallbacks);
                } else {
                    Log.e(TAG, responseString);
                    swipeRefreshLayout.setRefreshing(false);
                }
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        terminalItem = mScheduleDateItems.get(parent.getSelectedItemPosition()).getFerriesTerminalItem().get(mPosition);
        getLoaderManager().restartLoader(FERRIES_DEPARTURES_LOADER_ID, null, this);
        getLoaderManager().restartLoader(FERRIES_VEHICLE_SPACE_LOADER_ID, null, ferriesTerminalSyncCallbacks);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

}
