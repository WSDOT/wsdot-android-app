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

import gov.wa.wsdot.android.wsdot.shared.FerriesRouteAlertItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RouteAlertsItems extends ListActivity {

	private static final String DEBUG_TAG = "RouteAlertItems";
	private FerriesRouteItem routeItems;
	private ArrayList<FerriesRouteAlertItem> routeAlertItems = null;
	private AlertItemAdapter adapter;
	private Runnable viewAlerts;
	DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		routeItems = (FerriesRouteItem)getIntent().getSerializableExtra("routeItems");
		setContentView(R.layout.main);
		((TextView)findViewById(R.id.sub_section)).setText("Ferries Route Alerts");
		routeAlertItems = new ArrayList<FerriesRouteAlertItem>();
        this.adapter = new AlertItemAdapter(this, android.R.layout.simple_list_item_2, routeAlertItems);
        setListAdapter(this.adapter);

        viewAlerts = new Runnable() {
        	public void run() {
        		getRouteAlerts();
        	}
        };
        
        Thread thread = new Thread(null, viewAlerts, "RouteAlertsBackground");
        thread.start();
	}

    private Runnable returnRes = new Runnable() {
        public void run() {
            if (routeAlertItems != null && routeAlertItems.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i=0;i<routeAlertItems.size();i++)
                	adapter.add(routeAlertItems.get(i));
            }
            adapter.notifyDataSetChanged();
        }
    };
    
    private void getRouteAlerts() {
        int numAlerts = routeItems.getFerriesRouteAlertItem().size();
        routeAlertItems = new ArrayList<FerriesRouteAlertItem>();
		
		for (int j=0; j<numAlerts; j++)
		{
			FerriesRouteAlertItem i = new FerriesRouteAlertItem();
			i.setAlertFullTitle(routeItems.getFerriesRouteAlertItem().get(j).getAlertFullTitle());
			i.setPublishDate(routeItems.getFerriesRouteAlertItem().get(j).getPublishDate());
			i.setAlertDescription(routeItems.getFerriesRouteAlertItem().get(j).getAlertDescription());
			i.setAlertFullText(routeItems.getFerriesRouteAlertItem().get(j).getAlertFullText());
			routeAlertItems.add(i);
		}
		runOnUiThread(returnRes);
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(this, RouteAlertsItemDetails.class);
		b.putString("AlertFullTitle", routeAlertItems.get(position).getAlertFullTitle());
		b.putString("AlertPublishDate", routeAlertItems.get(position).getPublishDate());
		b.putString("AlertDescription", routeAlertItems.get(position).getAlertDescription());
		b.putString("AlertFullText", routeAlertItems.get(position).getAlertFullText());
		intent.putExtras(b);
		startActivity(intent);		
	}

	private class AlertItemAdapter extends ArrayAdapter<FerriesRouteAlertItem> {
        private ArrayList<FerriesRouteAlertItem> items;

        public AlertItemAdapter(Context context, int textViewResourceId, ArrayList<FerriesRouteAlertItem> items) {
	        super(context, textViewResourceId, items);
	        this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(android.R.layout.simple_list_item_2, null);
	        }
	        FerriesRouteAlertItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(android.R.id.text1);
	            TextView bt = (TextView) v.findViewById(android.R.id.text2);
	            if (tt != null) {
	            	tt.setText(o.getAlertFullTitle());
	            }
	            if(bt != null) {
	            	try {
	            		Date date = new Date(Long.parseLong(o.getPublishDate()));
	            		bt.setText(displayDateFormat.format(date));
	            	} catch (Exception e) {
	            		Log.e(DEBUG_TAG, "Error parsing date", e);
	            	}	            	
	            	
	            }
	        }
	        return v;
        }
	}	
	
}
