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

import gov.wa.wsdot.android.wsdot.shared.ExpressLaneItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SeattleExpressLanes extends ListActivity {
	private ArrayList<ExpressLaneItem> expressLaneItems;
	private ExpressLaneItemAdapter adapter;
	
	private HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.details);

        routeImage.put(5, R.drawable.i5);
        routeImage.put(90, R.drawable.i90);
        
        expressLaneItems = (ArrayList<ExpressLaneItem>)getIntent().getSerializableExtra("ExpressLane");
        Collections.sort(expressLaneItems, new RouteComparator());
        this.adapter = new ExpressLaneItemAdapter(this, R.layout.details_item, expressLaneItems);
        setListAdapter(this.adapter);

        View title = findViewById(android.R.id.title);
        if (title instanceof TextView) {
            TextView titleText = (TextView)title;
            int dialogPadding = (int)getResources().getDimension(R.dimen.dialog_padding);
            titleText.setSingleLine();
            titleText.setEllipsize(TruncateAt.END);
            titleText.setGravity(Gravity.CENTER_VERTICAL);
            titleText.setPadding(dialogPadding, dialogPadding, dialogPadding, dialogPadding);
            titleText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dialog_menu_generic, 0, 0, 0);
            titleText.setCompoundDrawablePadding(dialogPadding);
        }       
        setTitle("Express Lanes");
    }
    
    private class RouteComparator implements Comparator<ExpressLaneItem> {

    	public int compare(ExpressLaneItem object1, ExpressLaneItem object2) {
			int route1 = object1.getRoute();
			int route2 = object2.getRoute();
			
			if (route1 > route2) {
				return 1;
			} else if (route1 < route2) {
				return -1;
			} else {
				return 0;
			}			
		}    	
    }
    
	private class ExpressLaneItemAdapter extends ArrayAdapter<ExpressLaneItem> {
        private ArrayList<ExpressLaneItem> items;

        public ExpressLaneItemAdapter(Context context, int textViewResourceId, ArrayList<ExpressLaneItem> items) {
	        super(context, textViewResourceId, items);
	        this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.details_item, null);
	        }
	        ExpressLaneItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(R.id.title);
	            TextView bt = (TextView) v.findViewById(R.id.description);
	            ImageView iv = (ImageView) v.findViewById(R.id.icon);
	            if (tt != null) {
	            	tt.setText(o.getTitle());
	            }
	            if(bt != null) {
            		bt.setText(o.getStatus());
	            }
	       		iv.setImageResource(routeImage.get(o.getRoute()));
	        }
	        return v;
        }
	}
}
