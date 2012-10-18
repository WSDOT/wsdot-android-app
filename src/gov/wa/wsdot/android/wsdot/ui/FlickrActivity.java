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
import gov.wa.wsdot.android.wsdot.shared.FlickrItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery.LayoutParams;
import android.widget.GridView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class FlickrActivity extends SherlockActivity {
	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private static final String DEBUG_TAG = "Photos";
    private ArrayList<FlickrItem> mFlickrItems = null;
	private ImageAdapter adapter;
	private View mLoadingSpinner;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/News & Social Media/Photos");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.activity_flickr);
        mLoadingSpinner = findViewById(R.id.loading_spinner);

        this.adapter = new ImageAdapter(this);
        new GetRSSItems().execute();
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
			mFlickrItems.clear();
			this.adapter.notifyDataSetChanged();
			new GetRSSItems().execute();
		}
		
		return super.onOptionsItemSelected(item);
	}    
    
    private class GetRSSItems extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}
    	
	    protected void onCancelled() {
	        Toast.makeText(FlickrActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }

		@Override
		protected String doInBackground(String... params) {
	    	String patternStr = "http://farm.*jpg";
	    	Pattern pattern = Pattern.compile(patternStr);
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
				
				for (int j=0; j < items.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);
						i = new FlickrItem();
						i.setTitle(item.getString("title"));
						i.setLink(item.getString("link"));
						i.setPublished(item.getString("published"));
						tmpContent = item.getString("description");
						content = tmpContent.replace("<p><a href=\"http://www.flickr.com/people/wsdot/\">WSDOT</a> posted a photo:</p>", "");
						i.setContent(content);
	                	Matcher matcher = pattern.matcher(content);
	                	boolean matchFound = matcher.find();
	
	                	if (matchFound) {
	                		String tmpString = matcher.group();
	                		String imageSrc = tmpString.replace("_m", "_s"); // We want the small 75x75 images
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
	                	}
	                	
	                	mFlickrItems.add(i);
						publishProgress(1);
					} else {
						break;
					}
				}
	        } catch (Exception e) {
	            Log.e(DEBUG_TAG, "Error parsing Flickr JSON feed", e);
	        }
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mLoadingSpinner.setVisibility(View.GONE);
			
			showImages();
		}   
    }
    
    private void showImages() {
    	try {
            GridView gridView = (GridView) findViewById(R.id.gridview);
            gridView.setAdapter(this.adapter);    
            gridView.setOnItemClickListener(new OnItemClickListener() {
            	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        		Bundle b = new Bundle();
	        		Intent intent = new Intent(FlickrActivity.this, FlickrDetailsActivity.class);
	        		b.putString("title", mFlickrItems.get(position).getTitle());
	        		b.putString("link", mFlickrItems.get(position).getLink());
	        		b.putString("content", mFlickrItems.get(position).getContent());
	        		intent.putExtras(b);
	        		startActivity(intent);
	            }
	        });

    	} catch (Exception e) {
            Log.e("DEBUG_TAG", "Error getting images", e);
        }	        
    }  
    
    @SuppressWarnings("deprecation")
	public View makeView() {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(0xFFFFFFFF);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setLayoutParams(new 
                ImageSwitcher.LayoutParams(
                        LayoutParams.FILL_PARENT,
                        LayoutParams.FILL_PARENT));
        return imageView;
    }
    
    public class ImageAdapter extends BaseAdapter {
        private Context context;
      
        public ImageAdapter(Context c) {
        	context = c;       	
        }

        public int getCount() {
        	return mFlickrItems.size();
        }
        
        public Object getItem(int position) {
        	return null;
        }
        
        public long getItemId(int position) {
        	return 0;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
            	imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(75, 75));
                imageView.setPadding(5, 5, 5, 5);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageDrawable(mFlickrItems.get(position).getImage());

            return imageView;
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
}
