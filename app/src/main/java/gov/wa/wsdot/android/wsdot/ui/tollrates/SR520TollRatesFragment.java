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

package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;

public class SR520TollRatesFragment extends BaseFragment {
	
    private static final String TAG = SR520TollRatesFragment.class.getSimpleName();
    private Adapter mAdapter;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
    }
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new Adapter();

        mRecyclerView.setAdapter(mAdapter);

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
        		{"5 AM to 6 AM", "$1.80", "$3.45"},
        		{"6 AM to 7 AM", "$3.10", "$4.70"},
        		{"7 AM to 9 AM", "$3.90", "$5.55"},
        		{"9 AM to 10 AM", "$3.10", "$4.70"},
        		{"10 AM to 2 PM", " $2.45", "$4.15"},
        		{"2 PM to 3 PM", "$3.10", "$4.70"},
        		{"3 PM to 6 PM", "$3.90", "$5.55"},
        		{"6 PM to 7 PM", "$3.10", "$4.70"},
        		{"7 PM to 9 PM", "$2.45", "$4.15"},
        		{"9 PM to 11 PM", "$1.80", "$3.45"},
        		{"11 PM to 11:59 PM", "0", "0"}
        		};

        String[][] weekendData = {
        		{"Midnight to 5 AM", "0", "0"},
        		{"5 AM to 8 AM", "$1.25", "$2.85"},
        		{"8 AM to 11 AM", "$1.85", "$3.50"},
        		{"11 AM to 6 PM", "$2.40", "$4.10"},
        		{"6 PM to 9 PM", "$1.85", "$3.50"},
        		{"9 PM to 11 PM", " $1.25", "$2.85"},
        		{"11 PM to 11:59 PM", "0", "0"}
        		};
        
        map = new HashMap<String, String>();
        map.put("hours", "Monday to Friday");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_mail", "Pay By Mail");
        mAdapter.addSeparatorItem(map);
        
        BuildAdapterData(weekdayData);
        
        map = new HashMap<String, String>();
        map.put("hours", "Weekends and Holidays");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_mail", "Pay By Mail");
        mAdapter.addSeparatorItem(map);
        
        BuildAdapterData(weekendData);		
	}

	private void BuildAdapterData(String[][] data) {
    	HashMap<String, String> map = null;
    	
        for (int i = 0; i < data.length; i++) {
        	map = new HashMap<String, String>();
        	map.put("hours", data[i][0]);
            map.put("goodtogo_pass", data[i][1]);
            map.put("pay_by_mail", data[i][2]);
            mAdapter.addItem(map);
        }
    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;

        private TreeSet<Integer> mSeparatorsSet = new TreeSet<>();
        private ArrayList<HashMap<String, String>> mData = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = null;

            switch (viewType) {
                case TYPE_ITEM:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_sr520_row, parent, false);
                    return new ItemViewHolder(itemView);
                case TYPE_SEPARATOR:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_sr520_header, parent, false);
                    return new TitleViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, int position) {

            ItemViewHolder itemholder;
            TitleViewHolder titleholder;

            HashMap<String, String> map = mData.get(position);

            if (getItemViewType(position) == TYPE_ITEM){
                itemholder = (ItemViewHolder) viewholder;
                itemholder.hours.setText(map.get("hours"));
                itemholder.hours.setTypeface(tf);
                itemholder.goodToGoPass.setText(map.get("goodtogo_pass"));
                itemholder.goodToGoPass.setTypeface(tf);
                itemholder.payByMail.setText(map.get("pay_by_mail"));
                itemholder.payByMail.setTypeface(tf);
            }else{
                titleholder = (TitleViewHolder) viewholder;
                titleholder.hours.setText(map.get("hours"));
                titleholder.hours.setTypeface(tfb);
                titleholder.goodToGoPass.setText(map.get("goodtogo_pass"));
                titleholder.goodToGoPass.setTypeface(tfb);
                titleholder.payByMail.setText(map.get("pay_by_mail"));
                titleholder.payByMail.setTypeface(tfb);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
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
            hours = (TextView) itemView.findViewById(R.id.hours);
            goodToGoPass = (TextView) itemView.findViewById(R.id.goodtogo_pass);
            payByMail = (TextView) itemView.findViewById(R.id.pay_by_mail);
        }
    }
    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        protected TextView hours;
        protected TextView goodToGoPass;
        protected TextView payByMail;

        public TitleViewHolder(View itemView) {
            super(itemView);
            hours = (TextView) itemView.findViewById(R.id.hours_title);
            goodToGoPass = (TextView) itemView.findViewById(R.id.goodtogo_pass_title);
            payByMail = (TextView) itemView.findViewById(R.id.pay_by_mail_title);
        }
    }
}
