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
import gov.wa.wsdot.android.wsdot.ui.widget.MyMapView;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import gov.wa.wsdot.android.wsdot.util.FixedMyLocationOverlay;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class VesselWatchMapActivity extends SherlockMapActivity {

	private static final String DEBUG_TAG = "VesselWatchMap";
	private MyMapView map = null;
	private Handler handler = new Handler();
	private Timer timer;
	private boolean firstRun = true;
	private FixedMyLocationOverlay myLocationOverlay;
	private VesselsOverlay vessels = null;
	private CamerasOverlay cameras = null;
	boolean showCameras;
	boolean showShadows;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Setup the unique latitude, longitude and zoom level
        prepareMap();

        /**
         * Using an extended version of MyLocationOverlay class because it has been
         * reported the Motorola Droid X phones throw an exception when they try to
         * draw the dot showing the location of the device.
         * 
         * See this post titled, "Android applications that use the MyLocationOverlay
         * class crash on the new Droid X"
         * 
         * http://dimitar.me/applications-that-use-the-mylocationoverlay-class-crash-on-the-new-droid-x/
         */
		myLocationOverlay = new FixedMyLocationOverlay(this, map);
		map.getOverlays().add(myLocationOverlay);		

        // Check preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        showCameras = settings.getBoolean("KEY_SHOW_CAMERAS", true); 
        showShadows = settings.getBoolean("KEY_SHOW_MARKER_SHADOWS", true);
    }
	
	public void prepareMap() {
		setContentView(R.layout.map);
		setSupportProgressBarIndeterminateVisibility(false);
		
		Double latitude = 47.565125;
        Double longitude = -122.480508;
        map = (MyMapView) findViewById(R.id.mapview);
        map.setSatellite(false);
        map.getController().setZoom(11);
        map.setBuiltInZoomControls(true);
        map.setTraffic(false);
        GeoPoint newPoint = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        map.getController().animateTo(newPoint);		
	}

	@Override
	protected void onPause() {
		super.onPause();
		timer.cancel();
		myLocationOverlay.disableMyLocation();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		timer = new Timer();
		timer.schedule(new MyTimerTask(), 0, 30000); // Schedule vessels to update every 30 seconds
		myLocationOverlay.enableMyLocation();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
	    getSupportMenuInflater().inflate(R.menu.vessel_watch, menu);

	    if (showCameras) {
	    	menu.getItem(1).setTitle("Hide Cameras");
	    } else {
	    	menu.getItem(1).setTitle("Show Cameras");
	    }	    
	    
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {

	    case android.R.id.home:
	    	finish();
	    	return true;	    
	    case R.id.my_location:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/My Location");
	        myLocationOverlay.runOnFirstFix(new Runnable() {
	            public void run() {	    	
	            	map.getController().animateTo(myLocationOverlay.getMyLocation());
	            }
	        });
	        return true;
	    case R.id.goto_anacortes:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Anacortes");
	    	goToLocation(48.535868, -123.013808, 10);
	    	return true;
	    case R.id.goto_edmonds:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Edmonds");
	    	goToLocation(47.803096, -122.438718, 12);
	    	return true;
	    case R.id.goto_fauntleroy:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Fauntleroy");
	    	goToLocation(47.513625, -122.450820, 13);
	    	return true;
	    case R.id.goto_mukilteo:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Mukilteo");
	    	goToLocation(47.963857, -122.327721, 13);
	    	return true;
	    case R.id.goto_pointdefiance:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Pt Defiance");
	    	goToLocation(47.319040, -122.510890, 13);
	    	return true;
	    case R.id.goto_porttownsend:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Port Townsend");
	    	goToLocation(48.135562, -122.714449, 12);
	    	return true;
	    case R.id.goto_sanjuanislands:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/San Juan Islands");
	    	goToLocation(48.557233, -122.897078, 12);
	    	return true;
	    case R.id.goto_seattle:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Seattle");
	    	goToLocation(47.565125, -122.480508, 11);
	    	return true;
	    case R.id.goto_seattlebainbridge:
	    	AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/GoTo Location/Seattle-Bainbridge");
	    	goToLocation(47.600325, -122.437249, 12);
	    	return true;
	    case R.id.toggle_cameras:
	    	toggleCameras(item);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	private void toggleCameras(MenuItem item) {
		if (showCameras) {
			AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/Hide Cameras");
			map.getOverlays().remove(cameras);
			map.invalidate();
			item.setTitle("Show Cameras");
			showCameras = false;
		} else {
			AnalyticsUtils.getInstance(this).trackPageView("/Ferries/Vessel Watch/Show Cameras");
			map.getOverlays().add(cameras);
			map.invalidate();
			item.setTitle("Hide Cameras");
			showCameras = true;
		}		

		// Save camera display preference
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("KEY_SHOW_CAMERAS", showCameras);
		editor.commit();
	}	
	
	public void goToLocation(double latitude, double longitude, int zoomLevel) {	
        GeoPoint newPoint = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
        map.getController().setZoom(zoomLevel);
        map.getController().setCenter(newPoint);
	}	
	
    public class MyTimerTask extends TimerTask {
        private Runnable runnable = new Runnable() {
            public void run() {
                new OverlayTask().execute();
            }
        };

        public void run() {
            handler.post(runnable);
        }
    }
	
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1E6), (int)(lon*1E6)));
	 }
    
	private class VesselsOverlay extends ItemizedOverlay<VesselItem> {
		private List<VesselItem> vesselItems = new ArrayList<VesselItem>();
		private HashMap<Integer, Integer> ferryIcons = new HashMap<Integer, Integer>();
		
		public VesselsOverlay() {
			super(null);

			ferryIcons.put(0, R.drawable.ferry_0);
			ferryIcons.put(30, R.drawable.ferry_30);
			ferryIcons.put(60, R.drawable.ferry_60);
			ferryIcons.put(90, R.drawable.ferry_90);
			ferryIcons.put(120, R.drawable.ferry_120);
			ferryIcons.put(150, R.drawable.ferry_150);
			ferryIcons.put(180, R.drawable.ferry_180);
			ferryIcons.put(210, R.drawable.ferry_210);
			ferryIcons.put(240, R.drawable.ferry_240);
			ferryIcons.put(270, R.drawable.ferry_270);
			ferryIcons.put(300, R.drawable.ferry_300);
			ferryIcons.put(330, R.drawable.ferry_330);
			ferryIcons.put(360, R.drawable.ferry_360);
						
			try {
				URL url = new URL("http://www.wsdot.wa.gov/ferries/vesselwatch/Vessels.ashx");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONArray items = obj.getJSONArray("vessellist");
				int ferryIcon;
				
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					if (item.getString("inservice").equalsIgnoreCase("false")) {
						continue;
					}
					
					int nearest = (item.getInt("head") + 30 / 2) / 30 * 30; // round heading to nearest 30 degrees
					ferryIcon = ferryIcons.get(nearest);
					String route = item.getString("route");
					String lastDock = item.getString("lastdock");
					String arrivingTerminal = item.getString("aterm");
					String leftDock = item.getString("leftdock");
					String actualDeparture = "";
					
					if (route.length() == 0) route = "Not available";
					if (lastDock.length() == 0) lastDock = "Not available";
					if (arrivingTerminal.length() == 0) arrivingTerminal = "Not available";
					if (leftDock.length() == 0) {
						actualDeparture = "--:--";
					} else {
						actualDeparture = leftDock + " " + item.getString("leftdockAMPM");
					}
					
					vesselItems.add(new VesselItem(getPoint(item.getDouble("lat"), item.getDouble("lon")),
							item.getString("name"),
							"<b>Route:</b> " + route
								+ "<br><b>Departing:</b> " + lastDock
								+ "<br><b>Arriving:</b> " + arrivingTerminal
								+ "<br><b>Scheduled Departure:</b> " + item.getString("nextdep") + " " + item.getString("nextdepAMPM")
								+ "<br><b>Actual Departure:</b> " + actualDeparture
								+ "<br><b>Estimated Arrival:</b> " + item.getString("eta") + " " + item.getString("etaAMPM")
								+ "<br><b>Heading:</b> "	+ Integer.toString(item.getInt("head")) + "\u00b0 " + item.getString("headtxt")
								+ "<br><b>Speed:</b> " + Double.toString(item.getDouble("speed")) + " knots"
								+ "<br><br><a href=\"http://www.wsdot.com/ferries/vesselwatch/VesselDetail.aspx?vessel_id="
									+ item.getInt("vesselID") + "\">" + item.getString("name") + " Web page</a>",
							getMarker(ferryIcon)));
				}
				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}

			populate();
		}

		@Override
		protected VesselItem createItem(int i) {
			return(vesselItems.get(i));
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
			Bundle b = new Bundle();
			Intent intent = new Intent(VesselWatchMapActivity.this, VesselWatchDetailsActivity.class);
			b.putString("title", item.getTitle());
			b.putString("description", item.getSnippet());
			intent.putExtras(b);
			startActivity(intent);
			
			return true;
		}
		
		 @Override
		 public int size() {
			 return(vesselItems.size());
		 }
		 
		 private Drawable getMarker(int resource) {
			 Drawable marker = getResources().getDrawable(resource);
			 marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
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
				URL url = new URL("http://data.wsdot.wa.gov/mobile/WSFCameras.js.gz");
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
				int video = 0;

				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					
					cameraItems.add(new CameraItem(getPoint(item.getDouble("lat"), item.getDouble("lon")),
							item.getString("title"),
							item.getString("url") + "," + video,
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
			Bundle b = new Bundle();
			Intent intent = new Intent(VesselWatchMapActivity.this, CameraActivity.class);
			b.putString("title", item.getTitle());
			b.putString("url", item.getSnippet());
			intent.putExtras(b);
			startActivity(intent);			

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
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	class VesselItem extends OverlayItem {
		 Drawable marker = null;
	
		 VesselItem(GeoPoint pt, String title, String description, Drawable marker) {
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
		private final ProgressDialog dialog = new ProgressDialog(VesselWatchMapActivity.this);
		
		@Override
		public void onPreExecute() {
			if (vessels != null) {
				map.getOverlays().remove(vessels);
				vessels = null;
			}
			
			if (firstRun) {
				if (cameras != null) {
					map.getOverlays().remove(cameras);
					map.invalidate();
					cameras = null;
				}
				
				this.dialog.setMessage("Retrieving ferry and camera locations ...");				
				this.dialog.setOnCancelListener(new OnCancelListener() {
		            public void onCancel(DialogInterface dialog) {
		                cancel(true);
		            }
				});
				
				this.dialog.show();
			} else {
				setSupportProgressBarIndeterminateVisibility(true);
			}
		 }

	    protected void onCancelled() {
	        Toast.makeText(VesselWatchMapActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		 @Override
		 public Void doInBackground(Void... unused) {
			 if (!this.isCancelled()) vessels = new VesselsOverlay();
			 if (firstRun) {
				 if (!this.isCancelled()) cameras = new CamerasOverlay();	 
			 }
			 return null;
		 }

		 @Override
		 public void onPostExecute(Void unused) {
			 if (firstRun) {
				if (this.dialog.isShowing()) {
					this.dialog.dismiss();
				}
			 } else {
				 setSupportProgressBarIndeterminateVisibility(false);
			 }

			map.getOverlays().add(vessels);
			
			if (showCameras) {
				if (firstRun) {
					map.getOverlays().add(cameras);
				} else {
					map.getOverlays().remove(cameras);
					map.getOverlays().add(cameras);
				}
			}

			if (firstRun) firstRun = false;
			map.invalidate();
		 }
	}
}
