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

package gov.wa.wsdot.android.wsdot.ui.amtrakcascades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesStationItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;

public class AmtrakCascadesSchedulesFragment extends BaseFragment implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    
    private List<AmtrakCascadesStationItem> amtrakStationItems = new ArrayList<AmtrakCascadesStationItem>();
    private Map<String, String> stationsMap = new HashMap<String, String>();
    private Map<String, String> daysOfWeekMap = new HashMap<String, String>();
    private Spinner daySpinner;
    private Spinner originSpinner;
    private Spinner destinationSpinner;
    
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected double mLatitude;
    protected double mLongitude;

    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int REQUEST_ACCESS_FINE_LOCATION = 100;    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
        
    }

    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        
    }

    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        
    }

    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub
        
    }

}
