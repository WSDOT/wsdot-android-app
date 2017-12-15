package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MyRoute;
import gov.wa.wsdot.android.wsdot.service.CamerasSyncService;
import gov.wa.wsdot.android.wsdot.service.FerriesSchedulesSyncService;
import gov.wa.wsdot.android.wsdot.service.MountainPassesSyncService;
import gov.wa.wsdot.android.wsdot.service.TravelTimesSyncService;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts.MyRouteAlertsListActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.ProgressDialogFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class MyRouteFragment extends BaseFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    final String TAG = MyRouteFragment.class.getSimpleName();

    final int MY_CURSOR_ID = 1;
    private Tracker mTracker;
    private ProgressDialogFragment progressDialog;
    private int runningTasks = 0;

    List<LatLng> myRoute = new ArrayList<>();

    final private String[] projection = {
            MyRoute._ID,
            MyRoute.MY_ROUTE_TITLE,
            MyRoute.MY_ROUTE_DISPLAY_LAT,
            MyRoute.MY_ROUTE_DISPLAY_LONG,
            MyRoute.MY_ROUTE_DISPLAY_ZOOM,
            MyRoute.MY_ROUTE_LOCATIONS,
            MyRoute.MY_ROUTE_IS_STARRED,
    };

    MyRouteAdapter myRouteAdapter;

    public MyRouteFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_route, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();
        this.getLoaderManager().initLoader(MY_CURSOR_ID, null, this);
    }

    public void onOptionSelected(final long routeID, int position){

        // GA tracker
        mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();

        String routeName = getRouteName(routeID);
        switch (position){
            case 0: // Delete Route
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Button Tap")
                        .setAction("Delete Route")
                        .setLabel("My Routes")
                        .build());
                deleteRouteAction(routeName, routeID);
                break;
            case 1: // Rename route
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Button Tap")
                        .setAction("Rename Route")
                        .setLabel("My Routes")
                        .build());
                renameRouteAction(routeName, routeID);
                break;
            case 2: // Show on Map
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Button Tap")
                        .setAction("Show Route on Map")
                        .setLabel("My Routes")
                        .build());
                Bundle b = new Bundle();
                Intent intent = new Intent(getActivity(), MyRouteMapActivity.class);
                b.putLong("route_id", routeID);
                b.putString("route_name", routeName);
                intent.putExtras(b);
                startActivity(intent);
                break;
            case 3: // Find favorites
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Button Tap")
                        .setAction("Find Favorites")
                        .setLabel("My Routes")
                        .build());
                findFavoritesOnRoute(routeID);
                break;
            default:
                break;
        }
    }


    public String getRouteName(long routeID) {
        ContentResolver resolver = getActivity().getContentResolver();
        String routeName = "";
        Cursor cursor = null;
        try {
            cursor = resolver.query(MyRoute.CONTENT_URI,
                    projection,
                    MyRoute._ID + " = ?",
                    new String[] {String.valueOf(routeID)},
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {
                routeName = cursor.getString(cursor.getColumnIndex(WSDOTContract.MyRoute.MY_ROUTE_TITLE));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return routeName;
    }

    public void deleteRouteAction(final String routeName, final long routeID){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Delete Route: " + routeName + "?");
        builder.setMessage("This cannot be undone.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().getContentResolver().delete(
                        MyRoute.CONTENT_URI,
                        MyRoute._ID + "=?",
                        new String[]{String.valueOf(routeID)}
                );
                myRouteAdapter.notifyDataSetChanged();
                dialog.dismiss();
                Snackbar.make(getView().findViewById(R.id.my_route_fragment), "Route Deleted", Snackbar.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void renameRouteAction(final String routeName, final long routeID){

        AlertDialog.Builder builder = new AlertDialog.Builder(MyRouteFragment.this.getContext(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Enter a new name");

        // Set up the input
        final EditText input = new EditText(MyRouteFragment.this.getContext());
        input.setHint(routeName);

        Drawable drawable = input.getBackground(); // get current EditText drawable
        drawable.setColorFilter(ContextCompat.getColor(MyRouteFragment.this.getContext(), R.color.primary), PorterDuff.Mode.SRC_ATOP); // change the drawable color

        if(Build.VERSION.SDK_INT > 16) {
            input.setBackground(drawable); // set the new drawable to EditText
        }else{
            input.setBackgroundDrawable(drawable); // use setBackgroundDrawable because setBackground required API 16
        }

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = routeName;
                if (!input.getText().toString().trim().equals("")) {
                    newName = input.getText().toString();
                }
                dialog.dismiss();
                ContentValues values = new ContentValues();
                values.put(WSDOTContract.MyRoute.MY_ROUTE_TITLE, newName);
                getActivity().getContentResolver().update(MyRoute.CONTENT_URI,
                        values,
                        MyRoute._ID + "=?",
                        new String[]{String.valueOf(routeID)}
                );
                myRouteAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void findFavoritesOnRoute(final long routeID){

        Cursor data = getActivity().getContentResolver().query(
                MyRoute.CONTENT_URI,
                projection,
                MyRoute._ID + "=?",
                new String[] {String.valueOf(routeID)},
                null
        );

        if (data != null){
            data.moveToFirst();
            try {
                myRoute = ParserUtils.getRouteArrayList(new JSONArray(data.getString(data.getColumnIndex(MyRoute.MY_ROUTE_LOCATIONS))));

                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle("Add Favorites?")
                        .setMessage("Traffic cameras, travel times, pass reports, and other content will " +
                                "be added to your favorites if they are on this route. ")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                progressDialog = new ProgressDialogFragment();
                                Bundle args = new Bundle();
                                args.putString("message", "Finding Favorites...");
                                progressDialog.setArguments(args);
                                progressDialog.show(getActivity().getSupportFragmentManager(), "progress_dialog");

                                runningTasks = ((FindFavoritesOnRouteActivity) getActivity()).MAX_NUM_TASKS;

                                Intent mCamerasIntent = new Intent(getActivity(), CamerasSyncService.class);
                                getActivity().startService(mCamerasIntent);

                                Intent mTravelTimesIntent = new Intent(getActivity(), TravelTimesSyncService.class);
                                getActivity().startService(mTravelTimesIntent);

                                Intent mFerriesSchedulesIntent = new Intent(getActivity(), FerriesSchedulesSyncService.class);
                                getActivity().startService(mFerriesSchedulesIntent);

                                Intent mMountainPassesIntent = new Intent(getActivity(), MountainPassesSyncService.class);
                                getActivity().startService(mMountainPassesIntent);
                            }
                        })

                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

            } catch (JSONException e){
                Toast.makeText(getActivity(), "Error reading saved route.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Error finding saved route.", Toast.LENGTH_LONG).show();
        }

    }

    public List<LatLng> myGetRoute() {
        return myRoute;
    }

    public void myTaskComplete(){
        runningTasks--;
        if (runningTasks == 0){
            progressDialog.dismiss();
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Uri.withAppendedPath(MyRoute.CONTENT_URI, Uri.encode("")),
                projection,
                null,
                null,
                null
        );
    }

    public void onLoadFinished(Loader<Cursor> arg0, final Cursor arg1) {
        myRouteAdapter = new MyRouteAdapter(getContext(), arg1);
        ListView list = (ListView) getActivity().findViewById(R.id.list_view);
        list.setEmptyView(getActivity().findViewById(R.id.empty_list_view));
        list.setAdapter(myRouteAdapter);
    }

    public void onLoaderReset(Loader<Cursor> arg0) {
        myRouteAdapter.changeCursor(null);
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
                            .make(MyRouteFragment.this.getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

                    Snackbar removed_snackbar = Snackbar
                            .make(MyRouteFragment.this.getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

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
            alert_button.setContentDescription("Check alerts on route");
            alert_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Button Tap")
                            .setAction("Check Alerts")
                            .setLabel("My Routes")
                            .build());

                    Cursor c = myRouteAdapter.getCursor();
                    c.moveToPosition(position);
                    Bundle b = new Bundle();

                    Intent intent = new Intent(getActivity(), MyRouteAlertsListActivity.class);

                    b.putString("title", "Alerts on Route: " + c.getString(c.getColumnIndex(MyRoute.MY_ROUTE_TITLE)));
                    b.putString("route", c.getString(c.getColumnIndexOrThrow(MyRoute.MY_ROUTE_LOCATIONS)));

                    intent.putExtras(b);
                    startActivity(intent);
                }
            });

            ImageButton map_button = (ImageButton) view.findViewById(R.id.map_button);
            map_button.setTag(cursor.getPosition());
            map_button.setContentDescription("Check map for route");
            map_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Button Tap")
                            .setAction("Check Map for Route")
                            .setLabel("My Routes")
                            .build());

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
            settings_button.setContentDescription("Route Settings");
            settings_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Cursor c = myRouteAdapter.getCursor();
                    c.moveToPosition(position);

                    int[] menu_icons = {R.drawable.ic_action_delete_forever, R.drawable.ic_action_edit, R.drawable.ic_action_route, R.drawable.ic_action_favorite};
                    long routeID = c.getLong(c.getColumnIndex(MyRoute._ID));

                    RouteOptionsDialogFragment.newInstance(routeID, getResources().getStringArray(R.array.my_route_options), menu_icons).show(getActivity().getSupportFragmentManager(), "dialog");
                }
            });
        }

    }
}
