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
package gov.wa.wsdot.android.wsdot.ui.mountainpasses;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem.MountainPassItemActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class MountainPassesFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener, Injectable {

    private static final String TAG = MountainPassesFragment.class.getSimpleName();
    private View mEmptyView;
    private static SwipeRefreshLayout swipeRefreshLayout;

    private static MountainPassAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private static MountainPassViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MountainPassAdapter(getActivity());
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

        mEmptyView = root.findViewById( R.id.empty_list_view );
        mEmptyView.setVisibility(View.GONE);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MountainPassViewModel.class);

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

        viewModel.getPasses().observe(this, passes -> {
            mAdapter.setData(passes);
        });

        return root;
    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see RecyclerView.Adapter
     */
    private class MountainPassAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;
        private List<MountainPassEntity> mData = new ArrayList<>();

        public MountainPassAdapter(Context context) {
            this.context = context;
        }

        public void setData(List<MountainPassEntity> data){
            this.mData = data;
            this.notifyDataSetChanged();
        }

        @Override
        public MtPassVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_details_with_icon, null);
            MtPassVH viewholder = new MtPassVH(view);
            view.setTag(viewholder);
            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            MtPassVH mtpassVH = (MtPassVH) viewHolder;

            MountainPassEntity pass = mData.get(position);

            String title = pass.getName();
            mtpassVH.title.setText(title);
            mtpassVH.title.setTypeface(tfb);

            mtpassVH.setPos(position);

            String created_at = pass.getDateUpdated();

            mtpassVH.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
            mtpassVH.created_at.setTypeface(tf);

            String text = pass.getWeatherCondition();

            if (text.equals("")) {
                mtpassVH.text.setVisibility(View.GONE);
            } else {
                mtpassVH.text.setVisibility(View.VISIBLE);
                mtpassVH.text.setText(text);
                mtpassVH.text.setTypeface(tf);
            }

            int icon = getResources().getIdentifier(pass.getWeatherIcon(), "drawable", getActivity().getPackageName());

            mtpassVH.icon.setImageResource(icon);

            mtpassVH.star_button.setTag(pass.getPassId());

            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            mtpassVH.star_button.setOnCheckedChangeListener(null);
            mtpassVH.star_button.setContentDescription("favorite");
            mtpassVH.star_button
                    .setChecked(pass.getIsStarred() != 0);
            mtpassVH.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
                    int passId = (Integer) buttonView.getTag();

                    Snackbar added_snackbar = Snackbar
                            .make(getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

                    Snackbar removed_snackbar = Snackbar
                            .make(getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

                    added_snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("added to favorites");
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });

                    removed_snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("removed from favorites");
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });

                    if (isChecked){
                        added_snackbar.show();
                    }else{
                        removed_snackbar.show();
                    }

                    viewModel.setIsStarredFor(passId, isChecked ? 1 : 0);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        // View Holder for Mt pass list items.
        private class MtPassVH extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView icon;
            TextView title;
            TextView created_at;
            TextView text;
            CheckBox star_button;
            int itemId;

            public MtPassVH(View view) {
                super(view);
                title = view.findViewById(R.id.title);
                created_at = view.findViewById(R.id.created_at);
                text = view.findViewById(R.id.text);
                icon = view.findViewById(R.id.icon);
                star_button = view.findViewById(R.id.star_button);
                view.setOnClickListener(this);
            }

            public void setPos(int position){
                this.itemId = position;
            }

            public void onClick(View v) {
                MyLogger.crashlyticsLog("Mountain Passes", "Tap", "MountainPassesFragment " + mData.get(this.itemId).getPassId(), 1);
                Bundle b = new Bundle();
                Intent intent = new Intent(getActivity(), MountainPassItemActivity.class);
                b.putInt("id", mData.get(this.itemId).getPassId());
                intent.putExtras(b);
                startActivity(intent);
            }
        }
    }

    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        viewModel.forceRefreshPasses();
    }
}