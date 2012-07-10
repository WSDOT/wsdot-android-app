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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class SR16TollRatesFragment extends SherlockListFragment {
	private MyCustomAdapter adapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Toll Rates/SR 16");        
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

        return root;
    }
	
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
        HashMap<String, String> map = null;
        String[][] vehicleTypeData = {
           		{"Two (includes motorcycle)", "$4.00", "$5.00", "$6.00"},
        		{"Three", "$6.00", "$7.50", "$9.00"},
        		{"Four", "$8.00", "$10.00", "$12.00"},
        		{"Five", "$10.00", "$12.50", "$15.00"},
        		{"Six or more", "$12.00", "$15.00", "$18.00"}
        		};
        
        adapter = new MyCustomAdapter();
        setListAdapter(adapter);
        
        map = new HashMap<String, String>();
        map.put("number_axles", "Number of Axles");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_cash", "Cash");
        map.put("pay_by_mail", "Pay By Mail");
        adapter.addSeparatorItem(map);
        
        for (int i = 0; i < vehicleTypeData.length; i++) {
        	map = new HashMap<String, String>();
            map.put("number_axles", vehicleTypeData[i][0]);
        	map.put("goodtogo_pass", vehicleTypeData[i][1]);
            map.put("pay_by_cash", vehicleTypeData[i][2]);
            map.put("pay_by_mail", vehicleTypeData[i][3]);
            adapter.addItem(map);
        }		
	}

	private class MyCustomAdapter extends BaseAdapter {
        
    	private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

        ArrayList<HashMap<String, String>> mData = new ArrayList<HashMap<String, String>>();
        private LayoutInflater mInflater;
        private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
 
        public MyCustomAdapter() {
            mInflater = getActivity().getLayoutInflater();
        }
        
        public void addItem(final HashMap<String, String> map) {
            mData.add(map);
            notifyDataSetChanged();
        }
        
        public void addSeparatorItem(final HashMap<String, String> item) {
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
        
        public HashMap<String, String> getItem(int position) {
            return mData.get(position);
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int type = getItemViewType(position);
            
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(R.layout.tollrates_sr16_row, null);
                        holder.numberAxles = (TextView)convertView.findViewById(R.id.number_axles);
                        holder.goodToGoPass = (TextView)convertView.findViewById(R.id.goodtogo_pass);
                        holder.payByCash = (TextView)convertView.findViewById(R.id.pay_by_cash);
                        holder.payByMail = (TextView)convertView.findViewById(R.id.pay_by_mail);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.tollrates_sr16_header, null);
                        holder.numberAxles = (TextView)convertView.findViewById(R.id.number_axles_title);
                        holder.goodToGoPass = (TextView)convertView.findViewById(R.id.goodtogo_pass_title);
                        holder.payByCash = (TextView)convertView.findViewById(R.id.pay_by_cash_title);
                        holder.payByMail = (TextView)convertView.findViewById(R.id.pay_by_mail_title);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.numberAxles.setText(mData.get(position).get("number_axles"));
            holder.goodToGoPass.setText(mData.get(position).get("goodtogo_pass"));
            holder.payByCash.setText(mData.get(position).get("pay_by_cash"));
            holder.payByMail.setText(mData.get(position).get("pay_by_mail"));
            
            return convertView;
        }
    }
    
    public static class ViewHolder {
        public TextView numberAxles;
        public TextView goodToGoPass;
        public TextView payByCash;
        public TextView payByMail;
    }
}
