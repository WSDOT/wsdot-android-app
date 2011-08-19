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

import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesRouteItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RouteSchedules extends ListActivity {
	private static final String DEBUG_TAG = "RouteSchedules";
	private ArrayList<FerriesRouteItem> routeItems = null;
	private RouteItemAdapter adapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Route Schedules");
        
        setContentView(R.layout.main);
        ((TextView)findViewById(R.id.sub_section)).setText("Ferries Route Schedules");
        routeItems = new ArrayList<FerriesRouteItem>();
        this.adapter = new RouteItemAdapter(this, android.R.layout.simple_list_item_1, routeItems);
        setListAdapter(this.adapter);
        new GetRouteAlerts().execute();
    }
	
    private class GetRouteAlerts extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(RouteSchedules.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving route schedules ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			this.dialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                cancel(true);
	            }
			});
	        this.dialog.show();
		}

	    protected void onCancelled() {
	        Toast.makeText(RouteSchedules.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/WSFRouteSchedules.js.gz");
				URLConnection urlConn = url.openConnection();
				
				BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
                GZIPInputStream gzin = new GZIPInputStream(bis);
                InputStreamReader is = new InputStreamReader(gzin);
                BufferedReader in = new BufferedReader(is);
				
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONArray items = new JSONArray(jsonFile);
				routeItems = new ArrayList<FerriesRouteItem>();
				FerriesRouteItem route = null;
				FerriesScheduleDateItem scheduleDate = null;
				FerriesTerminalItem terminal = null;
				FerriesAnnotationsItem notes = null;
				FerriesScheduleTimesItem timesItem = null;
				FerriesAnnotationIndexesItem indexesItem = null;
				
				for (int i=0; i < items.length(); i++) {
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(i);
						route = new FerriesRouteItem();
						route.setRouteID(item.getInt("RouteID"));
						route.setDescription(item.getString("Description"));
						
						JSONArray dates = item.getJSONArray("Date");
						for (int j=0; j < dates.length(); j++) {
							JSONObject date = dates.getJSONObject(j);
							scheduleDate = new FerriesScheduleDateItem();
							scheduleDate.setDate(date.getString("Date").substring(6, 19));
							
							JSONArray sailings = date.getJSONArray("Sailings");
							for (int k=0; k < sailings.length(); k++) {
								JSONObject sailing = sailings.getJSONObject(k);
								terminal = new FerriesTerminalItem();
								terminal.setArrivingTerminalID(sailing.getInt("ArrivingTerminalID"));
								terminal.setArrivingTerminalName(sailing.getString("ArrivingTerminalName"));
								terminal.setDepartingTerminalID(sailing.getInt("DepartingTerminalID"));
								terminal.setDepartingTerminalName(sailing.getString("DepartingTerminalName"));
								
								JSONArray annotations = sailing.getJSONArray("Annotations");
								for (int l=0; l < annotations.length(); l++) {
									notes = new FerriesAnnotationsItem();
									notes.setAnnotation(annotations.getString(l));
									terminal.setAnnotations(notes);	
								}
								
								JSONArray times = sailing.getJSONArray("Times");
								for (int m=0; m < times.length(); m++) {
									JSONObject time = times.getJSONObject(m);
									timesItem = new FerriesScheduleTimesItem();
									timesItem.setDepartingTime(time.getString("DepartingTime").substring(6, 19));
									
									
									JSONArray annotationIndexes = time.getJSONArray("AnnotationIndexes");
									for (int n=0; n < annotationIndexes.length(); n++) {
										indexesItem = new FerriesAnnotationIndexesItem();
										indexesItem.setIndex(annotationIndexes.getInt(n));
										timesItem.setAnnotationIndexes(indexesItem);									
									}
									
									terminal.setScheduleTimes(timesItem);
								}
								
								scheduleDate.setFerriesTerminalItem(terminal);
							}
							
							route.setFerriesScheduleDateItem(scheduleDate);
						}
						
						routeItems.add(route);
					} else {
						break;
					}
				}			

			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
            if (routeItems != null && routeItems.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i=0;i<routeItems.size();i++)
                adapter.add(routeItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(this, RouteSchedulesDays.class);
		b.putSerializable("routeItems", routeItems.get(position));
		intent.putExtras(b);
		startActivity(intent);
	}  
	
	private class RouteItemAdapter extends ArrayAdapter<FerriesRouteItem> {
        private ArrayList<FerriesRouteItem> items;

        public RouteItemAdapter(Context context, int textViewResourceId, ArrayList<FerriesRouteItem> items) {
	        super(context, textViewResourceId, items);
	        this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(android.R.layout.simple_list_item_1, null);
	        }
	        FerriesRouteItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(android.R.id.text1);
	            if (tt != null) {
	            	tt.setText(o.getDescription());
	            }
	        }
	        return v;
        }
	}
}
