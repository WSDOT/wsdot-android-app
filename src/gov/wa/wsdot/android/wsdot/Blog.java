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

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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

public class Blog extends ListActivity {
	private static final String DEBUG_TAG = "Blog";
	private ArrayList<BlogItem> blogItems = null;
	private BlogItemAdapter adapter;
	DateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz"); //e.g. 2010-08-20T10:11:00.000-07:00
	DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((TextView)findViewById(R.id.sub_section)).setText("Blog");
        blogItems = new ArrayList<BlogItem>();
        this.adapter = new BlogItemAdapter(this, R.layout.news_item, blogItems);
        setListAdapter(this.adapter);
        new GetBlogItems().execute();
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Bundle b = new Bundle();
		Intent intent = new Intent(this, BlogItemDetails.class);
		b.putString("heading", blogItems.get(position).getTitle());
		b.putString("content", blogItems.get(position).getContent());
		intent.putExtras(b);
		startActivity(intent);
	}

	private class GetBlogItems extends AsyncTask<String, Integer, String> {
		private final ProgressDialog dialog = new ProgressDialog(Blog.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving blog stories ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setMax(25);
			this.dialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                cancel(true);
	            }
			});
	        this.dialog.show();
		}
		
	    protected void onCancelled() {
	        Toast.makeText(Blog.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		@Override
		protected String doInBackground(String... params) {
			try {
				URL text = new URL("http://feeds.feedburner.com/feedburner/ygdt");
				
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				
				parser.setInput(text.openStream(), null);
				int parserEvent = parser.getEventType();
				String tag;
				String title;
				String published;
				String link;
				String content;
				boolean inTitle = false;
				boolean inPublished = false;
				boolean inLink = false;
				boolean inContent = false;
				boolean inEntry = false;
				blogItems = new ArrayList<BlogItem>();
				BlogItem i = null;
				
				while (parserEvent != XmlPullParser.END_DOCUMENT) {
					if (!this.isCancelled()) {
						switch(parserEvent) {
						case XmlPullParser.TEXT:
							if (inEntry & inTitle) {
								title = parser.getText();
								i.setTitle(title);
							}
							if (inEntry & inPublished) {
								published = parser.getText();
								i.setPublished(published);
							}
							if (inEntry & inLink) {
								link = parser.getText();
								i.setLink(link);
							}
							if (inEntry & inContent) {
								content = parser.getText();
								i.setContent(content);
							}
							break;
						case XmlPullParser.END_TAG:
							tag = parser.getName();
							if (tag.compareTo("entry") == 0) {
								inEntry = false;
								blogItems.add(i);
								publishProgress(1);
							}
							if (tag.compareTo("title") == 0) {
								inTitle = false;
							}
							if (tag.compareTo("published") == 0) {
								inPublished = false;
							}
							if (tag.compareTo("link") == 0) {
								inLink = false;
							}
							if (tag.compareTo("content") == 0) {
								inContent = false;
							}
							break;
						case XmlPullParser.START_TAG:
							tag = parser.getName();
							if (tag.compareTo("entry") == 0) {
								inEntry = true;
								i = new BlogItem();
							}
							if (tag.compareTo("title") == 0) {
								inTitle = true;
							}
							if (tag.compareTo("published") == 0) {
								inPublished = true;
							}
							if (tag.compareTo("link") == 0) {
								inLink = true;
							}					
							if (tag.compareTo("content") == 0) {
								inContent = true;
							}
							break;
						}
						parserEvent = parser.next();
					} else {
						break;
					}
				}
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			return null;
		}		
		
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
            if(blogItems != null && blogItems.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<blogItems.size();i++)
                adapter.add(blogItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}
	}   
	
	private class BlogItemAdapter extends ArrayAdapter<BlogItem> {
        private ArrayList<BlogItem> items;

        public BlogItemAdapter(Context context, int textViewResourceId, ArrayList<BlogItem> items) {
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
	        BlogItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(R.id.toptext);
	            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
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
	        return v;
        }
	}
}
