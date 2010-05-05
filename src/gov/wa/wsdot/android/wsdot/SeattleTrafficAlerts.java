package gov.wa.wsdot.android.wsdot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SeattleTrafficAlerts extends ListActivity {
	private static final String DEBUG_TAG = "SeattleIncidents";
	private Stack<SeattleIncidentItem> seattleIncidentItems = null;
	private SeattleIncidentItemAdapter adapter;
    private Stack<String> blocking = null;
    private Stack<String> construction = null;
    private Stack<String> special = null;
    private Stack<String> closed = null;
    private Stack<String> amberalert = null;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seattle_incidents);
        seattleIncidentItems = new Stack<SeattleIncidentItem>();      
        this.adapter = new SeattleIncidentItemAdapter(this, R.layout.seattle_incident_item, seattleIncidentItems);
        setListAdapter(this.adapter);
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
            if (seattleIncidentItems != null && seattleIncidentItems.size() > 0) {
            	adapter.notifyDataSetChanged();
                while (!seattleIncidentItems.empty()) {
                	adapter.add(seattleIncidentItems.pop());                	
                }
            }
            adapter.notifyDataSetChanged();
		}   
    }   
	
	private class SeattleIncidentItemAdapter extends ArrayAdapter<SeattleIncidentItem> {
		private Stack<SeattleIncidentItem> items;

        public SeattleIncidentItemAdapter(Context context, int textViewResourceId, Stack<SeattleIncidentItem> items) {
        	super(context, textViewResourceId, items);
        	this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.seattle_incident_item, null);
            }
            SeattleIncidentItem o = items.get(position);
            if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.title);
                TextView bt = (TextView) v.findViewById(R.id.description);
                if (tt != null) {
                      tt.setText(o.getTitle());
                }
                if(bt != null) {
                    bt.setText(o.getDescription());
                }                        
            }
            return v;
        }
	}
}