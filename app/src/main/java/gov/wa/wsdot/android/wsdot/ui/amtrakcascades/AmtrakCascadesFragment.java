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

package gov.wa.wsdot.android.wsdot.ui.amtrakcascades;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class AmtrakCascadesFragment extends BaseFragment {
    
    private static final String TAG = AmtrakCascadesFragment.class.getSimpleName();
    private ArrayList<ViewItem> listViewItems;
    private ItemAdapter mAdapter;
    private Tracker mTracker;

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

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list, null);

        // Build items for list
        listViewItems = new ArrayList<>();
        listViewItems.add(new ViewItem("Buy Tickets", "http://m.amtrak.com"));
        listViewItems.add(new ViewItem("Check Schedules and Status", AmtrakCascadesSchedulesActivity.class));

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ItemAdapter(listViewItems);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        return root;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Custom adapter for items in recycler view that need a cursor adapter.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see CursorRecyclerAdapter
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class ItemAdapter extends RecyclerView.Adapter<AmtrakViewHolder> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private List<ViewItem> itemList;

        public ItemAdapter(List<ViewItem> posts){
            this.itemList = posts;
            notifyDataSetChanged();
        }

        @Override
        public AmtrakViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.list_item, parent, false);
            return new AmtrakViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(AmtrakViewHolder holder, int position) {

            ViewItem item = itemList.get(position);
            holder.title.setText(item.getTitle());
            holder.title.setTypeface(tf);
            holder.clz = item.getClz();
            holder.url = item.getUrl();

            final int pos = position;
            final Object clz = item.getClz();
            final String url = item.getUrl();


            // Set onClickListener for holder's view
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            if (clz instanceof Class<?>) {
                                intent.setClass(getActivity(), (Class<?>) clz);
                            } else {
                                // GA tracker
                                mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
                                mTracker.setScreenName("/Amtrak Cascades/Buy Tickets");
                                mTracker.send(new HitBuilders.ScreenViewBuilder().build());

                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                            }
                            startActivity(intent);
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            if (itemList == null) {
                return 0;
            }else {
                return itemList.size();
            }
        }
    }

    public static class AmtrakViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected Object clz;
        protected String url;

        public AmtrakViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
        }

    }

    public class ViewItem {
        private String title;
        private Object clz;
        private String url;

        public ViewItem(String title, Class<?> clz) {
            this.title = title;
            this.clz = clz;
        }
        public ViewItem(String title, String url) {
            this.title = title;
            this.url = url;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public Object getClz() {
            return clz;
        }
        public void setClz(Object clz) {
            this.clz = clz;
        }
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
    }
}
