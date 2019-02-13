package gov.wa.wsdot.android.wsdot.ui.myroute.report;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.MyRouteViewModel;

public class MyRouteReportActivity extends BaseActivity implements
        OnMapReadyCallback {

    private MyRouteViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_route_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteViewModel.class);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);


        MyRouteReportFragment reportFragment = (MyRouteReportFragment) getSupportFragmentManager().findFragmentById(R.id.report_fragment);





    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
