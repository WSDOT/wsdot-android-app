/*
 * Copyright (c) 2017 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.service.EventService;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.amtrakcascades.AmtrakCascadesActivity;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.FerriesActivity;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassesActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.MyRouteActivity;
import gov.wa.wsdot.android.wsdot.ui.tollrates.TollRatesActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;

public class DashboardFragment extends BaseFragment implements Injectable {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        MyLogger.crashlyticsLog("Home", "Screen View", "DashboardFragment", 1);

        View root = inflater.inflate(R.layout.fragment_dashboard, null);
        root.findViewById(R.id.home_btn_traffic).setOnClickListener(view -> startActivity(new Intent(getActivity(), TrafficMapActivity.class)));
        root.findViewById(R.id.home_btn_ferries).setOnClickListener(view -> startActivity(new Intent(getActivity(), FerriesActivity.class)));
        root.findViewById(R.id.home_btn_passes).setOnClickListener(view -> startActivity(new Intent(getActivity(), MountainPassesActivity.class)));
        root.findViewById(R.id.home_btn_tolling).setOnClickListener(view -> startActivity(new Intent(getActivity(), TollRatesActivity.class)));
        root.findViewById(R.id.home_btn_border).setOnClickListener(view -> startActivity(new Intent(getActivity(), BorderWaitActivity.class)));
        root.findViewById(R.id.home_btn_amtrak).setOnClickListener(view -> startActivity(new Intent(getActivity(), AmtrakCascadesActivity.class)));
        root.findViewById(R.id.home_btn_my_routes).setOnClickListener(view -> startActivity(new Intent(getActivity(), MyRouteActivity.class)));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Boolean eventActive = prefs.getBoolean(getString(R.string.event_is_active), false);
        LinearLayout banner_view = root.findViewById(R.id.event_banner);
        TextView banner_text = root.findViewById(R.id.event_banner_text);

        if (eventActive) {
            banner_view.setVisibility(View.VISIBLE);
            banner_view.setOnClickListener(view -> startActivity(new Intent(getActivity(), EventActivity.class)));

            banner_text.setText(prefs.getString(getString(R.string.event_banner_text_key), "error"));

        } else {
            banner_view.setVisibility(View.GONE);
        }

        return root;
    }
}