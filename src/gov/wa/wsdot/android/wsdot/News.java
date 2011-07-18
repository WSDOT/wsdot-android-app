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

import gov.wa.wsdot.android.wsdot.shared.NewsItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

public class News extends ListActivity {
	private static final String DEBUG_TAG = "News";
	private ArrayList<NewsItem> newsItems = null;
	private NewsItemAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/News & Social Media/News");
        
        setContentView(R.layout.main);
        ((TextView)findViewById(R.id.sub_section)).setText("News");
        newsItems = new ArrayList<NewsItem>();
        this.adapter = new NewsItemAdapter(this, R.layout.news_item, newsItems);
        setListAdapter(this.adapter);
        new GetNewsItems().execute();
    }
	
    private class GetNewsItems extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(News.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving news headlines ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setMax(10);
			this.dialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                cancel(true);
	            }
			});
	        this.dialog.show();
		}
    			
	    protected void onCancelled() {
	        Toast.makeText(News.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
	    
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
			DateFormat parseDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
			DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
			
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/News.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("news");
				JSONArray items = result.getJSONArray("items");
				newsItems = new ArrayList<NewsItem>();
				NewsItem i = null;
				
				for (int j=0; j < items.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);
						i = new NewsItem();
						i.setTitle(item.getString("title"));
						i.setDescription(item.getString("description"));
						i.setLink(item.getString("link"));
						
		            	try {
		            		Date date = parseDateFormat.parse(item.getString("pubdate"));
		            		i.setPubDate(displayDateFormat.format(date));
		            	} catch (Exception e) {
		            		i.setPubDate("");
		            		Log.e(DEBUG_TAG, "Error parsing date", e);
		            	}				
						
						newsItems.add(i);
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
            if (newsItems != null && newsItems.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i=0;i<newsItems.size();i++)
                adapter.add(newsItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(this, NewsItemDetails.class);
		b.putString("heading", newsItems.get(position).getTitle());
		b.putString("description", newsItems.get(position).getDescription());
		b.putString("link", newsItems.get(position).getLink());
		b.putString("publishDate", newsItems.get(position).getPubDate());
		intent.putExtras(b);
		startActivity(intent);
	}    
	
	private class NewsItemAdapter extends ArrayAdapter<NewsItem> {
        private ArrayList<NewsItem> items;

        public NewsItemAdapter(Context context, int textViewResourceId, ArrayList<NewsItem> items) {
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
	        NewsItem o = items.get(position);
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