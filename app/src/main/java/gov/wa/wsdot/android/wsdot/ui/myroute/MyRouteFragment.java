package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.myroute.MyRouteEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.myroute.report.MyRouteReportActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.ProgressDialogFragment;

public class MyRouteFragment extends BaseFragment implements Injectable {

    final String TAG = MyRouteFragment.class.getSimpleName();

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
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyRouteAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mEmptyView = root.findViewById(R.id.empty_list_view);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteViewModel.class);

        viewModel.loadMyRoutes().observe(getViewLifecycleOwner(), myRoutes -> {
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

        switch (position){
            case 0: // Delete Route
                deleteRouteAction(routeName, routeID);
                break;
            case 1: // Rename route
                renameRouteAction(routeName, routeID);
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
        builder.setPositiveButton("OK", (dialog, which) -> {

            viewModel.deleteRoute(routeID);

            mAdapter.notifyDataSetChanged();
            dialog.dismiss();
            Snackbar.make(getView().findViewById(R.id.my_route_fragment), "Route Deleted", Snackbar.LENGTH_LONG).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
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

    /**
     * Custom adapter for items in recycler view.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see RecyclerView.Adapter
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

            // Set onClickListener for holder's view
            itemViewHolder.view.setOnClickListener(
                    v -> {
                        Bundle b = new Bundle();
                        b.putLong("route_id", myRoute.getMyRouteId());
                        b.putString("route_name", myRoute.getTitle());
                        b.putString("route", myRoute.getRouteLocations());
                        Intent intent = new Intent(getActivity(), MyRouteReportActivity.class);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
            );

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

            itemViewHolder.settings_button.setTag(i);
            itemViewHolder.settings_button.setContentDescription("Route Settings");
            itemViewHolder.settings_button.setOnClickListener(v -> {

                int[] menu_icons = {R.drawable.ic_action_delete_forever, R.drawable.ic_action_edit, R.drawable.ic_action_route};
                long routeID = myRoute.getMyRouteId();
                String routeName = myRoute.getTitle();

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
        protected ImageButton settings_button;
        public View view;

        public MyRouteVH(View itemView) {
            super(itemView);
            view = itemView;
            title = itemView.findViewById(R.id.title);
            star_button = itemView.findViewById(R.id.star_button);
            settings_button = itemView.findViewById(R.id.settings_button);
        }
    }
}
