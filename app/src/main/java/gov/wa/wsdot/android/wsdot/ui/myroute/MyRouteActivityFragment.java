package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MyRoute;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.FerriesRouteAlertsBulletinsActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MyRouteActivityFragment extends BaseFragment {

    Cursor cursor;
    MyRouteAdapter myRouteAdapter;

    protected RecyclerView mRecyclerView;

    private static final String[] my_route_projection = {

            MyRoute._ID,
            MyRoute.MY_ROUTE_TITLE,
            MyRoute.MY_ROUTE_DISPLAY_LAT,
            MyRoute.MY_ROUTE_DISPLAY_LONG,
            MyRoute.MY_ROUTE_DISPLAY_ZOOM,
            MyRoute.MY_ROUTE_IS_STARRED,
    };

    public MyRouteActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_route, container, false);
    }

    @Override
    public void onResume(){
        super.onResume();

        ListView list = (ListView) getActivity().findViewById(R.id.list_view);
        list.setEmptyView(getActivity().findViewById(R.id.empty_list_view));

        cursor = getActivity().getContentResolver().query(
                MyRoute.CONTENT_URI,
                my_route_projection,
                null,
                null,
                null
        );

        myRouteAdapter = new MyRouteAdapter(getContext(), cursor);
        // Attach cursor adapter to the ListView
        list.setAdapter(myRouteAdapter);
    }

    @Override
    public void onPause(){
        super.onPause();
        cursor.close();
    }

    /**
     * Custom adapter for items in recycler view that need a cursor adapter.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see CursorRecyclerAdapter
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class MyRouteAdapter extends CursorAdapter {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;

        public MyRouteAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_my_route, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(cursor.getString(cursor.getColumnIndex(WSDOTContract.MyRoute.MY_ROUTE_TITLE)));
            title.setTypeface(tfb);

            CheckBox star_button = (CheckBox) view.findViewById(R.id.star_button);

            star_button.setTag(cursor.getInt(cursor.getColumnIndex("_id")));

            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            star_button.setOnCheckedChangeListener(null);
            star_button.setContentDescription("favorite");
            star_button.setChecked(cursor.getInt(cursor
                            .getColumnIndex(WSDOTContract.MyRoute.MY_ROUTE_IS_STARRED)) != 0);
            star_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
                    int rowId = (Integer) buttonView.getTag();
                    ContentValues values = new ContentValues();
                    values.put(WSDOTContract.MyRoute.MY_ROUTE_IS_STARRED, isChecked ? 1 : 0);

                    Snackbar added_snackbar = Snackbar
                            .make(MyRouteActivityFragment.this.getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

                    Snackbar removed_snackbar = Snackbar
                            .make(MyRouteActivityFragment.this.getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

                    added_snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("added to favorites");
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });
                    removed_snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("removed from favorites");
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });

                    if (isChecked){
                        added_snackbar.show();
                    }else{
                        removed_snackbar.show();
                    }

                    getActivity().getContentResolver().update(
                            MyRoute.CONTENT_URI,
                            values,
                            MyRoute._ID + "=?",
                            new String[] {Integer.toString(rowId)}
                    );
                }
            });

            final int position = cursor.getPosition();

            ImageButton alert_button = (ImageButton) view.findViewById(R.id.alert_button);
            alert_button.setTag(cursor.getPosition());
            alert_button.setImageResource(R.drawable.ic_route_alert);
            alert_button.setContentDescription("Check alerts on route");
            alert_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Cursor c = myRouteAdapter.getCursor();
                    c.moveToPosition(position);
                    Bundle b = new Bundle();

                    //TODO - make My Route Alerts activity
                   // Intent intent = new Intent(getActivity(), FerriesRouteAlertsBulletinsActivity.class);
                   // b.putString("title", c.getString(c.getColumnIndex(WSDOTContract.FerriesSchedules.FERRIES_SCHEDULE_TITLE)));
                   // b.putString("alert", c.getString(c.getColumnIndex(WSDOTContract.FerriesSchedules.FERRIES_SCHEDULE_ALERT)));


                    //intent.putExtras(b);
                    //startActivity(intent);
                }
            });

            ImageButton map_button = (ImageButton) view.findViewById(R.id.map_button);
            map_button.setTag(cursor.getPosition());
            map_button.setImageResource(R.drawable.ic_map);
            map_button.setContentDescription("Check map for route");
            map_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Cursor c = myRouteAdapter.getCursor();
                    c.moveToPosition(position);
                    Bundle b = new Bundle();

                    Intent intent = new Intent(getActivity(), TrafficMapActivity.class);

                    b.putFloat("lat", c.getFloat(c.getColumnIndex(MyRoute.MY_ROUTE_DISPLAY_LAT)));
                    b.putFloat("long", c.getFloat(c.getColumnIndex(MyRoute.MY_ROUTE_DISPLAY_LONG)));
                    b.putInt("zoom", c.getInt(c.getColumnIndex(MyRoute.MY_ROUTE_DISPLAY_ZOOM)));

                    intent.putExtras(b);
                    startActivity(intent);
                }
            });

            ImageButton settings_button = (ImageButton) view.findViewById(R.id.settings_button);
            settings_button.setTag(cursor.getPosition());
            settings_button.setImageResource(R.drawable.ic_menu_settings);
            settings_button.setContentDescription("Route Settings");
            settings_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Cursor c = myRouteAdapter.getCursor();
                    c.moveToPosition(position);
                    Bundle b = new Bundle();


                    //TODO
                   // Intent intent = new Intent(getActivity(), TrafficMapActivity.class);


                    //intent.putExtras(b);
                    //startActivity(intent);
                }
            });
        }

    }
}
