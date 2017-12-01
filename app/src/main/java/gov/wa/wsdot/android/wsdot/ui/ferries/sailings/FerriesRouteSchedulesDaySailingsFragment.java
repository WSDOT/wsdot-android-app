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

package gov.wa.wsdot.android.wsdot.ui.ferries.sailings;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.FerrySchedulesViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerriesRouteSchedulesDayDeparturesActivity;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FerriesRouteSchedulesDaySailingsFragment extends BaseFragment implements Injectable {
	
	private static final String TAG = FerriesRouteSchedulesDaySailingsFragment.class.getSimpleName();
	private static ArrayList<FerriesScheduleDateItem> scheduleDateItems;
	private static SailingsAdapter mAdapter;
	private static View mLoadingSpinner;

	private static Integer mId;
	private static String mDates;

	protected RecyclerView mRecyclerView;
	protected LinearLayoutManager mLayoutManager;

	private static FerrySchedulesViewModel viewModel;

	@Inject
	ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getActivity().getIntent().getExtras();
        mId = args.getInt("id");
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_with_spinner, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FerrySchedulesViewModel.class);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mLoadingSpinner.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        mLoadingSpinner.setVisibility(View.GONE);
                        break;
                    case ERROR:
                        mLoadingSpinner.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getFerryScheduleFor(mId).observe(this, schedule -> {
            if (schedule != null) {
                mDates = schedule.getDate();
                viewModel.loadDatesWithSailingsFromJson(mDates);
            }
        });

        viewModel.getDatesWithSailings().observe(this, dates -> {
            scheduleDateItems = new ArrayList<>(dates);
            mAdapter.setData(dates.get(0).getFerriesTerminalItem());
        });

        return root;
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
                    v -> {
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
                        b.putInt("scheduleId", mId);
                        b.putInt("terminalId", terminalId);
                        b.putString("terminalNames", terminalNames);
                        b.putInt("terminalIndex", pos);
                        b.putSerializable("scheduleDateItems", scheduleDateItems);
                        intent.putExtras(b);
                        startActivity(intent);
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
