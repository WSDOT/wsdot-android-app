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
package gov.wa.wsdot.android.wsdot.util.map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.RestAreaItem;

public class RestAreasOverlay {

    @SuppressWarnings("unused")
    private static final String TAG = RestAreasOverlay.class.getSimpleName();
    private List<RestAreaItem> restAreaItems = new ArrayList<>();

    public RestAreasOverlay(InputStream inputStream) {
        restAreaItems = getRestAreas(inputStream);
    }

    public List<RestAreaItem> getRestAreaItems() {
        return restAreaItems;
    }

    public int size() {
        return restAreaItems.size();
    }

    private List<RestAreaItem> getRestAreas(InputStream inputStream) {

        List<RestAreaItem> restAreas = new ArrayList<>();
        RestAreaItem item;
        Integer restarea = R.drawable.restarea;
        Integer restarea_dump = R.drawable.restarea_trailerdump;

        String jsonString = parseInputJsonFile(inputStream);

        JSONArray restAreasJSON = null;

        try {
            restAreasJSON = new JSONArray(jsonString);

            for (int i = 0; i < restAreasJSON.length(); i++){
                item = new RestAreaItem();

                JSONObject restAreaJSON = restAreasJSON.getJSONObject(i);

                item.setLocation(restAreaJSON.getString("location"));
                item.setRoute(restAreaJSON.getString("route"));
                item.setMilepost(restAreaJSON.getInt("milepost"));
                item.setDirection(restAreaJSON.getString("direction"));
                item.setIcon(restAreaJSON.getBoolean("hasDump") ? restarea_dump : restarea);
                item.setLatitude(Double.valueOf(restAreaJSON.getString("latitude")));
                item.setLongitude(Double.valueOf(restAreaJSON.getString("longitude")));
                item.setNotes(restAreaJSON.getString("notes"));

                JSONArray amenitiesJSON = restAreaJSON.getJSONArray("amenities");
                for (int j = 0; j < amenitiesJSON.length(); j++){
                    item.addAmenitie(amenitiesJSON.getString(j));
                }

                restAreas.add(item);
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }

        return restAreas;
    }

    private String parseInputJsonFile(InputStream is){
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        return writer.toString();
    }

}
