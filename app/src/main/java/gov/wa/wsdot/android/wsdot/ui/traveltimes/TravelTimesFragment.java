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

package gov.wa.wsdot.android.wsdot.ui.traveltimes;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.traveltimes.TravelTimeEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.TravelTimes;
import gov.wa.wsdot.android.wsdot.service.TravelTimesSyncService;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class TravelTimesFragment extends BaseFragment implements
        OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener,
		Injectable {

    private static final String TAG = TravelTimesFragment.class.getSimpleName();
	private static TravelTimesAdapter mAdapter;

	private View mEmptyView;

	private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	private static TravelTimesViewModel viewModel;

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TravelTimesAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

		mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
				R.color.holo_blue_bright,
				R.color.holo_green_light,
				R.color.holo_orange_light,
				R.color.holo_red_light);
        
        mEmptyView = root.findViewById(R.id.empty_list_view);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TravelTimesViewModel.class);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case SUCCESS:
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getQueryTravelTimes().observe(this, travelTimes -> {
            if (travelTimes != null) {
                if (travelTimes.size() == 0) {
                    TextView t = (TextView) mEmptyView;
                    if (!viewModel.getQueryTermValue().equals("")) {
                        t.setText(R.string.no_matching_travel_times);
                    } else {
                        t.setText("travel times unavailable.");
                    }
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                }
                mAdapter.setData(new ArrayList<>(travelTimes));
            }
        });

        viewModel.setQueryTerm("");

        return root;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
		
        //Create the search view
        SearchView searchView = new SearchView(
                ((BaseActivity) getActivity()).getSupportActionBar().getThemedContext());
        searchView.setQueryHint("Search Travel Times");

        searchView.setOnQueryTextListener(this);
		
        MenuItem menuItem_Search = menu.add(R.string.search_title).setIcon(R.drawable.ic_menu_search);
        MenuItemCompat.setActionView(menuItem_Search, searchView);
        MenuItemCompat.setShowAsAction(menuItem_Search,
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
                        | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        
        MenuItemCompat.setOnActionExpandListener(menuItem_Search, new MenuItemCompat.OnActionExpandListener() {
			public boolean onMenuItemActionCollapse(MenuItem item) {
				viewModel.setQueryTerm("");
				return true;
			}

			public boolean onMenuItemActionExpand(MenuItem item) {
				return true;
			}
		});
	}

	public boolean onQueryTextChange(String newText) {
        String query = !TextUtils.isEmpty(newText) ? newText : "";
        viewModel.setQueryTerm(query);
        return true;
	}

	public boolean onQueryTextSubmit(String query) {
		viewModel.setQueryTerm(query);
		return false;
	}

    /**
     * Custom adapter for items in recycler view.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class TravelTimesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;

        private ArrayList<TravelTimeEntity> mData = new ArrayList<>();

        private List<RecyclerView.ViewHolder> mItems = new ArrayList<>();

        public TravelTimesAdapter(Context context) {
            this.context = context;
        }

        public void setData(ArrayList<TravelTimeEntity> data){
            mData = data;
            this.notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_travel_times, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(viewholder);
            mItems.add(viewholder);
            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            ViewHolder holder = (ViewHolder) viewHolder;

            String average_time;

            TravelTimeEntity travelTime = mData.get(position);

            String title = travelTime.getTitle();
            holder.title.setText(title);
            holder.title.setTypeface(tfb);

            String distance = travelTime.getDistance();
            int average = travelTime.getAverage();

            if (average == 0) {
                average_time = "Not Available";
            } else {
                average_time = average + " min";
            }

            holder.distance_average_time.setText(distance + " / " + average_time);
            holder.distance_average_time.setTypeface(tf);

            int current = travelTime.getCurrent();

            if (current < average) {
                holder.current_time.setTextColor(0xFF008060);
            } else if ((current > average) && (average != 0)) {
                holder.current_time.setTextColor(Color.RED);
            } else {
                holder.current_time.setTextColor(Color.BLACK);
            }

            holder.current_time.setText(current + " min");
            holder.current_time.setTypeface(tfb);

            String created_at = travelTime.getUpdated();
            holder.updated.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", true));
            holder.updated.setTypeface(tf);

            holder.star_button.setTag(travelTime.getTravelTimeId());
            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            holder.star_button.setOnCheckedChangeListener(null);
            holder.star_button
                    .setChecked(travelTime.getIsStarred() != 0);
            holder.star_button.setOnCheckedChangeListener((buttonView, isChecked) -> {

                int timeId = (Integer) buttonView.getTag();

                Snackbar added_snackbar = Snackbar
                        .make(getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

                Snackbar removed_snackbar = Snackbar
                        .make(getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

                if (isChecked){
                    added_snackbar.show();
                }else{
                    removed_snackbar.show();
                }

                viewModel.setIsStarredFor(timeId, isChecked ? 1 : 0);
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public TextView current_time;
            public TextView distance_average_time;
            public TextView updated;
            public CheckBox star_button;

            public ViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.title);
                current_time = view.findViewById(R.id.current_time);
                distance_average_time = view.findViewById(R.id.distance_average_time);
                updated = view.findViewById(R.id.updated);
                star_button = view.findViewById(R.id.star_button);
            }
        }
    }

    public void onRefresh() {
		swipeRefreshLayout.setRefreshing(true);
		viewModel.forceRefreshTravelTimes();
    }

}
