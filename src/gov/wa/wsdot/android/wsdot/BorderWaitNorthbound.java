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

import gov.wa.wsdot.android.wsdot.shared.BorderWaitItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BorderWaitNorthbound extends ListActivity {
	private static final String DEBUG_TAG = "BorderWaitNorthbound";
	private ArrayList<BorderWaitItem> borderWaitItems = null;
	private BorderWaitItemAdapter adapter;	
	
	private HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AnalyticsUtils.getInstance(this).trackPageView("/Canadian Border/Northbound");
		
        borderWaitItems = new ArrayList<BorderWaitItem>();
        this.adapter = new BorderWaitItemAdapter(this, R.layout.borderwait_row, borderWaitItems);
        setListAdapter(this.adapter);		

        routeImage.put(5, R.drawable.i5);
        routeImage.put(9, R.drawable.sr9);
        routeImage.put(539, R.drawable.sr539);
        routeImage.put(543, R.drawable.sr543);
        routeImage.put(97, R.drawable.us97);
        
        new GetBorderWaitItems().execute();
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.refresh_menu_items, menu);
    	
    	return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_refresh:
			this.adapter.clear();
			borderWaitItems.clear();
			new GetBorderWaitItems().execute();
		}
		
		return super.onOptionsItemSelected(item);
	}	

	private class GetBorderWaitItems extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(BorderWaitNorthbound.this);

		@Override
		protected void onPreExecute() {
			this.dialog.setTitle("Loading...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setMax(9);
			this.dialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                cancel(true);
	            }
			});
	        this.dialog.show();
		}
    	
	    protected void onCancelled() {
	        Toast.makeText(BorderWaitNorthbound.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/BorderCrossings.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("waittimes");
				JSONArray items = result.getJSONArray("items");
				borderWaitItems = new ArrayList<BorderWaitItem>();
				BorderWaitItem i = null;
							
				for (int j=0; j < items.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);
						i = new BorderWaitItem();
						i.setLane(item.getString("lane"));
						i.setUpdated(item.getString("updated"));
						i.setName(item.getString("name"));
						i.setRoute(item.getInt("route"));
						i.setDirection(item.getString("direction"));
						i.setWait(item.getInt("wait"));
						
						if (item.getString("direction").equals("northbound")) {
							borderWaitItems.add(i);
						}
						publishProgress(1);
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
			
            if(borderWaitItems != null && borderWaitItems.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<borderWaitItems.size();i++)
                adapter.add(borderWaitItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }
	
	private class BorderWaitItemAdapter extends ArrayAdapter<BorderWaitItem> {
        private ArrayList<BorderWaitItem> items;

        public BorderWaitItemAdapter(Context context, int textViewResourceId, ArrayList<BorderWaitItem> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        
        @SuppressWarnings("unused")
		public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.borderwait_row, null);
	        }
	        BorderWaitItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(R.id.toptext);
	            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
	            TextView rt = (TextView) v.findViewById(R.id.righttext);
	            ImageView iv = (ImageView) v.findViewById(R.id.icon);
	            if (tt != null) {
	            	tt.setText(o.getName() + " (" + o.getLane() + ")");
	            }
	            if(bt != null) {
            		bt.setText(o.getUpdated());
	            }
	            if (rt != null) {
	            	if (o.getWait() == -1) {
	            		rt.setText("N/A");
	            	} else if (o.getWait() < 5) {
	            		rt.setText("< 5 min");
	            	} else {
	            		rt.setText(o.getWait() + " min");
	            	}
	            }
	            iv.setImageResource(routeImage.get(o.getRoute()));
	        }
	        return v;
        }
        
	}	

}
