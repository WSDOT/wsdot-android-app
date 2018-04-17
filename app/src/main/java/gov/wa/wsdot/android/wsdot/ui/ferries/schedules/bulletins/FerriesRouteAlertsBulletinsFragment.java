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

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules.bulletins;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteAlertItem;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;

public class FerriesRouteAlertsBulletinsFragment extends BaseListFragment implements
		Injectable {

	private static final String TAG = FerriesRouteAlertsBulletinsFragment.class.getName();
	private static ArrayList<FerriesRouteAlertItem> routeAlertItems;
	private static RouteAlertItemAdapter adapter;

	private static View mLoadingSpinner;
    private View mEmptyView;

	private static Integer mId;

	private static FerriesBulletinsViewModel viewModel;

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Bundle args = getActivity().getIntent().getExtras();
        if (args != null) {
            mId = args.getInt("routeId", 0);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new RouteAlertItemAdapter(getActivity());
        setListAdapter(adapter);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        mEmptyView = root.findViewById( R.id.empty_list_view );

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FerriesBulletinsViewModel.class);
        viewModel.init(mId, null);

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

        viewModel.getAlerts().observe(this, alerts -> {
            if (alerts != null) {
                mEmptyView.setVisibility(View.GONE);
                adapter.setData(new ArrayList<>(alerts));
                routeAlertItems = new ArrayList<>(alerts);
            } else {
                adapter.setData(null);
                TextView t = (TextView) mEmptyView;
                t.setText(R.string.no_alerts);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        });

        disableAds(root);
        
        return root;
    }
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(getActivity(), FerriesRouteAlertsBulletinDetailsActivity.class);
        b.putInt("routeId", mId);
        b.putInt("alertId", routeAlertItems.get(position).getBulletinID());
        b.putString("AlertFullTitle", routeAlertItems.get(position).getAlertFullTitle());
		intent.putExtras(b);
		startActivity(intent);		
	}

	
	private class RouteAlertItemAdapter extends ArrayAdapter<FerriesRouteAlertItem> {
		private final LayoutInflater mInflater;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        //DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
        
        public RouteAlertItemAdapter(Context context) {
	        super(context, R.layout.simple_list_item);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(ArrayList<FerriesRouteAlertItem> data) {
            clear();
            if (data != null) {
                addAll(data); // Only in API level 11
                notifyDataSetChanged();                
            }        	
        }
        
        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        ViewHolder holder = null;
        	
        	if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.simple_list_item, null);
	            holder = new ViewHolder();
	            holder.title = convertView.findViewById(R.id.title);
	            holder.title.setTypeface(tfb);
	            holder.description = convertView.findViewById(R.id.description);
	            holder.description.setTypeface(tf);
	            
	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        FerriesRouteAlertItem item = getItem(position);
	        
        	holder.title.setText(item.getAlertFullTitle());
            
        	try {
        		Date date = new Date(Long.parseLong(item.getPublishDate()));
        		holder.description.setText(ParserUtils.relativeTime(date));
        	} catch (Exception e) {
        		Log.e(TAG, "Error parsing date", e);
        	}	            	

	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView title;
		public TextView description;
	}
	
}
