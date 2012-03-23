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

import gov.wa.wsdot.android.wsdot.shared.TwitterItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Twitter extends ListActivity {
	private static final String DEBUG_TAG = "Twitter";
	private ArrayList<TwitterItem> twitterItems = null;
	private TwitterItemAdapter adapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/News & Social Media/Twitter");
        
        setContentView(R.layout.main);
        twitterItems = new ArrayList<TwitterItem>();
        this.adapter = new TwitterItemAdapter(this, R.layout.news_item, twitterItems);
        ((TextView)findViewById(R.id.sub_section)).setText("Tweets");
        setListAdapter(this.adapter);
        new GetTwitterItems().execute();
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
			twitterItems.clear();
			new GetTwitterItems().execute();
		}
		
		return super.onOptionsItemSelected(item);
	}    
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(this, TwitterItemDetails.class);
		b.putString("description", twitterItems.get(position).getDescription());
		b.putString("link", twitterItems.get(position).getLink());
		b.putString("publishDate", twitterItems.get(position).getPubDate());
		intent.putExtras(b);
		startActivity(intent);
	}
	   
    private class GetTwitterItems extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(Twitter.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving latest tweets ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setMax(20);
			this.dialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                cancel(true);
	            }
			});
	        this.dialog.show();
		}
    	
	    protected void onCancelled() {
	        Toast.makeText(Twitter.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
	    	String patternStr = "(http://[A-Za-z0-9./]+)"; // Find bit.ly addresses
	    	Pattern pattern = Pattern.compile(patternStr);
	    	DateFormat parseDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy"); //e.g. Mon Aug 23 17:46:24 +0000 2010
	    	DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	    	
			try {
				URL url = new URL("http://twitter.com/statuses/user_timeline/14124059.json");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONArray items = new JSONArray(jsonFile);
				String d;			
				twitterItems = new ArrayList<TwitterItem>();
				TwitterItem i = null;
				
				for (int j=0; j < items.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);
						i = new TwitterItem();
						d = item.getString("text");
	                	Matcher matcher = pattern.matcher(d);
	                	boolean matchFound = matcher.find();
	                	if (matchFound) {
	                		String textLink = matcher.group();
	                		String hyperLink = "<a href=\"" + textLink + "\">" + textLink + "</a>";
	                		d = matcher.replaceFirst(hyperLink);
	                	}
						i.setTitle(item.getString("text"));
						i.setDescription(d);
						
		            	try {
		            		Date date = parseDateFormat.parse(item.getString("created_at"));
		            		i.setPubDate(displayDateFormat.format(date));
		            	} catch (Exception e) {
		            		i.setPubDate("");
		            		Log.e(DEBUG_TAG, "Error parsing date", e);
		            	}
						
						twitterItems.add(i);
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
            if (twitterItems != null && twitterItems.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<twitterItems.size();i++)
                adapter.add(twitterItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }
	
	private class TwitterItemAdapter extends ArrayAdapter<TwitterItem> {
        private ArrayList<TwitterItem> items;

        public TwitterItemAdapter(Context context, int textViewResourceId, ArrayList<TwitterItem> items) {
        	super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.news_item, null);
	        }
	        TwitterItem o = items.get(position);
	        if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (tt != null) {
                	tt.setText(o.getTitle());
                }
                if(bt != null) {
	        		bt.setText(o.getPubDate());
                }
	        }
	        return v;
        }
	}
}