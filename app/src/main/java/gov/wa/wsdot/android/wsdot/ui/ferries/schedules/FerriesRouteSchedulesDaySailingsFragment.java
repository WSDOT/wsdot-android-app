/*
 * Copyright (c) 2017 Washington State Department of Transportation
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FerriesRouteSchedulesDaySailingsFragment extends BaseFragment
        implements LoaderCallbacks<ArrayList<FerriesScheduleDateItem>> {
	
	private static final String TAG = FerriesRouteSchedulesDaySailingsFragment.class.getSimpleName();
	private static ArrayList<FerriesScheduleDateItem> scheduleDateItems;
	private static SailingsAdapter mAdapter;
	private static View mLoadingSpinner;
	private static String mDates;

	protected RecyclerView mRecyclerView;
	protected LinearLayoutManager mLayoutManager;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		if (context instanceof Activity) {
			Bundle args = ((Activity)context).getIntent().getExtras();
			mDates = args.getString("date");
		}
	}

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
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_with_spinner, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SailingsAdapter(null);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        
        return root;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		//setListAdapter(null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
        getLoaderManager().initLoader(0, null, this);
	}


	public Loader<ArrayList<FerriesScheduleDateItem>> onCreateLoader(int id,
			Bundle args) {
		
		// This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
		return new TerminalLoader(getActivity());
	}

	public void onLoadFinished(Loader<ArrayList<FerriesScheduleDateItem>> loader,
			ArrayList<FerriesScheduleDateItem> data) {
		
		mLoadingSpinner.setVisibility(View.GONE);
		mAdapter.setData(data.get(0).getFerriesTerminalItem());
	}

	public void onLoaderReset(Loader<ArrayList<FerriesScheduleDateItem>> loader) {
		mAdapter.setData(null);
	}
	
	public static class TerminalLoader extends AsyncTaskLoader<ArrayList<FerriesScheduleDateItem>> {

		public TerminalLoader(Context context) {
			super(context);
		}

		@Override
		public ArrayList<FerriesScheduleDateItem> loadInBackground() {
			scheduleDateItems = new ArrayList<FerriesScheduleDateItem>();
			FerriesScheduleDateItem scheduleDate = null;
			FerriesTerminalItem terminal = null;
			FerriesAnnotationsItem notes = null;
			FerriesScheduleTimesItem timesItem = null;
			FerriesAnnotationIndexesItem indexesItem = null;
			Date now = new Date();
			
	    	try {   		
				JSONArray dates = new JSONArray(mDates);
				int numDates = dates.length();
				for (int j=0; j < numDates; j++) {
					JSONObject date = dates.getJSONObject(j);
					scheduleDate = new FerriesScheduleDateItem();
					scheduleDate.setDate(date.getString("Date").substring(6, 19));
					
					JSONArray sailings = date.getJSONArray("Sailings");
					int numSailings = sailings.length();
					for (int k=0; k < numSailings; k++) {
						JSONObject sailing = sailings.getJSONObject(k);
						terminal = new FerriesTerminalItem();
						terminal.setArrivingTerminalID(sailing.getInt("ArrivingTerminalID"));
						terminal.setArrivingTerminalName(sailing.getString("ArrivingTerminalName"));
						terminal.setDepartingTerminalID(sailing.getInt("DepartingTerminalID"));
						terminal.setDepartingTerminalName(sailing.getString("DepartingTerminalName"));
						
						JSONArray annotations = sailing.getJSONArray("Annotations");
						int numAnnotations = annotations.length();
						for (int l=0; l < numAnnotations; l++) {
							notes = new FerriesAnnotationsItem();
							notes.setAnnotation(annotations.getString(l));
							terminal.setAnnotations(notes);	
						}
						
						JSONArray times = sailing.getJSONArray("Times");
						int numTimes = times.length();
						for (int m=0; m < numTimes; m++) {
							JSONObject time = times.getJSONObject(m);
							
							// Don't display past sailing times. Doesn't make sense.
                            if (now.after(new Date(Long.parseLong(time
                                    .getString("DepartingTime")
                                    .substring(6, 19))))) {
                                continue;
                            }
														
							timesItem = new FerriesScheduleTimesItem();
							timesItem.setDepartingTime(time.getString("DepartingTime").substring(6, 19));
							
							try {
								timesItem.setArrivingTime(time.getString("ArrivingTime").substring(6, 19));	
							} catch (StringIndexOutOfBoundsException e) {
								timesItem.setArrivingTime("N/A");
							}
							
							JSONArray annotationIndexes = time.getJSONArray("AnnotationIndexes");
							int numIndexes = annotationIndexes.length();
							for (int n=0; n < numIndexes; n++) {
								indexesItem = new FerriesAnnotationIndexesItem();
								indexesItem.setIndex(annotationIndexes.getInt(n));
								timesItem.setAnnotationIndexes(indexesItem);									
							}
							terminal.setScheduleTimes(timesItem);
						}
						scheduleDate.setFerriesTerminalItem(terminal);
					}
					
					scheduleDateItems.add(scheduleDate);
				}
			} catch (Exception e) {
				Log.e(TAG, "Error adding schedule date items", e);
			}
		
	    	return scheduleDateItems;		
		}

		@Override
		public void deliverResult(ArrayList<FerriesScheduleDateItem> data) {
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
		public void onCanceled(ArrayList<FerriesScheduleDateItem> data) {
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
    private class SailingsAdapter extends RecyclerView.Adapter<FerrydepartureVH> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private List<FerriesTerminalItem> items;

        public SailingsAdapter(List<FerriesTerminalItem> items){
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public FerrydepartureVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.list_item, parent, false);
            return new FerrydepartureVH(itemView);
        }

        @Override
        public void onBindViewHolder(FerrydepartureVH holder, int position) {

            FerriesTerminalItem item = items.get(position);

            holder.title.setText(item.getDepartingTerminalName() + " to " + item.getArrivingTerminalName());
            holder.title.setTypeface(tf);

            final int pos = position;

            // Set onClickListener for holder's view
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            String terminalNames = scheduleDateItems.get(0)
                                    .getFerriesTerminalItem().get(pos)
                                    .getDepartingTerminalName()
                                    + " to "
                                    + scheduleDateItems.get(0).getFerriesTerminalItem()
                                    .get(pos).getArrivingTerminalName();

                            int terminalId = scheduleDateItems.get(0).getFerriesTerminalItem()
                                    .get(pos).getDepartingTerminalID();

                            Bundle b = new Bundle();
                            Intent intent = new Intent(getActivity(), FerriesRouteSchedulesDayDeparturesActivity.class);
                            b.putInt("terminalId", terminalId);
                            b.putString("terminalNames", terminalNames);
                            b.putInt("position", pos);
                            b.putSerializable("scheduleDateItems", scheduleDateItems);
                            intent.putExtras(b);
                            startActivity(intent);
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            if (items == null) {
                return 0;
            }else {
                return items.size();
            }
        }

        public void clear(){
            if (items != null) {
                this.items.clear();
                notifyDataSetChanged();
            }
        }

        public void setData(List<FerriesTerminalItem> items){
            this.items = items;
            notifyDataSetChanged();
        }

    }

    // View Holder for ferry departure list items.
    private class FerrydepartureVH extends RecyclerView.ViewHolder{
        TextView title;

        public FerrydepartureVH(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
        }
    }

}
