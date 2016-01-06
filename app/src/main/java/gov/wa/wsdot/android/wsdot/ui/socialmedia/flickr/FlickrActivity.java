/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.socialmedia.flickr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.FlickrItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.widget.SquareImageView;

public class FlickrActivity extends BaseActivity implements
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FlickrActivity.class.getSimpleName();
    private static final int IO_BUFFER_SIZE = 4 * 1024;
    private ArrayList<FlickrItem> mFlickrItems = null;
	private ImageAdapter adapter;
	private static SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar mToolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_flickr);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);

        this.adapter = new ImageAdapter(this);
        
        new GetRSSItems().execute();
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
    
    private class GetRSSItems extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			swipeRefreshLayout.post(new Runnable() {
				public void run() {
					swipeRefreshLayout.setRefreshing(true);
				}
			});
		}
    	
	    protected void onCancelled() {
	        Toast.makeText(FlickrActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }

		@Override
		protected String doInBackground(String... params) {
	    	BufferedInputStream ins;
	        BufferedOutputStream out;
	    	String content;
	    	String tmpContent;
	    	
	        try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/FlickrPhotos.js.gz");
				URLConnection urlConn = url.openConnection();
				
				BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
                GZIPInputStream gzin = new GZIPInputStream(bis);
                InputStreamReader is = new InputStreamReader(gzin);
                BufferedReader in = new BufferedReader(is);
				
				String jsonFile = "";
				String line;
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONArray items = obj.getJSONArray("items");
				mFlickrItems = new ArrayList<FlickrItem>();
				FlickrItem i = null;
				
				int numItems = items.length();
				for (int j=0; j < numItems; j++) {
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);
						i = new FlickrItem();
						i.setTitle(item.getString("title"));
						i.setLink(item.getString("link"));
						JSONObject media = item.getJSONObject("media");
	                    i.setMedia(media.getString("m"));
						i.setPublished(item.getString("published"));
						tmpContent = item.getString("description");
						content = tmpContent.replace(" <p><a href=\"https://www.flickr.com/people/wsdot/\">WSDOT</a> posted a photo:</p> ", "");
						i.setContent(content);
						
                		String imageSrc = i.getMedia();
                        ins = new BufferedInputStream(new URL(imageSrc).openStream(), IO_BUFFER_SIZE);
                        final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                        out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
                        copy(ins, out);
                        out.flush();
                        final byte[] data = dataStream.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);                        

                        @SuppressWarnings("deprecation")
						final Drawable image = new BitmapDrawable(bitmap);
                        i.setImage(image);
	                	
	                	mFlickrItems.add(i);
					} else {
						break;
					}
				}
	        } catch (Exception e) {
	            Log.e(TAG, "Error parsing Flickr JSON feed", e);
	        }
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
		    swipeRefreshLayout.setRefreshing(false);
			
			showImages();
		}   
    }
    
    private void showImages() {
    	try {
            GridView gridView = (GridView) findViewById(R.id.gridview);
            gridView.setAdapter(this.adapter);    
            gridView.setOnItemClickListener(new OnItemClickListener() {
            	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mFlickrItems.get(position).getLink()));
                    startActivity(intent);
	            }
	        });

    	} catch (Exception e) {
            Log.e("DEBUG_TAG", "Error getting images", e);
        }	        
    }  
    
    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater inflater;
      
        public ImageAdapter(Context context) {
        	inflater = LayoutInflater.from(context);
        }

        public int getCount() {
        	return mFlickrItems.size();
        }
        
        public Object getItem(int position) {
        	return mFlickrItems.get(position);
        }
        
        public long getItemId(int position) {
        	return 0;
        }
        
        public View getView(int position, View view, ViewGroup viewGroup) {
            SquareImageView picture;
            TextView name;

            if (view == null) {
                view = inflater.inflate(R.layout.gridview_item, viewGroup, false);
                view.setTag(R.id.picture, view.findViewById(R.id.picture));
                view.setTag(R.id.text, view.findViewById(R.id.text));
            }
            
            picture = (SquareImageView)view.getTag(R.id.picture);
            name = (TextView)view.getTag(R.id.text);

            FlickrItem item = (FlickrItem)getItem(position);
            picture.setImageDrawable(item.getImage());
            name.setText(mFlickrItems.get(position).getTitle());

            return view;
        }
    }
    
    /**
     * Copy the content of the input stream into the output stream, using a
     * temporary byte array buffer whose size is defined by
     * {@link #IO_BUFFER_SIZE}.
     * 
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     * @throws IOException If any error occurs during the copy.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    public void onRefresh() {
        mFlickrItems.clear();
        this.adapter.notifyDataSetChanged();
        new GetRSSItems().execute();        
    }    
}
