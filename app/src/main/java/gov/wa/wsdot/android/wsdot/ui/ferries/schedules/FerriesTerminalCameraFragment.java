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

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.service.CamerasSyncService;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.widget.ResizeableImageView;
import gov.wa.wsdot.android.wsdot.util.ImageManager;

public class FerriesTerminalCameraFragment extends BaseListFragment
        implements LoaderCallbacks<Cursor> {

    private static Map<Integer, FerriesTerminalItem> ferriesTerminalMap = new HashMap<Integer, FerriesTerminalItem>();
    private static int mTerminalId;
    private CamerasSyncReceiver mCamerasReceiver;
    private Intent camerasIntent;
    private View mEmptyView;
    private static View mLoadingSpinner;
    private static CameraImageAdapter mAdapter;
    private static ArrayList<CameraItem> cameraItems = new ArrayList<CameraItem>();
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        Bundle args = activity.getIntent().getExtras();
        
        mTerminalId = args.getInt("terminalId");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
        
        createTerminalLocations();
        
        camerasIntent = new Intent(getActivity(), CamerasSyncService.class);
        getActivity().startService(camerasIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        IntentFilter camerasFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        camerasFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mCamerasReceiver = new CamerasSyncReceiver();
        getActivity().registerReceiver(mCamerasReceiver, camerasFilter); 
    }

    @Override
    public void onPause() {
        super.onPause();
        
        getActivity().unregisterReceiver(mCamerasReceiver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mAdapter = new CameraImageAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);
        
        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        mEmptyView = root.findViewById( R.id.empty_list_view );
        
        disableAds(root);
        
        return root;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
            Cameras.CAMERA_LATITUDE,
            Cameras.CAMERA_LONGITUDE,
            Cameras.CAMERA_TITLE,
            Cameras.CAMERA_URL,
            Cameras.CAMERA_HAS_VIDEO,
            Cameras.CAMERA_ID,
            Cameras.CAMERA_ROAD_NAME
            };
        
        Uri baseUri = Uri.withAppendedPath(Cameras.CONTENT_ROAD_NAME_URI, Uri.encode("Ferries"));
        
        CursorLoader cursorLoader = new CameraImagesLoader(getActivity(),
                baseUri,
                projection,
                null,
                null,
                null
                );
        
        return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cameraItems.clear();
        
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int distance = getDistanceFromTerminal(mTerminalId,
                        cursor.getDouble(cursor.getColumnIndex(Cameras.CAMERA_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(Cameras.CAMERA_LONGITUDE)));

                // If less than a mile from terminal, show the camera
                if (distance < 5280) { // in feet
                    CameraItem camera = new CameraItem();
                    camera.setCameraId(cursor.getInt(cursor.getColumnIndex(Cameras.CAMERA_ID)));
                    camera.setTitle(cursor.getString(cursor.getColumnIndex(Cameras.CAMERA_TITLE)));
                    camera.setImageUrl(cursor.getString(cursor.getColumnIndex(Cameras.CAMERA_URL)));
                    camera.setLatitude(cursor.getDouble(cursor.getColumnIndex(Cameras.CAMERA_LATITUDE)));
                    camera.setLongitude(cursor.getDouble(cursor.getColumnIndex(Cameras.CAMERA_LONGITUDE)));
                    camera.setDistance(distance);
                    
                    cameraItems.add(camera);
                }
                cursor.moveToNext();
            }
            
            Collections.sort(cameraItems, CameraItem.cameraDistanceComparator);
            mAdapter.setData(cameraItems);
        } else {
            TextView t = (TextView) mEmptyView;
            t.setText(R.string.no_list_data);
            getListView().setEmptyView(mEmptyView);
        }
        
        mLoadingSpinner.setVisibility(View.GONE);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setData(null);
    }

    public static class CameraImagesLoader extends CursorLoader {
        public CameraImagesLoader(Context context, Uri uri,
                String[] projection, String selection, String[] selectionArgs,
                String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            
            mAdapter.clear();
            mLoadingSpinner.setVisibility(View.VISIBLE);

            forceLoad();
        }

        @Override
        public Cursor loadInBackground() {
            return super.loadInBackground();
        }

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        Bundle b = new Bundle();
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        b.putInt("id", cameraItems.get(position).getCameraId());
        intent.putExtras(b);
        
        startActivity(intent);
    }
    
    private void createTerminalLocations() {
        ferriesTerminalMap.put(1, new FerriesTerminalItem(1, "Anacortes", 48.507351, -122.677));
        ferriesTerminalMap.put(3, new FerriesTerminalItem(3, "Bainbridge Island", 47.622339, -122.509617));
        ferriesTerminalMap.put(4, new FerriesTerminalItem(4, "Bremerton", 47.561847, -122.624089));
        ferriesTerminalMap.put(5, new FerriesTerminalItem(5, "Clinton", 47.9754, -122.349581));
        ferriesTerminalMap.put(11, new FerriesTerminalItem(11, "Coupeville", 48.159008, -122.672603));
        ferriesTerminalMap.put(8, new FerriesTerminalItem(8, "Edmonds", 47.813378, -122.385378));
        ferriesTerminalMap.put(9, new FerriesTerminalItem(9, "Fauntleroy", 47.5232, -122.3967));
        ferriesTerminalMap.put(10, new FerriesTerminalItem(10, "Friday Harbor", 48.535783, -123.013844));
        ferriesTerminalMap.put(12, new FerriesTerminalItem(12, "Kingston", 47.794606, -122.494328));
        ferriesTerminalMap.put(13, new FerriesTerminalItem(13, "Lopez Island", 48.570928, -122.882764));
        ferriesTerminalMap.put(14, new FerriesTerminalItem(14, "Mukilteo", 47.949544, -122.304997));
        ferriesTerminalMap.put(15, new FerriesTerminalItem(15, "Orcas Island", 48.597333, -122.943494));
        ferriesTerminalMap.put(16, new FerriesTerminalItem(16, "Point Defiance", 47.306519, -122.514053));
        ferriesTerminalMap.put(17, new FerriesTerminalItem(17, "Port Townsend", 48.110847, -122.759039));
        ferriesTerminalMap.put(7, new FerriesTerminalItem(7, "Seattle", 47.602501, -122.340472));
        ferriesTerminalMap.put(18, new FerriesTerminalItem(18, "Shaw Island", 48.584792, -122.92965));
        ferriesTerminalMap.put(19, new FerriesTerminalItem(19, "Sidney B.C.", 48.643114, -123.396739));
        ferriesTerminalMap.put(20, new FerriesTerminalItem(20, "Southworth", 47.513064, -122.495742));
        ferriesTerminalMap.put(21, new FerriesTerminalItem(21, "Tahlequah", 47.331961, -122.507786));
        ferriesTerminalMap.put(22, new FerriesTerminalItem(22, "Vashon Island", 47.51095, -122.463639));
    }
    
    public class CamerasSyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");
            
            if (responseString != null) {
                if (responseString.equals("OK") || responseString.equals("NOP")) {
                    // We've got cameras, now add them.
                    getLoaderManager().initLoader(0, null, FerriesTerminalCameraFragment.this);
                } else {
                    Log.e("CameraDownloadReceiver", responseString);
                }
            }
        }
    }

    private class CameraImageAdapter extends ArrayAdapter<CameraItem> {
        private final LayoutInflater mInflater;
        private ImageManager imageManager;
        
        public CameraImageAdapter(Context context) {
            super(context, R.layout.list_item_resizeable_image);
            
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imageManager = new ImageManager(context, 5 * DateUtils.MINUTE_IN_MILLIS); // Cache for 5 minutes.
        }
        
        public void setData(ArrayList<CameraItem> data) {
            clear();
            if (data != null) {
                //addAll(data); // Only in API level 11
                notifyDataSetChanged();
                int size = data.size();
                for (int i=0; i < size; i++) {
                    add(data.get(i));
                }
                notifyDataSetChanged();                
            }
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_resizeable_image, null);
                holder = new ViewHolder();
                holder.image = (ResizeableImageView) convertView.findViewById(R.id.image);
                
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            CameraItem item = getItem(position);
            
            holder.image.setTag(item.getImageUrl());
            imageManager.displayImage(item.getImageUrl(), getActivity(), holder.image);
            
            return convertView;
        }
        
        public class ViewHolder {
            public ImageView image;
        } 
    }

    /**
     * Haversine formula
     * 
     * Provides great-circle distances between two points on a sphere from
     * their longitudes and latitudes.
     * 
     * http://en.wikipedia.org/wiki/Haversine_formula
     * 
     * @param latitude
     * @param longitude
     */
    protected int getDistanceFromTerminal(int terminalId, double latitude, double longitude) {
        FerriesTerminalItem terminal = ferriesTerminalMap.get(terminalId);
        double earthRadius = 20902200; // feet
        double dLat = Math.toRadians(terminal.getLatitude() - latitude);
        double dLng = Math.toRadians(terminal.getLongitude() - longitude);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(latitude))
                * Math.cos(Math.toRadians(terminal.getLatitude()));
        
        double c = 2 * Math.asin(Math.sqrt(a));
        int distance = (int) Math.round(earthRadius * c);
        
        return distance;
    }
}
