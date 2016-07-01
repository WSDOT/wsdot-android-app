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
package gov.wa.wsdot.android.wsdot.util.map;

        import java.util.ArrayList;
        import java.util.List;

        import gov.wa.wsdot.android.wsdot.R;
        import gov.wa.wsdot.android.wsdot.shared.RestAreaItem;

public class RestAreasOverlay {

    final String visitor_info = "Visitor Information";
    final String telephone = "Telephones";
    final String picnic = "Picnic Areas";
    final String vending = "Vending Machines";
    final String water = "Drinking Water";
    final String dump = "RV Dumping Station";
    final String electric = "Electrical Vehicle Charging Station";
    final String no_winter_dump = "RV Dumping Station Closed during the Winter.";

    @SuppressWarnings("unused")
    private static final String TAG = CalloutsOverlay.class.getSimpleName();
    private List<RestAreaItem> restAreaItems = new ArrayList<>();

    public RestAreasOverlay() {
        restAreaItems = getRestAreas();
    }

    public List<RestAreaItem> getRestAreaItems() {
        return restAreaItems;
    }

    public int size() {
        return restAreaItems.size();
    }

    private List<RestAreaItem> getRestAreas() {

        List<RestAreaItem> restAreas = new ArrayList<>();

        List<String> amenities = new ArrayList<>();
        RestAreaItem item;
        Integer restarea = R.drawable.restarea;
        Integer restarea_dump = R.drawable.restarea_trailerdump;

        // Alpowa Summit - MP 413
        item = new RestAreaItem();
        item.setLocation("Alpowa Summit");
        item.setRoute("SR 12");
        item.setMilepost(413);
        item.setDirection("EastBound");
        item.setIcon(restarea);
        item.setLatitude(46.4346999);
        item.setLongitude(-117.424100000);
        item.setNotes("None.");
        item.addAmenitie(telephone);
        item.addAmenitie(visitor_info);
        item.addAmenitie(water);
        item.addAmenitie(picnic);
        restAreas.add(item);

        // Alpowa Summit - MP 413
        item = new RestAreaItem();
        item.setLocation("Alpowa Summit");
        item.setRoute("SR 12");
        item.setMilepost(413);
        item.setDirection("WestBound");
        item.setIcon(restarea);
        item.setLatitude(46.436279440);
        item.setLongitude(-117.424693900);
        item.setNotes("None.");
        item.addAmenitie(telephone);
        item.addAmenitie(visitor_info);
        item.addAmenitie(water);
        item.addAmenitie(picnic);
        restAreas.add(item);

        // Bevin Lake - MP 126
        item = new RestAreaItem();
        item.setLocation("Bevin Lake");
        item.setRoute("SR 12");
        item.setMilepost(126);
        item.setDirection("Multidirectional");
        item.setIcon(restarea);
        item.setLatitude(46.55383);
        item.setLongitude(-121.7366);
        item.setNotes("None.");
        item.addAmenitie(water);
        item.addAmenitie(picnic);
        restAreas.add(item);

        // Blue Lake - MP 89
        item = new RestAreaItem();
        item.setLocation("Blue Lake");
        item.setRoute("SR 17");
        item.setMilepost(89);
        item.setDirection("Multidirectional");
        item.setIcon(restarea);
        item.setLatitude(47.569423610);
        item.setLongitude(-119.447738600);
        item.setNotes("None.");
        item.addAmenitie(water);
        item.addAmenitie(picnic);
        restAreas.add(item);

        // Nason Creek - MP 81
        item = new RestAreaItem();
        item.setLocation("Nason Creek");
        item.setRoute("US 02");
        item.setMilepost(81);
        item.setDirection("Multidirectional");
        item.setIcon(restarea_dump);
        item.setLatitude(47.766436390);
        item.setLongitude(-120.793140300);
        item.addAmenitie(telephone);
        item.addAmenitie(water);
        item.addAmenitie(picnic);
        item.addAmenitie(vending);
        item.setNotes(no_winter_dump);
        restAreas.add(item);
        return restAreas;
    }
}
