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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RouteAlertsItems extends ListActivity {

	public FerriesRouteItem routeItems;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		routeItems = (FerriesRouteItem)getIntent().getSerializableExtra("routeItems");
		setContentView(R.layout.main);
		((TextView)findViewById(R.id.sub_section)).setText("Ferries Route Alerts");

		int numAlerts = routeItems.getFerriesRouteAlertItem().size();
		String[] alerts = new String[numAlerts];
		
		for (int i=0; i<numAlerts; i++)
		{
			alerts[i] = routeItems.getFerriesRouteAlertItem().get(i).getAlertFullTitle();
		}
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, alerts));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Bundle b = new Bundle();
		Intent intent = new Intent(this, RouteAlertsItemDetails.class);
		b.putString("AlertFullTitle", routeItems.getFerriesRouteAlertItem().get(position).getAlertFullTitle());		
		b.putString("AlertDescription", routeItems.getFerriesRouteAlertItem().get(position).getAlertDescription());
		b.putString("AlertFullText", routeItems.getFerriesRouteAlertItem().get(position).getAlertFullText());
		intent.putExtras(b);
		startActivity(intent);		
	}
}
