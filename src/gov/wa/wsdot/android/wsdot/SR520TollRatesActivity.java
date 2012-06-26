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

public class SR520TollRatesActivity extends ListActivity {
	
	private MyCustomAdapter adapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Toll Rates/SR 520");
        
        HashMap<String, String> map = null;
        String[][] weekdayData = {
        		{"Midnight to 5 a.m.", "0", "0"},
        		{"5 a.m. to 6 a.m.", "$1.64", "$3.18"},
        		{"6 a.m. to 7 a.m.", "$2.87", "$4.41"},
        		{"7 a.m. to 9 a.m.", "$3.59", "$5.13"},
        		{"9 a.m. to 10 a.m.", "$2.87", "$4.41"},
        		{"10 a.m. to 2 p.m.", " $2.31", "$3.84"},
        		{"2 p.m. to 3 p.m.", "$2.87", "$4.41"},
        		{"3 p.m. to 6 p.m.", "$3.59", "$5.13"},
        		{"6 p.m. to 7 p.m.", "$2.87", "$4.41"},
        		{"7 p.m. to 9 p.m.", "$2.31", "$3.84"},
        		{"9 p.m. to 11 p.m.", "$1.64", "$3.18"},
        		{"11 p.m. to 11:59 p.m.", "0", "0"}
        		};

        String[][] weekendData = {
        		{"Midnight to 5 a.m.", "0", "0"},
        		{"5 a.m. to 8 a.m.", "$1.13", "$2.67"},
        		{"8 a.m. to 11 a.m.", "$1.69", "$3.23"},
        		{"11 a.m. to 6 p.m.", "$2.26", "$3.79"},
        		{"6 p.m. to 9 p.m.", "$1.69", "$3.23"},
        		{"9 p.m. to 11 p.m.", " $1.13", "$2.67"},
        		{"11 p.m. to 11:59 p.m.", "0", "0"}
        		};
                
        adapter = new MyCustomAdapter();
        setListAdapter(adapter);
        
        map = new HashMap<String, String>();
        map.put("hours", "Mondays - Fridays");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_mail", "Pay By Mail");
        adapter.addSeparatorItem(map);
        
        BuildAdapterData(weekdayData);
        
        map = new HashMap<String, String>();
        map.put("hours", "Saturdays and Sundays");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_mail", "Pay By Mail");
        adapter.addSeparatorItem(map);
        
        BuildAdapterData(weekendData);
    }
    
    private void BuildAdapterData(String[][] data) {
    	HashMap<String, String> map = null;
    	
        for (int i = 0; i < data.length; i++) {
        	map = new HashMap<String, String>();
        	map.put("hours", data[i][0]);
            map.put("goodtogo_pass", data[i][1]);
            map.put("pay_by_mail", data[i][2]);
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
            ViewHolder holder = null;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(R.layout.tollrates_sr520_row, null);
                        holder.hours = (TextView)convertView.findViewById(R.id.hours);
                        holder.goodToGoPass = (TextView)convertView.findViewById(R.id.goodtogo_pass);
                        holder.payByMail = (TextView)convertView.findViewById(R.id.pay_by_mail);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.tollrates_sr520_header, null);
                        holder.hours = (TextView)convertView.findViewById(R.id.hours_title);
                        holder.goodToGoPass = (TextView)convertView.findViewById(R.id.goodtogo_pass_title);
                        holder.payByMail = (TextView)convertView.findViewById(R.id.pay_by_mail_title);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.hours.setText(mData.get(position).get("hours"));
            holder.goodToGoPass.setText(mData.get(position).get("goodtogo_pass"));
            holder.payByMail.setText(mData.get(position).get("pay_by_mail"));
            return convertView;
        }
    }
    
    public static class ViewHolder {
        public TextView hours;
        public TextView goodToGoPass;
        public TextView payByMail;
    }
}
