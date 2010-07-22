/*
 * Copyright (c) 2010 Washington State Department of Transportation
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RouteSchedulesDays extends ListActivity {

	private FerriesRouteItem routeItems;
	private ArrayList<Date> date = null;
	private DaysOfWeekAdapter adapter;
	private Runnable viewSchedules;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		routeItems = (FerriesRouteItem)getIntent().getSerializableExtra("routeItems");
		setContentView(R.layout.main);
		((TextView)findViewById(R.id.sub_section)).setText("Ferries Route Schedules");
		date = new ArrayList<Date>();
        this.adapter = new DaysOfWeekAdapter(this, android.R.layout.simple_list_item_1, date);
        setListAdapter(this.adapter);

        viewSchedules = new Runnable() {
        	public void run() {
        		getDates();
        	}
        };
        
        Thread thread = new Thread(null, viewSchedules, "RouteSchedulesBackground");
        thread.start();
	}

    private Runnable returnRes = new Runnable() {
        public void run() {
            if (date != null && date.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i=0;i<date.size();i++)
                	adapter.add(date.get(i));
            }
            adapter.notifyDataSetChanged();
        }
    };

    private void getDates() {
    	Calendar cal = Calendar.getInstance();
        date = new ArrayList<Date>();
		
		for (int i=0; i<7; i++)
		{
			date.add(cal.getTime());
			cal.add(Calendar.DATE, 1);
		}
		runOnUiThread(returnRes);
    }
	
    /*
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
	*/

	private class DaysOfWeekAdapter extends ArrayAdapter<Date> {
        private ArrayList<Date> items;

        public DaysOfWeekAdapter(Context context, int textViewResourceId, ArrayList<Date> items) {
	        super(context, textViewResourceId, items);
	        this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        String strDateFormat = "EEEE";
	        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
	        
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(android.R.layout.simple_list_item_1, null);
	        }
	        Date o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(android.R.id.text1);
	            if(tt != null) {
	            	tt.setText(sdf.format(o.getTime()));
	            }
	        }
	        return v;
        }
	}	
	
}
