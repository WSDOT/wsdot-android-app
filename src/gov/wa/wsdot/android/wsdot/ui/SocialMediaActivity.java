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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

public class SocialMediaActivity extends SherlockListActivity {

	private ArrayList<ListViewItem> listViewItems;
	private ListViewArrayAdapter mAdapter;
	private View mLoadingSpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AnalyticsUtils.getInstance(this).trackPageView("/News & Social Media");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.fragment_list_with_spinner);
        mLoadingSpinner = findViewById(R.id.loading_spinner);        
       
		listViewItems = new ArrayList<ListViewItem>();
        mAdapter = new ListViewArrayAdapter(this, R.layout.list_item_with_icon, listViewItems);
        setListAdapter(mAdapter);
        
        AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        ListView listView = getListView();        
        listView.setLayoutAnimation(controller);        
        
        new BuildMenuAsyncTask().execute();		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
	    	finish();
	    	return true;		
		}
		
		return super.onOptionsItemSelected(item);
	}	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, (Class<?>) listViewItems.get(position).getClz());
		startActivity(intent);
	}

	private class BuildMenuAsyncTask extends AsyncTask<String, Integer, String> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... arg0) {
			listViewItems = new ArrayList<ListViewItem>();
			
	        listViewItems.add(new ListViewItem("Blogger", BlogActivity.class, R.drawable.ic_list_blogger));
	        listViewItems.add(new ListViewItem("Facebook", FacebookActivity.class, R.drawable.ic_list_facebook));
	        listViewItems.add(new ListViewItem("Flickr", FlickrActivity.class, R.drawable.ic_list_flickr));
	        listViewItems.add(new ListViewItem("News", NewsActivity.class, R.drawable.ic_list_wsdot));
	        listViewItems.add(new ListViewItem("Twitter", TwitterActivity.class, R.drawable.ic_list_twitter));
	        listViewItems.add(new ListViewItem("YouTube", YouTubeActivity.class, R.drawable.ic_list_youtube));
	        
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mLoadingSpinner.setVisibility(View.GONE);
			
            if(listViewItems != null && listViewItems.size() > 0){
                mAdapter.notifyDataSetChanged();
                for(int i=0;i<listViewItems.size();i++) {
                	mAdapter.add(listViewItems.get(i));
                }
            }
            mAdapter.notifyDataSetChanged();
		}
		
	}
	
	private class ListViewArrayAdapter extends ArrayAdapter<ListViewItem> {
		private ArrayList<ListViewItem> items;
		private Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		
		public ListViewArrayAdapter(Context context, int detailsItem, ArrayList<ListViewItem> items) {
			super(context, detailsItem, items);
			this.items = items;
		}
		
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = getLayoutInflater().inflate(R.layout.list_item_with_icon, null);
	        }
	        
	        ListViewItem item = items.get(position);
	        
	        if (item != null) {
	            TextView tt = (TextView) convertView.findViewById(R.id.title);
	            tt.setTypeface(tf);
	            ImageView iv = (ImageView) convertView.findViewById(R.id.icon);
	            
	            if (tt != null) {
	            	tt.setText(item.getTitle());
	            }
	            
	       		iv.setImageResource(item.getIcon());
	        }
	        
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
		public ImageView iv;
	}		
	
	public class ListViewItem {
		private String title;
		private Object clz;
		private int icon;
		
		public ListViewItem(String title, Class<?> clz, int icon) {
			this.title = title;
			this.clz = clz;
			this.icon = icon;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public Object getClz() {
			return clz;
		}
		public void setClz(Object clz) {
			this.clz = clz;
		}
		public int getIcon() {
			return icon;
		}
		public void setIcon(int icon) {
			this.icon = icon;
		}
		
	}
}
