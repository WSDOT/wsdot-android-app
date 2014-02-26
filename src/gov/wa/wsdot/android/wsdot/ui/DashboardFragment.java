/*
 * Copyright (c) 2014 Washington State Department of Transportation
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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DashboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, null);

        root.findViewById(R.id.home_btn_traffic).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	startActivity(new Intent(getActivity(), TrafficMapActivity.class));
            }            
        });

        root.findViewById(R.id.home_btn_ferries).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	startActivity(new Intent(getActivity(), FerriesActivity.class));
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
            	startActivity(new Intent(getActivity(), TollRatesActivity.class));
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