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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RouteAlerts extends ListActivity {
	private static final String DEBUG_TAG = "RouteAlerts";
	private ArrayList<FerriesRouteItem> routeItems = null;
	private RouteItemAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((TextView)findViewById(R.id.sub_section)).setText("Ferries Route Alerts");
        routeItems = new ArrayList<FerriesRouteItem>();
        this.adapter = new RouteItemAdapter(this, android.R.layout.simple_list_item_1, routeItems);
        setListAdapter(this.adapter);
        new GetRouteAlerts().execute();
    }
	
    private class GetRouteAlerts extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(RouteAlerts.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving routes with alerts ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        this.dialog.setCancelable(true);
	        this.dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/WSFRouteAlerts.js.gz");
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
				FerriesRouteItem i = null;
				FerriesRouteAlertItem a = null;
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					i = new FerriesRouteItem();
					i.setRouteID(item.getInt("RouteID"));
					i.setDescription(item.getString("Description"));
					
					JSONArray alerts = item.getJSONArray("RouteAlert");
					for (int k=0; k < alerts.length(); k++) {
						JSONObject alert = alerts.getJSONObject(k);
						a = new FerriesRouteAlertItem();
						a.setBulletinID(alert.getInt("BulletinID"));
						a.setPublishDate(alert.getString("PublishDate").substring(6, 19));
						a.setAlertDescription(alert.getString("AlertDescription"));
						a.setAlertFullTitle(alert.getString("AlertFullTitle"));
						
						if (alert.getString("AlertFullText").equals("null")) {
							a.setAlertFullText("");
						}
						else {
							a.setAlertFullText(alert.getString("AlertFullText"));
						}
						
						i.setFerriesRouteAlertItem(a);
					}				
					routeItems.add(i);
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
		Intent intent = new Intent(this, RouteAlertsItems.class);
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
