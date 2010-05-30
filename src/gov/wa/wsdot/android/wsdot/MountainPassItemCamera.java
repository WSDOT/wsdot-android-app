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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class MountainPassItemCamera extends Activity {
	
	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private static final String DEBUG_TAG = "MountainPassConditionsPhotos";
    ArrayList<String> remoteImages = new ArrayList<String>();
    ArrayList<Drawable> bitmapImages = new ArrayList<Drawable>();
	
    @Override    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);
        Bundle b = getIntent().getExtras();
        remoteImages = b.getStringArrayList("Cameras");
        new GetCameraImages().execute();
    }

    private class GetCameraImages extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(MountainPassItemCamera.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving camera images ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setCancelable(true);
	        this.dialog.setMax(remoteImages.size());
	        this.dialog.show();
		}
    	
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
		   	BufferedInputStream in;
	        BufferedOutputStream out;      
	        
	    	for (int i=0; i < remoteImages.size(); i++) {   		
	    		final Drawable image;
	    		Bitmap bitmap = null;
	            try {
	                in = new BufferedInputStream(new URL(remoteImages.get(i)).openStream(), IO_BUFFER_SIZE);
	                final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
	                out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
	                copy(in, out);
	                out.flush();
	                final byte[] data = dataStream.toByteArray();
	                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
	    	    } catch (Exception e) {
	    	        Log.e(DEBUG_TAG, "Error retrieving camera images", e);
	    	        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_offline);
	    	    } finally {
	    	    	image = new BitmapDrawable(bitmap);
	    	    	bitmapImages.add(image);
	    	    	publishProgress(1);
	    	    }
	    	}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			populateGallery();
		}   
    }  

    private void populateGallery() {
    	try {
            Gallery gallery = (Gallery) findViewById(R.id.gallery);
            gallery.setAdapter(new ImageAdapter(this));
    	} catch (Exception e) {
            Log.e("DEBUG_TAG", "Error getting images", e);
        }	        
    }    
    
    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private int itemBackground;
 
        public ImageAdapter(Context c) {
            context = c;
            TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
            itemBackground = a.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 0);
            a.recycle();                    
        }
 
        public int getCount() {
            return remoteImages.size();
        }
 
        public Object getItem(int position) {
            return position;
        }            
 
        public long getItemId(int position) {
            return position;
        }
 
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(bitmapImages.get(position));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new Gallery.LayoutParams(290, 270));
            imageView.setBackgroundResource(itemBackground);
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
