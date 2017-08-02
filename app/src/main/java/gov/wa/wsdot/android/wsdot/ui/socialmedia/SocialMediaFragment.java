/*
 * Copyright (c) 2017 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.socialmedia;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.ui.socialmedia.blogger.BlogActivity;
import gov.wa.wsdot.android.wsdot.ui.socialmedia.facebook.FacebookActivity;
import gov.wa.wsdot.android.wsdot.ui.socialmedia.flickr.FlickrActivity;
import gov.wa.wsdot.android.wsdot.ui.socialmedia.news.NewsActivity;
import gov.wa.wsdot.android.wsdot.ui.socialmedia.twitter.TwitterActivity;
import gov.wa.wsdot.android.wsdot.ui.socialmedia.youtube.YouTubeActivity;

public class SocialMediaFragment extends BaseListFragment {

    private static final String TAG = SocialMediaFragment.class.getSimpleName();
    private ArrayList<ListViewItem> listViewItems;
    private ListViewArrayAdapter mAdapter;
    private View mLoadingSpinner;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Since we are building a simple navigation, just hide the spinner.
        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        mLoadingSpinner.setVisibility(View.GONE);
        
        disableAds(root);
        
        return root;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        listViewItems = new ArrayList<ListViewItem>();
        listViewItems.add(new ListViewItem("Blogger", BlogActivity.class, R.drawable.ic_list_blogger));
        listViewItems.add(new ListViewItem("Facebook", FacebookActivity.class, R.drawable.ic_list_facebook));
        listViewItems.add(new ListViewItem("Flickr", FlickrActivity.class, R.drawable.ic_list_flickr));
        listViewItems.add(new ListViewItem("News", NewsActivity.class, R.drawable.ic_list_rss));
        listViewItems.add(new ListViewItem("Twitter", TwitterActivity.class, R.drawable.ic_list_twitter));
        listViewItems.add(new ListViewItem("YouTube", YouTubeActivity.class, R.drawable.ic_list_youtube));
        
        mAdapter = new ListViewArrayAdapter(getActivity(), R.layout.list_item_with_icon, listViewItems);
        setListAdapter(mAdapter);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(getActivity(), (Class<?>) listViewItems.get(position).getClz());
        startActivity(intent);
    }
    
    private class ListViewArrayAdapter extends ArrayAdapter<ListViewItem> {
        private final LayoutInflater mInflater;
        private ArrayList<ListViewItem> items;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        
        public ListViewArrayAdapter(Context context, int detailsItem, ArrayList<ListViewItem> items) {
            super(context, detailsItem, items);
            this.items = items;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_with_icon, null);
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
