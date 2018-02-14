package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts.MyRouteAlertsListActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.ProgressDialogFragment;

public class MyRouteFragment extends BaseFragment implements Injectable {

    final String TAG = MyRouteFragment.class.getSimpleName();

    private Tracker mTracker;
    private ProgressDialogFragment progressDialog;
    private LinearLayout mEmptyView;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private MyRouteViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    MyRouteAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_my_routes_recycler_list, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyRouteAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mEmptyView = root.findViewById(R.id.empty_list_view);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteViewModel.class);

        viewModel.loadMyRoutes().observe(this, myRoutes -> {
            if (myRoutes != null){
                if (myRoutes.size() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                }
                mAdapter.setData(myRoutes);
            }
        });

        return root;
    }

    public void onOptionSelected(final long routeID, String routeName, int position){

        // GA tracker
        mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();

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

    public void deleteRouteAction(final String routeName, final long routeID){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Delete Route: " + routeName + "?");
        builder.setMessage("This cannot be undone.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                viewModel.deleteRoute(routeID);

                mAdapter.notifyDataSetChanged();
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

        try {

            TypedArray ta = getActivity().getTheme().obtainStyledAttributes(R.styleable.ThemeStyles);

            Drawable drawable = input.getBackground(); // get current EditText drawable

            drawable.setColorFilter(ta.getColor(R.styleable.ThemeStyles_fabButtonColor, getResources().getColor(R.color.primary_default)), PorterDuff.Mode.SRC_ATOP); // change the drawable color

            if (Build.VERSION.SDK_INT > 16) {
                input.setBackground(drawable); // set the new drawable to EditText
            } else {
                input.setBackgroundDrawable(drawable); // use setBackground Drawable because setBackground required API 16
            }

        } catch (NullPointerException e) {
            MyLogger.crashlyticsLog("My Routes","get theme", "nonfatal error", 1);
        }

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = routeName;
            if (!input.getText().toString().trim().equals("")) {
                newName = input.getText().toString();
            }
            dialog.dismiss();

            viewModel.updateRouteTitle(routeID, newName);

            mAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void findFavoritesOnRoute(final long routeID){

        new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle("Add Favorites?")
                .setMessage("Traffic cameras, travel times, pass reports, and other content will " +
                            "be added to your favorites if they are on this route. ")

                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                    progressDialog = new ProgressDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("message", "Finding Favorites...");
                    progressDialog.setArguments(args);
                    progressDialog.show(getActivity().getSupportFragmentManager(), "progress_dialog");

                    viewModel.getFoundFavorites().observe(this, foundFav -> {
                        if (foundFav != null){
                            if (foundFav){
                                progressDialog.dismiss();
                                viewModel.getFoundFavorites().removeObservers(this);
                                viewModel.resetFindFavorites(); // reset the value back to false for reuse
                            }
                        }
                    });

                    viewModel.findFavoritesOnRoute(routeID);

                }).setNegativeButton(android.R.string.no, (dialog, which) -> {})
                .show();
    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class MyRouteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        private Context context;
        private List<MyRouteEntity> mData = new ArrayList<>();

        public MyRouteAdapter(Context context) {
            this.context = context;
        }

        public void setData(List<MyRouteEntity> data){
            this.mData = data;
            Log.e(TAG, String.valueOf(data.size()));
            this.notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_my_route, null);
            return new MyRouteVH(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

            MyRouteEntity myRoute = mData.get(i);

            MyRouteVH itemViewHolder = (MyRouteVH) viewHolder;

            itemViewHolder.title.setText(myRoute.getTitle());

            itemViewHolder.star_button.setTag(myRoute.getMyRouteId());

            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            itemViewHolder.star_button.setOnCheckedChangeListener(null);
            itemViewHolder.star_button.setContentDescription("favorite");
            itemViewHolder.star_button.setChecked(myRoute.getIsStarred() != 0);
            itemViewHolder.star_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
                    long rowId = (Long) buttonView.getTag();

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

                    viewModel.setIsStarred(rowId, (isChecked ? 1 : 0));

                }
            });

            itemViewHolder.alert_button.setTag(i);
            itemViewHolder.alert_button.setContentDescription("Check alerts on route");
            itemViewHolder.alert_button.setOnClickListener(v -> {
                mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Button Tap")
                        .setAction("Check Alerts")
                        .setLabel("My Routes")
                        .build());

                Bundle b = new Bundle();

                Intent intent = new Intent(getActivity(), MyRouteAlertsListActivity.class);

                b.putString("title", "Alerts on Route: " + myRoute.getTitle());
                b.putString("route", myRoute.getRouteLocations());

                intent.putExtras(b);
                startActivity(intent);
            });

            itemViewHolder.map_button.setTag(i);
            itemViewHolder.map_button.setContentDescription("Check map for route");
            itemViewHolder.map_button.setOnClickListener(v -> {
                mTracker = ((WsdotApplication) getActivity().getApplication()).getDefaultTracker();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Button Tap")
                        .setAction("Check Map for Route")
                        .setLabel("My Routes")
                        .build());

                Bundle b = new Bundle();

                Intent intent = new Intent(getActivity(), TrafficMapActivity.class);

                b.putDouble("lat", myRoute.getLatitude());
                b.putDouble("long", myRoute.getLongitude());
                b.putInt("zoom", myRoute.getZoom());

                intent.putExtras(b);
                startActivity(intent);
            });

            itemViewHolder.settings_button.setTag(i);
            itemViewHolder.settings_button.setContentDescription("Route Settings");
            itemViewHolder.settings_button.setOnClickListener(v -> {

                int[] menu_icons = {R.drawable.ic_action_delete_forever, R.drawable.ic_action_edit, R.drawable.ic_action_route, R.drawable.ic_action_favorite};
                long routeID = myRoute.getMyRouteId();
                String routeName = myRoute.getTitle();

                Log.e(TAG, String.valueOf(myRoute.getMyRouteId()));

                RouteOptionsDialogFragment.newInstance(routeID, routeName, getResources().getStringArray(R.array.my_route_options), menu_icons).show(getActivity().getSupportFragmentManager(), "dialog");
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    public class MyRouteVH extends RecyclerView.ViewHolder {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");

        protected TextView title;
        protected CheckBox star_button;
        protected ImageButton alert_button;
        protected ImageButton map_button;
        protected ImageButton settings_button;

        public MyRouteVH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            star_button = itemView.findViewById(R.id.star_button);
            alert_button = itemView.findViewById(R.id.alert_button);
            map_button = itemView.findViewById(R.id.map_button);
            settings_button = itemView.findViewById(R.id.settings_button);
        }
    }
}
