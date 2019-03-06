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

package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class SR16TollRatesFragment extends BaseFragment {
	
    private static final String TAG = SR16TollRatesFragment.class.getSimpleName();
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
		
        HashMap<String, String> map = null;
        String[][] vehicleTypeData = {
           		{"Two (includes motorcycle)", "$5.00", "$6.00", "$7.00"},
        		{"Three", "$7.50", "$9.00", "$10.50"},
        		{"Four", "$10.00", "$12.00", "$14.00"},
        		{"Five", "$12.50", "$15.00", "$17.50"},
        		{"Six or more", "$15.00", "$18.00", "$21.00"}
        		};
        
        map = new HashMap<String, String>();
        map.put("number_axles", "Number of Axles");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_cash", "Cash");
        map.put("pay_by_mail", "Pay By Mail");
        mAdapter.addSeparatorItem(map);
        
        for (int i = 0; i < vehicleTypeData.length; i++) {
        	map = new HashMap<String, String>();
            map.put("number_axles", vehicleTypeData[i][0]);
        	map.put("goodtogo_pass", vehicleTypeData[i][1]);
            map.put("pay_by_cash", vehicleTypeData[i][2]);
            map.put("pay_by_mail", vehicleTypeData[i][3]);
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

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        ArrayList<HashMap<String, String>> mData = new ArrayList<HashMap<String, String>>();
        private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = null;

            switch (viewType) {
                case TYPE_ITEM:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_sr16_row, parent, false);
                    return new ItemViewHolder(itemView);
                case TYPE_SEPARATOR:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_sr16_header, parent, false);
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
                itemholder.numberAxles.setText(map.get("number_axles"));
                itemholder.numberAxles.setTypeface(tf);
                itemholder.goodToGoPass.setText(map.get("goodtogo_pass"));
                itemholder.goodToGoPass.setTypeface(tf);
                itemholder.payByCash.setText(map.get("pay_by_cash"));
                itemholder.payByCash.setTypeface(tf);

                itemholder.payByMail.setText(map.get("pay_by_mail"));
                itemholder.payByMail.setTypeface(tf);
            }else{
                titleholder = (TitleViewHolder) viewholder;
                titleholder.numberAxles.setText(map.get("number_axles"));
                titleholder.numberAxles.setTypeface(tfb);
                titleholder.goodToGoPass.setText(map.get("goodtogo_pass"));
                titleholder.goodToGoPass.setTypeface(tfb);
                titleholder.payByCash.setText(map.get("pay_by_cash"));
                titleholder.payByCash.setTypeface(tfb);
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
        protected TextView numberAxles;
        protected TextView goodToGoPass;
        protected TextView payByCash;
        protected TextView payByMail;

        public ItemViewHolder(View itemView) {
            super(itemView);
            numberAxles = (TextView) itemView.findViewById(R.id.number_axles);
            goodToGoPass = (TextView) itemView.findViewById(R.id.goodtogo_pass);
            payByCash = (TextView) itemView.findViewById(R.id.pay_by_cash);
            payByMail = (TextView) itemView.findViewById(R.id.pay_by_mail);
        }
    }
    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        protected TextView numberAxles;
        protected TextView goodToGoPass;
        protected TextView payByCash;
        protected TextView payByMail;

        public TitleViewHolder(View itemView) {
            super(itemView);
            numberAxles = (TextView) itemView.findViewById(R.id.number_axles_title);
            goodToGoPass = (TextView) itemView.findViewById(R.id.goodtogo_pass_title);
            payByCash = (TextView) itemView.findViewById(R.id.pay_by_cash_title);
            payByMail = (TextView) itemView.findViewById(R.id.pay_by_mail_title);
        }
    }
}
