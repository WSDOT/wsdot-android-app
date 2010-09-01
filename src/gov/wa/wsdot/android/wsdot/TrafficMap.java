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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public abstract class TrafficMap extends MapActivity {
	
	private static final String DEBUG_TAG = "TrafficMap";
	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private HashMap<Integer, String[]> eventCategories = new HashMap<Integer, String[]>();
	protected MapView map = null;
	private AlertsOverlay alerts = null;
	private CamerasOverlay cameras = null;
	boolean showCameras;
	boolean showShadows;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup the unique latitude, longitude and zoom level
        prepareMap();
        
        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true); 
        showShadows = settings.getBoolean("KEY_SHOW_MARKER_SHADOWS", true);
        buildEventCategories();       

        new OverlayTask().execute();
    }
	
	abstract void prepareMap();

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}	
	
	private void buildEventCategories() {
		String[] event_construction = {"construction"};
		String[] event_closure = {"closure"};
		
		eventCategories.put(R.drawable.closed, event_closure);
		eventCategories.put(R.drawable.construction_high, event_construction);
	}

	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1E6), (int)(lon*1E6)));
	 }	
	
	private class AlertsOverlay extends ItemizedOverlay<AlertItem> {
		private List<AlertItem> alertItems = new ArrayList<AlertItem>();

		public AlertsOverlay() {
			super(null);			
			
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/HighwayAlerts.js.gz");
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
				JSONObject result = obj.getJSONObject("alerts");
				JSONArray items = result.getJSONArray("items");

				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					JSONObject startRoadwayLocation = item.getJSONObject("StartRoadwayLocation");
					
					alertItems.add(new AlertItem(getPoint(startRoadwayLocation.getDouble("Latitude"), startRoadwayLocation.getDouble("Longitude")),
							"",
							item.getString("HeadlineDescription"),
							getMarker(getCategoryIcon(eventCategories, item.getString("EventCategory")))));
				}
				 
			 } catch (Exception e) {
				 Log.e(DEBUG_TAG, "Error in network call", e);
			 }			 
			 
			 populate();
		}
		
		@Override
		protected AlertItem createItem(int i) {
			return(alertItems.get(i));
		}
		
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			if (!showShadows) {
				shadow = false;
			}
			super.draw(canvas, mapView, shadow);
		}

		@Override
		protected boolean onTap(int i) {
			OverlayItem item = getItem(i);
			AlertDialog.Builder dialog = new AlertDialog.Builder(TrafficMap.this);
			dialog.setMessage(item.getSnippet());  
			dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
		
			dialog.show();
			return true;
		} 
		 
		 @Override
		 public int size() {
			 return(alertItems.size());
		 }
		 
		 private Drawable getMarker(int resource) {
			 Drawable marker = getResources().getDrawable(resource);
			 marker.setBounds(0, 0, marker.getIntrinsicWidth(),
			 marker.getIntrinsicHeight());
			 boundCenterBottom(marker);

			 return(marker);
		 }
	}
	
	private class CamerasOverlay extends ItemizedOverlay<CameraItem> {
		private List<CameraItem> cameraItems = new ArrayList<CameraItem>();

		public CamerasOverlay() {
			super(null);	
			
			try {
				InputStream is = getResources().openRawResource(R.raw.cameras);
				byte [] buffer = new byte[is.available()];
				while (is.read(buffer) != -1);
				
				String jsonFile = new String(buffer);
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("cameras");
				JSONArray items = result.getJSONArray("items");

				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					
					cameraItems.add(new CameraItem(getPoint(item.getDouble("lat"), item.getDouble("lon")),
							item.getString("title"),
							item.getString("url"),
							getMarker(R.drawable.camera)));
				}
				 
			 } catch (Exception e) {
				 Log.e(DEBUG_TAG, "Error in network call", e);
			 }			 
			 
			 populate();
		}
		
		@Override
		protected CameraItem createItem(int i) {
			return(cameraItems.get(i));
		}
		
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			if (!showShadows) {
				shadow = false;
			}
			super.draw(canvas, mapView, shadow);
		}

		@Override
		protected boolean onTap(int i) {
			OverlayItem item = getItem(i);
			new GetCameraImage().execute(item.getSnippet());

			return true;
		} 
		 
		 @Override
		 public int size() {
			 return(cameraItems.size());
		 }
		 
		 private Drawable getMarker(int resource) {
			 Drawable marker = getResources().getDrawable(resource);
			 marker.setBounds(0, 0, marker.getIntrinsicWidth(),
			 marker.getIntrinsicHeight());
			 boundCenterBottom(marker);

			 return(marker);
		 }
	}	
	
	class AlertItem extends OverlayItem {
		 Drawable marker = null;
	
		 AlertItem(GeoPoint pt, String title, String description, Drawable marker) {
			 super(pt, title, description);
			 this.marker = marker;
		 }

		 @Override
		 public Drawable getMarker(int stateBitset) {
			 Drawable result = marker;
			 setState(result, stateBitset);

			 return result;
		 }
	}
	
	class CameraItem extends OverlayItem {
		 Drawable marker = null;
	
		 CameraItem(GeoPoint pt, String title, String description, Drawable marker) {
			 super(pt, title, description);
			 this.marker = marker;
		 }

		 @Override
		 public Drawable getMarker(int stateBitset) {
			 Drawable result = marker;
			 setState(result, stateBitset);

			 return result;
		 }
	}	
	
	class OverlayTask extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(TrafficMap.this);
		
		@Override
		public void onPreExecute() {
			if (alerts != null) {
				map.getOverlays().remove(alerts);
				map.invalidate();
				alerts = null;
			}
			if (cameras != null) {
				map.getOverlays().remove(cameras);
				map.invalidate();
				cameras = null;
			}
			
			if (showCameras) {
				this.dialog.setMessage("Retrieving latest traffic alerts and camera locations ...");	
			} else {
				this.dialog.setMessage("Retrieving latest traffic alerts ...");
			}
			
			this.dialog.show();
		 }

		 @Override
		 public Void doInBackground(Void... unused) {
			 alerts = new AlertsOverlay();
			 if (showCameras) {
				 cameras = new CamerasOverlay();	 
			 }
			 
			 return null;
		 }

		 @Override
		 public void onPostExecute(Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			map.getOverlays().add(alerts);
			if (showCameras) {
				map.getOverlays().add(cameras);	
			}
			
			map.invalidate();
		 }
	}	
	
	private class GetCameraImage extends AsyncTask<String, Void, Drawable> {
		private final ProgressDialog dialog = new ProgressDialog(TrafficMap.this);

		protected void onPreExecute() {
			this.dialog.setMessage("Retrieving camera image ...");
			this.dialog.show();
		}
		
		protected Drawable doInBackground(String... params) {
			return loadImageFromNetwork(params[0]);
		}
		
		protected void onPostExecute(Drawable result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(TrafficMap.this);
			LayoutInflater inflater = (LayoutInflater) TrafficMap.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.camera_dialog, null);
			ImageView image = (ImageView) layout.findViewById(R.id.image);
			
			if (image.equals(null)) {
				image.setImageResource(R.drawable.camera_offline);
			} else {
				image.setImageDrawable(result);				
			}	

			builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});

			builder.setView(layout);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();			
		}
	}
	
    private Drawable loadImageFromNetwork(String url) {
    	BufferedInputStream in;
        BufferedOutputStream out;  
        
        try {
            in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
            copy(in, out);
            out.flush();
            final byte[] data = dataStream.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);                        
            final Drawable image = new BitmapDrawable(bitmap);
            return image;
	    } catch (Exception e) {
	        Log.e(DEBUG_TAG, "Error retrieving camera images", e);
	    }
	    return null;	    
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
	
	@SuppressWarnings("unchecked")
	private static Integer getCategoryIcon(HashMap<Integer, String[]> eventCategories, String category) {
		Integer image = R.drawable.alert_highest;
		Set set = eventCategories.entrySet();
		Iterator i = set.iterator();
		
		if (category.equals("")) return image;
		
		while(i.hasNext()) {
			Map.Entry me = (Map.Entry)i.next();
			for (String phrase: (String[])me.getValue()) {
				String patternStr = phrase;
				Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(category);
				boolean matchFound = matcher.find();
				if (matchFound) {
					image = (Integer)me.getKey();
				}
			}
		}	
		return image;
	}
}
