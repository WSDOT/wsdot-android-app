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

import gov.wa.wsdot.android.wsdot.shared.BlogItem;
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

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class Blog extends SherlockListActivity {
	private static final String DEBUG_TAG = "Blog";
	private ArrayList<BlogItem> blogItems = null;
	private BlogItemAdapter adapter;
	DateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz"); //e.g. 2010-08-20T10:11:00.000-07:00
	DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	private View mLoadingSpinner;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/News & Social Media/Blog");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.fragment_list_with_spinner);
        mLoadingSpinner = findViewById(R.id.loading_spinner);
        
        blogItems = new ArrayList<BlogItem>();
        this.adapter = new BlogItemAdapter(this, R.layout.simple_list_item, blogItems);
        setListAdapter(this.adapter);
        new GetBlogItems().execute();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.refresh, menu);
    	
    	return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	    	return true;		
		case R.id.menu_refresh:
			this.adapter.clear();
			blogItems.clear();
			new GetBlogItems().execute();
		}
		
		return super.onOptionsItemSelected(item);
	}    
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Bundle b = new Bundle();
		Intent intent = new Intent(this, BlogItemDetails.class);
		b.putString("title", blogItems.get(position).getTitle());
		b.putString("content", blogItems.get(position).getContent());
		b.putString("link", blogItems.get(position).getLink());
		intent.putExtras(b);
		startActivity(intent);
	}

	private class GetBlogItems extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}
		
	    protected void onCancelled() {
	        Toast.makeText(Blog.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://wsdotblog.blogspot.com/feeds/posts/default?alt=json");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject data = obj.getJSONObject("feed");			
				JSONArray entries = data.getJSONArray("entry");
				blogItems = new ArrayList<BlogItem>();
				BlogItem i = null;
				
				for (int j=0; j < entries.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject entry = entries.getJSONObject(j);
						i = new BlogItem();
						i.setTitle(entry.getJSONObject("title").getString("$t"));
						i.setPublished(entry.getJSONObject("published").getString("$t"));
						i.setContent(entry.getJSONObject("content").getString("$t"));
						i.setLink(entry.getJSONArray("link").getJSONObject(4).getString("href"));
						
						blogItems.add(i);
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
			mLoadingSpinner.setVisibility(View.GONE);
			
			if(blogItems != null && blogItems.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<blogItems.size();i++)
                adapter.add(blogItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}
	}   
	
	private class BlogItemAdapter extends ArrayAdapter<BlogItem> {
        //private ArrayList<BlogItem> items;
        private Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

        public BlogItemAdapter(Context context, int textViewResourceId, ArrayList<BlogItem> items) {
	        super(context, textViewResourceId, items);
	        //this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = getLayoutInflater().inflate(R.layout.simple_list_item, null);
	        }
	        BlogItem o = getItem(position);
	        if (o != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.title);
	            tt.setTypeface(tfb);
	            TextView bt = (TextView) convertView.findViewById(R.id.description);
	            bt.setTypeface(tf);
	            if (tt != null) {
	            	tt.setText(o.getTitle());
	            }
	            if(bt != null){
	            	try {
	            		Date date = parseDateFormat.parse(o.getPublished());
	            		bt.setText(displayDateFormat.format(date));
	            	} catch (Exception e) {
	            		Log.e(DEBUG_TAG, "Error parsing date", e);
	            	}
	            }
	        }
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
		public TextView bt;
	}	
}
