/*
 * Copyright (c) 2014 Washington State Department of Transportation
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SR520TollRatesFragment extends ListFragment {
	
	private MyCustomAdapter adapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
    }
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        return root;
    }	
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
        HashMap<String, String> map = null;
        String[][] weekdayData = {
        		{"Midnight to 5 AM", "0", "0"},
        		{"5 AM to 6 AM", "$1.70", "$3.25"},
        		{"6 AM to 7 AM", "$2.95", "$4.50"},
        		{"7 AM to 9 AM", "$3.70", "$5.25"},
        		{"9 AM to 10 AM", "$2.95", "$4.50"},
        		{"10 AM to 2 PM", " $2.35", "$3.95"},
        		{"2 PM to 3 PM", "$2.95", "$4.50"},
        		{"3 PM to 6 PM", "$3.70", "$5.25"},
        		{"6 PM to 7 PM", "$2.95", "$4.50"},
        		{"7 PM to 9 PM", "$2.35", "$3.95"},
        		{"9 PM to 11 PM", "$1.70", "$3.25"},
        		{"11 PM to 11:59 PM", "0", "0"}
        		};

        String[][] weekendData = {
        		{"Midnight to 5 AM", "0", "0"},
        		{"5 AM to 8 AM", "$1.15", "$2.75"},
        		{"8 AM to 11 AM", "$1.75", "$3.30"},
        		{"11 AM to 6 PM", "$2.30", "$3.90"},
        		{"6 PM to 9 PM", "$1.75", "$3.30"},
        		{"9 PM to 11 PM", " $1.15", "$2.75"},
        		{"11 PM to 11:59 PM", "0", "0"}
        		};
                
        adapter = new MyCustomAdapter();
        setListAdapter(adapter);
        
        map = new HashMap<String, String>();
        map.put("hours", "Monday to Friday");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_mail", "Pay By Mail");
        adapter.addSeparatorItem(map);
        
        BuildAdapterData(weekdayData);
        
        map = new HashMap<String, String>();
        map.put("hours", "Weekends and Holidays");
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
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
 
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
                        convertView = mInflater.inflate(R.layout.tollrates_sr520_row, null);
                        holder.hours = (TextView)convertView.findViewById(R.id.hours);
                        holder.hours.setTypeface(tf);
                        holder.goodToGoPass = (TextView)convertView.findViewById(R.id.goodtogo_pass);
                        holder.goodToGoPass.setTypeface(tf);
                        holder.payByMail = (TextView)convertView.findViewById(R.id.pay_by_mail);
                        holder.payByMail.setTypeface(tf);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.tollrates_sr520_header, null);
                        holder.hours = (TextView)convertView.findViewById(R.id.hours_title);
                        holder.hours.setTypeface(tfb);
                        holder.goodToGoPass = (TextView)convertView.findViewById(R.id.goodtogo_pass_title);
                        holder.goodToGoPass.setTypeface(tfb);
                        holder.payByMail = (TextView)convertView.findViewById(R.id.pay_by_mail_title);
                        holder.payByMail.setTypeface(tfb);
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
