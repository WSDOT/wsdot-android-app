/*
 * Copyright (c) 2011 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot;

import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SR16TollRatesActivity extends ListActivity {
	private MyCustomAdapter adapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Toll Rates/SR 16");
        
        HashMap<String, String> map = null;
        String[][] vehicleTypeData = {
        		{"Passenger vehicle/Motorcycle", "Two", "$2.75", "$4.00"},
        		{"Passenger vehicle with small trailer", "Three", "$4.15", "$6.00"},
        		{"Tractor-trailer rig/passenger vehicle with trailer", "Four", "$5.50", "$8.00"},
        		{"Tractor with big trailer", "Five", "$6.90", "$10.00"},
        		{"Tractor with bigger trailer (six or more axles)", "Six or more", "$8.25", "$12.00"}
        		};
        
        adapter = new MyCustomAdapter();
        setListAdapter(adapter);
        
        map = new HashMap<String, String>();
        map.put("vehicle_type", "Vehicle Type");
        map.put("number_axles", "Number of Axles");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_cash", "Cash");
        adapter.addSeparatorItem(map);
        
        for (int i = 0; i < vehicleTypeData.length; i++) {
        	map = new HashMap<String, String>();
        	map.put("vehicle_type", vehicleTypeData[i][0]);
            map.put("number_axles", vehicleTypeData[i][1]);
        	map.put("goodtogo_pass", vehicleTypeData[i][2]);
            map.put("pay_by_cash", vehicleTypeData[i][3]);
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
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        	return (getItemViewType(position) != TYPE_SEPARATOR);  
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
            ViewHolder holder = null;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(R.layout.tollrates_sr16_row, null);
                        holder.vehicleType = (TextView)convertView.findViewById(R.id.vehicle_type);
                        holder.numberAxles = (TextView)convertView.findViewById(R.id.number_axles);
                        holder.goodToGoPass = (TextView)convertView.findViewById(R.id.goodtogo_pass);
                        holder.payByCash = (TextView)convertView.findViewById(R.id.pay_by_cash);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.tollrates_sr16_header, null);
                        holder.vehicleType = (TextView)convertView.findViewById(R.id.vehicle_type_title);
                        holder.numberAxles = (TextView)convertView.findViewById(R.id.number_axles_title);
                        holder.goodToGoPass = (TextView)convertView.findViewById(R.id.goodtogo_pass_title);
                        holder.payByCash = (TextView)convertView.findViewById(R.id.pay_by_cash_title);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.vehicleType.setText(mData.get(position).get("vehicle_type"));
            holder.numberAxles.setText(mData.get(position).get("number_axles"));
            holder.goodToGoPass.setText(mData.get(position).get("goodtogo_pass"));
            holder.payByCash.setText(mData.get(position).get("pay_by_cash"));
            return convertView;
        }
    }
    
    public static class ViewHolder {
    	public TextView vehicleType;
        public TextView numberAxles;
        public TextView goodToGoPass;
        public TextView payByCash;
    }
}
