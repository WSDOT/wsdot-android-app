package gov.wa.wsdot.android.wsdot.ui.settings;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.home.FavoritesFragment;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.OnStartDragListener;

public class SettingsActivity extends BaseActivity implements OnStartDragListener {

    final static String TAG = SettingsActivity.class.getSimpleName();

    private static final int HEADER_VIEWTYPE = 0;
    private static final int FAVORITES_SECTION_VIEWTYPE = 1;

    private int orderedViewTypes[] = new int[7];

    private SettingsAdapter mSettingsAdapter;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Check preferences and set defaults if none set
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        orderedViewTypes[0] = settings.getInt("KEY_FIRST_FAVORITES_SECTION", FavoritesFragment.MY_ROUTE_VIEWTYPE);
        orderedViewTypes[1] = settings.getInt("KEY_SECOND_FAVORITES_SECTION", FavoritesFragment.CAMERAS_VIEWTYPE);
        orderedViewTypes[2] = settings.getInt("KEY_THIRD_FAVORITES_SECTION", FavoritesFragment.FERRIES_SCHEDULES_VIEWTYPE);
        orderedViewTypes[3] = settings.getInt("KEY_FOURTH_FAVORITES_SECTION", FavoritesFragment.MOUNTAIN_PASSES_VIEWTYPE);
        orderedViewTypes[4] = settings.getInt("KEY_FIFTH_FAVORITES_SECTION", FavoritesFragment.TRAVEL_TIMES_VIEWTYPE);
        orderedViewTypes[5] = settings.getInt("KEY_SIXTH_FAVORITES_SECTION", FavoritesFragment.LOCATION_VIEWTYPE);
        orderedViewTypes[6] = settings.getInt("KEY_SEVENTH_FAVORITES_SECTION", FavoritesFragment.TOLL_RATE_VIEWTYPE);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);


        // Add swipe dismiss to favorites list items.
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN | ItemTouchHelper.UP, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                if (viewHolder instanceof SettingsAdapter.FavoritesSectionViewHolder && target instanceof SettingsAdapter.FavoritesSectionViewHolder) {
                    mSettingsAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    return true;
                }else{
                    return false;
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}

        };

        itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        mSettingsAdapter = new SettingsAdapter(this);
        mRecyclerView.setAdapter(mSettingsAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("Settings");
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Custom adapter for items in recycler view that need a cursor adapter.
     * Binds the custom ViewHolder class to it's data.
     *
     * @see CursorRecyclerAdapter
     * @see RecyclerView.Adapter
     */
    private class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        private List<FavoritesSectionViewHolder> mItems = new ArrayList<>();
        private OnStartDragListener mDragStartListener;


        public SettingsAdapter(OnStartDragListener dragStartListener){
            this.mDragStartListener = dragStartListener;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            if (viewHolder instanceof HeaderViewHolder){

                HeaderViewHolder holder = (HeaderViewHolder) viewHolder;

                String title = "Favorites Order";
                holder.title.setText(title);
                holder.title.setTypeface(tfb);

                //Remove divider if first element in list
                if (position == 0) {
                    holder.divider.setVisibility(View.GONE);
                }else{
                    holder.divider.setVisibility(View.VISIBLE);
                }

            } else if (viewHolder instanceof FavoritesSectionViewHolder){
                final FavoritesSectionViewHolder holder = (FavoritesSectionViewHolder) viewHolder;

                holder.title.setText((String) FavoritesFragment.headers.get(orderedViewTypes[position - 1]));
                holder.title.setTypeface(tfb);

                holder.handle.setOnTouchListener((v, event) -> {
                    v.performClick();
                    if ((event.getActionMasked() ==
                            MotionEvent.ACTION_DOWN) || (event.getActionMasked() == MotionEvent.ACTION_UP)) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView;

            switch (viewType){
                case HEADER_VIEWTYPE:
                    itemView = LayoutInflater.
                            from(parent.getContext()).inflate(R.layout.list_header, parent, false);
                    return new HeaderViewHolder(itemView);
                case FAVORITES_SECTION_VIEWTYPE:
                    itemView = LayoutInflater.
                            from(parent.getContext()).inflate(R.layout.list_item_settings, parent, false);

                    FavoritesSectionViewHolder viewHolder = new FavoritesSectionViewHolder(itemView);
                    mItems.add(viewHolder);
                    return viewHolder;
                default:
                    return null;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0){
                return HEADER_VIEWTYPE;
            } else {
                return FAVORITES_SECTION_VIEWTYPE;
            }
        }

        @Override
        public int getItemCount() {
            return orderedViewTypes.length + 1; // +1 for header
        }

        public boolean onItemMove(int fromPosition, int toPosition) {

            int temp = orderedViewTypes[fromPosition-1];
            orderedViewTypes[fromPosition-1] = orderedViewTypes[toPosition-1];
            orderedViewTypes[toPosition-1] = temp;

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("KEY_FIRST_FAVORITES_SECTION", orderedViewTypes[0]);
            editor.putInt("KEY_SECOND_FAVORITES_SECTION", orderedViewTypes[1]);
            editor.putInt("KEY_THIRD_FAVORITES_SECTION", orderedViewTypes[2]);
            editor.putInt("KEY_FOURTH_FAVORITES_SECTION", orderedViewTypes[3]);
            editor.putInt("KEY_FIFTH_FAVORITES_SECTION", orderedViewTypes[4]);
            editor.putInt("KEY_SIXTH_FAVORITES_SECTION", orderedViewTypes[5]);
            editor.putInt("KEY_SEVENTH_FAVORITES_SECTION", orderedViewTypes[6]);
            editor.apply();

            Collections.swap(mItems, fromPosition-1, toPosition-1);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        // View Holder for settings list
        private class FavoritesSectionViewHolder extends RecyclerView.ViewHolder{
            TextView title;
            ImageButton handle;
            public View view;

            public FavoritesSectionViewHolder(View v) {
                super(v);
                view = v;
                title = v.findViewById(R.id.title);
                handle = v.findViewById(R.id.drag_handle);
            }
        }

        private class HeaderViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            LinearLayout divider;

            public HeaderViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.list_header_title);
                divider = view.findViewById(R.id.divider);
            }
        }
    }

}
