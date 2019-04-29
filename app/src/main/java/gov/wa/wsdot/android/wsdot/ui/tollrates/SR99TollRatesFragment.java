package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.tollrows.TollRowEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.Converters;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class SR99TollRatesFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener, Injectable {

    private static final String TAG = SR520TollRatesFragment.class.getSimpleName();
    private Adapter mAdapter;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private View mEmptyView;
    private static SwipeRefreshLayout swipeRefreshLayout;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    TollRatesViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new Adapter();

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        swipeRefreshLayout = root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);

        mEmptyView = root.findViewById(R.id.empty_list_view);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TollRatesViewModel.class);

        viewModel.getResourceStatus().observe(getViewLifecycleOwner(), resourceStatus -> {
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

        viewModel.getTollRatesFor(99).observe(getViewLifecycleOwner(), tollRateTable -> {

            if (tollRateTable != null) {

                mAdapter.mData.clear();

                mEmptyView.setVisibility(View.GONE);

                HashMap<Integer, String> weekdayHeaderMap = null;
                HashMap<Integer, String> weekendHeaderMap = null;

                ArrayList<String[]> weekdays = new ArrayList<>();
                ArrayList<String[]> weekends = new ArrayList<>();

                for (TollRowEntity row: tollRateTable.rows) {

                    String[] rowValues = Converters.fromJsonString(row.getRowValues());

                    if (row.getHeader()) {
                        if (row.getWeekday()) {
                            weekdayHeaderMap = new HashMap<>();
                            for (int i = 0; i < rowValues.length; i++) {
                                weekdayHeaderMap.put(i, rowValues[i]);
                            }
                        } else {
                            weekendHeaderMap = new HashMap<>();
                            for (int i = 0; i < rowValues.length; i++) {
                                weekendHeaderMap.put(i, rowValues[i]);
                            }
                        }
                    } else {
                        if (row.getWeekday()) {
                            weekdays.add(rowValues);
                        } else {
                            weekends.add(rowValues);
                        }
                    }
                }

                String[][] weekdayData = new String[weekdays.size()][];

                for (int i = 0; i < weekdays.size(); i++){
                    weekdayData[i] = weekdays.get(i);
                }

                String[][] weekendData = new String[weekends.size()][];

                for (int i = 0; i < weekends.size(); i++){
                    weekendData[i] = weekends.get(i);
                }

                mAdapter.addSeparatorItem(weekdayHeaderMap);
                BuildAdapterData(weekdayData, tollRateTable.tollRateTableData.getNumCol());
                mAdapter.addSeparatorItem(weekendHeaderMap);
                BuildAdapterData(weekendData, tollRateTable.tollRateTableData.getNumCol());

            } else {
                Log.e(TAG, "its null");
            }
        });

        viewModel.refresh();

        return root;
    }

    private void BuildAdapterData(String[][] data, int numCol) {
        HashMap<Integer, String> map = null;

        for (int i = 0; i < data.length; i++) {

            map = new HashMap<>();
            for (int j = 0; j < numCol; j++) {
                map.put(j, data[i][j]);
            }

            mAdapter.addItem(map);
        }
    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see RecyclerView.Adapter
     */
    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;

        private TreeSet<Integer> mSeparatorsSet = new TreeSet<>();
        private ArrayList<HashMap<Integer, String>> mData = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = null;

            switch (viewType) {
                case TYPE_ITEM:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_three_col_row, parent, false);
                    return new ItemViewHolder(itemView);
                case TYPE_SEPARATOR:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_three_col_header, parent, false);
                    return new TitleViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, int position) {

            ItemViewHolder itemholder;
            TitleViewHolder titleholder;

            HashMap<Integer, String> map = mData.get(position);

            try {
                if (getItemViewType(position) == TYPE_ITEM) {
                    itemholder = (ItemViewHolder) viewholder;
                    itemholder.hours.setText(map.get(0));
                    itemholder.hours.setTypeface(tf);
                    itemholder.goodToGoPass.setText(map.get(1));
                    itemholder.goodToGoPass.setTypeface(tf);
                    itemholder.payByMail.setText(map.get(2));
                    itemholder.payByMail.setTypeface(tf);
                } else {
                    titleholder = (TitleViewHolder) viewholder;
                    titleholder.hours.setText(map.get(0));
                    titleholder.hours.setTypeface(tfb);
                    titleholder.goodToGoPass.setText(map.get(1));
                    titleholder.goodToGoPass.setTypeface(tfb);
                    titleholder.payByMail.setText(map.get(2));
                    titleholder.payByMail.setTypeface(tfb);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "map values null at:");
                Log.e(TAG, String.valueOf(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        }

        public void addItem(final HashMap<Integer, String> item) {
            mData.add(item);
            notifyDataSetChanged();
        }

        public void addSeparatorItem(final HashMap<Integer, String> item) {
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

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView hours;
        protected TextView goodToGoPass;
        protected TextView payByMail;

        public ItemViewHolder(View itemView) {
            super(itemView);
            hours = itemView.findViewById(R.id.hours);
            goodToGoPass = itemView.findViewById(R.id.goodtogo_pass);
            payByMail = itemView.findViewById(R.id.pay_by_mail);
        }
    }

    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        protected TextView hours;
        protected TextView goodToGoPass;
        protected TextView payByMail;

        public TitleViewHolder(View itemView) {
            super(itemView);
            hours = itemView.findViewById(R.id.hours_title);
            goodToGoPass = itemView.findViewById(R.id.goodtogo_pass_title);
            payByMail = itemView.findViewById(R.id.pay_by_mail_title);
        }
    }

    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        viewModel.refresh();
    }
}