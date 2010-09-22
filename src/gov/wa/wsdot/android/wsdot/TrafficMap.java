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
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class TrafficMap extends MapActivity {
	
	private static final String DEBUG_TAG = "TrafficMap";
	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private HashMap<Integer, String[]> eventCategories = new HashMap<Integer, String[]>();
	protected MapView map = null;
	protected MapController mapController = null;
	private AlertsOverlay alerts = null;
	private CamerasOverlay cameras = null;
	boolean showCameras;
	boolean showShadows;
	private MyLocationOverlay myLocationOverlay;
	private ArrayList<LatLonItem> seattleArea = new ArrayList<LatLonItem>();
	
	static final private int MENU_ITEM_SEATTLE_ALERTS = Menu.FIRST;
	static final private int MENU_ITEM_TRAVEL_TIMES = Menu.FIRST + 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup the unique latitude, longitude and zoom level
        prepareMap();
        prepareBoundingBox();
        
		myLocationOverlay = new MyLocationOverlay(this, map);
		map.getOverlays().add(myLocationOverlay);
		
		// Will be executed as soon as we have a location fix
        myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
            	map.getController().animateTo(myLocationOverlay.getMyLocation());
            }
        });
        
        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true); 
        showShadows = settings.getBoolean("KEY_SHOW_MARKER_SHADOWS", true);
        buildEventCategories();       

        new OverlayTask().execute();
    }
	
	public void prepareBoundingBox() {
		seattleArea.add(new LatLonItem(48.01749, -122.46185));
		seattleArea.add(new LatLonItem(48.01565, -121.86584));
		seattleArea.add(new LatLonItem(47.27737, -121.86310));
		seattleArea.add(new LatLonItem(47.28109, -122.45911));
	}
	
	public void prepareMap() {
		setContentView(R.layout.map);
		((TextView)findViewById(R.id.sub_section)).setText("Traffic Near You");	
        map = (MapView) findViewById(R.id.mapview);
        map.setSatellite(false);
        map.setBuiltInZoomControls(true);
        map.setTraffic(true);
        map.getController().setZoom(13);
	}

	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();

	}

	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		GeoPoint p = map.getMapCenter();
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.traffic_menu, menu);

	    /**
	     * Check if current location is within a lat/lon bounding box surrounding
	     * the greater Seattle area.
	     */
		if (inPolygon(seattleArea, p.getLatitudeE6(), p.getLongitudeE6())) {
			menu.add(0, MENU_ITEM_SEATTLE_ALERTS, menu.size(), "Seattle Alerts").setIcon(R.drawable.ic_menu_notifications);
		    menu.add(0, MENU_ITEM_TRAVEL_TIMES, menu.size(), "Travel Times").setIcon(R.drawable.ic_menu_recent_history);
		}
	    
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {

	    case R.id.my_location:
	    	((TextView)findViewById(R.id.sub_section)).setText("Traffic Near You");	
	    	map.getController().setCenter(myLocationOverlay.getMyLocation());
	        return true;
	    case R.id.goto_bellingham:
	    	goToLocation("Bellingham Traffic", 48.756302,-122.46151, 12);
	    	return true;	        
	    case R.id.goto_chehalis:
	    	goToLocation("Chelalis Traffic", 46.635529, -122.937698, 13);
	    	return true;
	    case R.id.goto_hoodcanal:
	    	goToLocation("Hood Canal Traffic", 47.85268,-122.628365, 13);
	    	return true;
	    case R.id.goto_mtvernon:
	    	goToLocation("Mt Vernon Traffic", 48.420657,-122.334824, 13);
	    	return true;
	    case R.id.goto_stanwood:
	    	goToLocation("Stanwood Traffic", 48.22959, -122.34581, 13);
	    	return true;
	    case R.id.goto_monroe:
	    	goToLocation("Monroe Traffic", 47.859476, -121.972446, 14);
	    	return true;
	    case R.id.goto_sultan:
	    	goToLocation("Sultan Traffic", 47.86034, -121.812286, 14);
	    	return true;
	    case R.id.goto_olympia:
	    	goToLocation("Olympia Traffic", 47.021461, -122.899933, 13);
	        return true;	    	    	
	    case R.id.goto_seattle:
	    	goToLocation("Seattle Area Traffic", 47.5990, -122.3350, 12);
	        return true;
	    case R.id.goto_spokane:
	    	goToLocation("Spokane Area Traffic", 47.658566, -117.425995, 12);
	        return true;	        
	    case R.id.goto_tacoma:
	    	goToLocation("Tacoma Traffic", 47.206275, -122.46254, 12);
	        return true;	        
	    case R.id.goto_vancouver:
	    	goToLocation("Vancouver Area Traffic", 45.639968, -122.610512, 12);
	        return true;
	    case R.id.goto_wenatchee:
	    	goToLocation("Wenatchee Traffic", 47.435867, -120.309563, 13);
	        return true;	        
	    //case R.id.my_places:
	    //	myPlaces();
	    //	return true;
	    case MENU_ITEM_SEATTLE_ALERTS:
	    	Intent alertsIntent = new Intent(this, SeattleTrafficAlerts.class);
	    	startActivity(alertsIntent);
	    	return true;
	    case MENU_ITEM_TRAVEL_TIMES:
	    	Intent timesIntent = new Intent(this, SeattleTrafficTravelTimes.class);
	    	startActivity(timesIntent);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void goToLocation(String title, double latitude, double longitude, int zoomLevel) {	
        GeoPoint newPoint = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        map.getController().setZoom(zoomLevel);
        map.getController().setCenter(newPoint);
        ((TextView)findViewById(R.id.sub_section)).setText(title);
	}

	/**
	 * Iterate through collection of LatLon objects in arrayList and see
	 * if passed latitude and longitude point is within the collection.
	 */	
	public boolean inPolygon(ArrayList<LatLonItem> points, int latitude, int longitude) {	
		int j = points.size() - 1;
		double lat = (double)(latitude / 1E6);
		double lon = (double)(longitude / 1E6);		
		boolean inPoly = false;
		
		for (int i = 0; i < points.size(); i++) {
			if ( (points.get(i).getLongitude() < lon && points.get(j).getLongitude() >= lon) || 
					(points.get(j).getLongitude() < lon && points.get(i).getLongitude() >= lon) ) {
						if ( points.get(i).getLatitude() + (lon - points.get(i).getLongitude()) / 
								(points.get(j).getLongitude() - points.get(i).getLongitude()) * 
									(points.get(j).getLatitude() - points.get(i).getLatitude()) < lat ) {
										inPoly = !inPoly;
						}
			}
			j = i;
		}
		return inPoly;
	}	
	
	/*
	public void myPlaces() {
		final CharSequence[] items = {"Seattle", "Tacoma", "Wenatchee"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("My Places");

		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
			}
		});
	
		AlertDialog alert = builder.create();
		alert.show();		
	}
	*/	
	
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
				/**
				 * Rather than reading a local static file, lets try reading
				 * a compressed file and perhaps caching it instead.
				 * 
				 * InputStream is = getResources().openRawResource(R.raw.cameras);
				 * byte [] buffer = new byte[is.available()];
				 * while (is.read(buffer) != -1);
				 *
				 * String jsonFile = new String(buffer);
				*/
				URL url = new URL("http://data.wsdot.wa.gov/mobile/Cameras.js.gz");
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
