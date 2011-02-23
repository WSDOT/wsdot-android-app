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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SeattleTrafficAlerts extends ListActivity {
	private static final String DEBUG_TAG = "SeattleIncidents";
	private Stack<SeattleIncidentItem> seattleIncidentItems = null;
    private Stack<String> blocking = null;
    private Stack<String> construction = null;
    private Stack<String> special = null;
    private Stack<String> closed = null;
    private Stack<String> amberalert = null;
	
    private MyCustomAdapter adapter;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((TextView)findViewById(R.id.sub_section)).setText("Seattle Area Alerts");
        seattleIncidentItems = new Stack<SeattleIncidentItem>();      
      
        adapter = new MyCustomAdapter();
        setListAdapter(adapter);
        
        new GetSeattleIncidentItems().execute();
    }
    
    private class GetSeattleIncidentItems extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(SeattleTrafficAlerts.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving Seattle area alerts ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setCancelable(true);
	        this.dialog.setMax(10);
	        this.dialog.show();
		}
    	
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
	    	blocking = new Stack<String>();
	    	construction = new Stack<String>();
	    	special = new Stack<String>();
	    	closed = new Stack<String>();
	    	amberalert = new Stack<String>();  	
	        List<Integer> blockingCategory = new ArrayList<Integer>();
	        List<Integer> constructionCategory = new ArrayList<Integer>();
	        List<Integer> specialCategory = new ArrayList<Integer>();
	        
	        blockingCategory.add(0); // Traffic conditions
	        blockingCategory.add(4); // Incident
	        blockingCategory.add(5); // Collision
	        blockingCategory.add(6); // Disabled vehicle
	        blockingCategory.add(10); // Water over roadway
	        blockingCategory.add(11); // Obstruction
	        blockingCategory.add(30); // Fallen tree
	        
	        constructionCategory.add(7); // Closures
	        constructionCategory.add(8); // Road work
	        constructionCategory.add(9); // Maintenance

	        specialCategory.add(2); // Winter driving restriction
	        specialCategory.add(12); // Sporting event
	        specialCategory.add(13); // Seahawks game
	        specialCategory.add(28); // Sounders game
	        specialCategory.add(14); // Mariners game
	        specialCategory.add(15); // Special event
	        specialCategory.add(16); // Restriction
	        specialCategory.add(17); // Flammable restriction
	        specialCategory.add(29); // Huskies game
	        
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/SeattleIncidents.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;

				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("incidents");
				JSONArray items = result.getJSONArray("items");
				seattleIncidentItems = new Stack<SeattleIncidentItem>();
				SeattleIncidentItem i = null;
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					i = new SeattleIncidentItem();
					i.setTitle(item.getString("title"));
					i.setDescription(item.getString("description"));
					i.setCategory(item.getInt("category"));
					i.setGuid(item.getInt("guid"));
					
					// Check if Traffic Management Center is closed
					if (i.getCategory().equals(27)) {
						closed.push(i.getDescription());
						break; // TSMC is closed so stop here
					}
					// Check if there is an active amber alert
					else if (i.getCategory().equals(24)) {
						amberalert.push(i.getDescription());
					}
					else if (blockingCategory.contains(i.getCategory())) {
						blocking.push(i.getDescription());
					}
	                else if (constructionCategory.contains(i.getCategory())) {
	                    construction.push(i.getDescription());
	                }
	                else if (specialCategory.contains(i.getCategory())) {
	                    special.push(i.getDescription());
	                }								
					seattleIncidentItems.push(i);
					publishProgress(1);
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
			if (amberalert != null && amberalert.size() != 0) {
				adapter.addSeparatorItem("Amber Alert");
				while (!amberalert.empty()) {
					adapter.addItem(amberalert.pop());
				}
			}
			if (closed != null && closed.size() == 0) {
				adapter.addSeparatorItem("Blocking Incidents");				
				if (blocking.empty()) {
					adapter.addItem("None reported");
				} else {
					while (!blocking.empty()) {
						adapter.addItem(blocking.pop());
					}					
				}
				adapter.addSeparatorItem("Construction Closures");
				if (construction.empty()) {
					adapter.addItem("None reported");
				} else {
					while (!construction.empty()) {
						adapter.addItem(construction.pop());
					}					
				}
				adapter.addSeparatorItem("Special Events");
				if (special.empty()) {
					adapter.addItem("None reported");
				} else {
					while (!special.empty()) {
						adapter.addItem(special.pop());
					}					
				}
			} else {
				adapter.addItem(closed.pop());
			}
			adapter.notifyDataSetChanged();
		}   
    }   
	
    private class MyCustomAdapter extends BaseAdapter {
 
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
 
        private ArrayList<String> mData = new ArrayList<String>();
        private LayoutInflater mInflater;
 
        private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
 
        public MyCustomAdapter() {
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
 
        public void addItem(final String item) {
            mData.add(item);
            notifyDataSetChanged();
        }
 
        public void addSeparatorItem(final String item) {
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
        	return (getItemViewType(position) != TYPE_SEPARATOR);  
        }          
        
        @Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
 
        public int getCount() {
            return mData.size();
        }
 
        public String getItem(int position) {
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
                        convertView = mInflater.inflate(R.layout.seattle_incident_item, null);
                        holder.textView = (TextView)convertView.findViewById(R.id.description);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.list_header, null);
                        holder.textView = (TextView)convertView.findViewById(R.id.list_header_title);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.textView.setText(mData.get(position));
            return convertView;
        }
 
    }
 
    public static class ViewHolder {
        public TextView textView;
    }	
}