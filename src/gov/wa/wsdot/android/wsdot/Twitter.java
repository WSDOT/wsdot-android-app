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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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

public class Twitter extends ListActivity {
	private static final String DEBUG_TAG = "Twitter";
	private ArrayList<TwitterItem> twitterItems = null;
	private TwitterItemAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter);
        twitterItems = new ArrayList<TwitterItem>();
        this.adapter = new TwitterItemAdapter(this, R.layout.row, twitterItems);
        ((TextView)findViewById(R.id.sub_section)).setText("Tweets");
        setListAdapter(this.adapter);
        new GetTwitterItems().execute();
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(this, TwitterItemDetails.class);
		b.putString("description", twitterItems.get(position).getDescription());
		b.putString("link", twitterItems.get(position).getLink());
		intent.putExtras(b);
		startActivity(intent);
	}
	   
    private class GetTwitterItems extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(Twitter.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving latest tweets ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setCancelable(true);
	        this.dialog.setMax(20);
	        this.dialog.show();
		}
    	
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
	    	String patternStr = "(http://[A-Za-z0-9./]+)"; // Find bit.ly addresses
	    	Pattern pattern = Pattern.compile(patternStr);
	    	
			try {
				URL text = new URL("http://twitter.com/statuses/user_timeline/14124059.rss");
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(text.openStream(), null);
				int parserEvent = parser.getEventType();
				String tag;
				String title;
				String tmpTitle;
				String pubDate;
				String link;
				String description;
				String tmpDescription;
				boolean inTitle = false;
				boolean inPubDate = false;
				boolean inLink = false;
				boolean inDescription = false;
				boolean inItem = false;
				twitterItems = new ArrayList<TwitterItem>();
				TwitterItem i = null;
				
				while (parserEvent != XmlPullParser.END_DOCUMENT) {
					switch(parserEvent) {
					case XmlPullParser.TEXT:
						if (inItem & inTitle) {
							tmpTitle = parser.getText();
							title = tmpTitle.replace("wsdot: ", "");
							i.setTitle(title);
						}
						if (inItem & inPubDate) {
							pubDate = parser.getText();
							i.setPubDate(pubDate);
						}
						if (inItem & inLink) {
							link = parser.getText();
							i.setLink(link);
						}
						if (inItem & inDescription) {
							tmpDescription = parser.getText();
							description = tmpDescription.replace("wsdot: ", "");
		                	Matcher matcher = pattern.matcher(description);
		                	boolean matchFound = matcher.find();
		                	if (matchFound) {
		                		String textLink = matcher.group();
		                		String hyperLink = "<a href=\"" + textLink + "\">" + textLink + "</a>";
		                		description = matcher.replaceFirst(hyperLink);
		                	}
							i.setDescription(description);
						}
						break;
					case XmlPullParser.END_TAG:
						tag = parser.getName();
						if (tag.compareTo("item") == 0) {
							inItem = false;
							twitterItems.add(i);
							publishProgress(1);
						}
						if (tag.compareTo("title") == 0) {
							inTitle = false;
						}
						if (tag.compareTo("pubDate") == 0) {
							inPubDate = false;
						}
						if (tag.compareTo("link") == 0) {
							inLink = false;
						}
						if (tag.compareTo("description") == 0) {
							inDescription = false;
						}
						break;
					case XmlPullParser.START_TAG:
						tag = parser.getName();
						if (tag.compareTo("item") == 0) {
							inItem = true;
							i = new TwitterItem();
						}
						if (tag.compareTo("title") == 0) {
							inTitle = true;
						}
						if (tag.compareTo("pubDate") == 0) {
							inPubDate = true;
						}
						if (tag.compareTo("link") == 0) {
							inLink = true;
						}					
						if (tag.compareTo("description") == 0) {
							inDescription = true;
						}
						break;
					}
					parserEvent = parser.next();
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
                if(bt != null){
                      bt.setText(o.getPubDate());
                }
	        }
	        return v;
        }
	}
}