package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.Ferries;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.TollRatesTabActivity;
import gov.wa.wsdot.android.wsdot.TrafficMap;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class DashboardFragment extends SherlockFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container);

        root.findViewById(R.id.home_btn_traffic).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	startActivity(new Intent(getActivity(), TrafficMap.class));
            }            
        });

        root.findViewById(R.id.home_btn_ferries).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	startActivity(new Intent(getActivity(), Ferries.class));
            }
        });

        root.findViewById(R.id.home_btn_passes).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	startActivity(new Intent(getActivity(), MountainPassesActivity.class));
            }
        });

        root.findViewById(R.id.home_btn_social).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	startActivity(new Intent(getActivity(), SocialMediaActivity.class));
            }
        });
        
        root.findViewById(R.id.home_btn_tolling).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	startActivity(new Intent(getActivity(), TollRatesTabActivity.class));
            }
        });        
        
        root.findViewById(R.id.home_btn_border).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), BorderWaitActivity.class));                
            }
        });
        
        return root;
    }

}