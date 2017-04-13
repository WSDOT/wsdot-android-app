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

package gov.wa.wsdot.android.wsdot.ui.mountainpasses;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.service.MountainPassesSyncService;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.util.UIUtils;

public class MountainPassItemActivity extends BaseActivity {

    private final String TAG = MountainPassItemActivity.class.getSimpleName();

	private boolean mIsStarred = false;
	private ContentResolver resolver;
	private int mId;
	private Tracker mTracker;

    private MountainPassesSyncReceiver mMountainPassesSyncReceiver;

    private TabLayout mTabLayout;
    private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
    private ViewPager mViewPager;
    private gov.wa.wsdot.android.wsdot.util.TabsAdapter mTabsAdapter;
    private Toolbar mToolbar;

	static final private int MENU_ITEM_STAR = 0;
	static final private int MENU_ITEM_REFRESH = 1;

    private final String REFRESHING_KEY = "refreshing";
    private Boolean mRefreshState = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    Bundle b = getIntent().getExtras();
	    mId = b.getInt("id");
	    String mountainPassName = b.getString("MountainPassName");
	    String cameras = b.getString("Cameras");
	    String forecast = b.getString("Forecasts");
		mIsStarred = b.getInt("isStarred") != 0;

	    // GA tracker
    	mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
    	mTracker.setScreenName("/Mountain Passes/Details/" + mountainPassName);
    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setContentView(R.layout.activity_with_tabs);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle(mountainPassName);
        setSupportActionBar(mToolbar);
		if(getSupportActionBar() != null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
		Spinner mSpinner = (Spinner) findViewById(R.id.spinner);
		mSpinner.setVisibility(View.GONE);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Add tab titles and their corresponding fragments to the fragment list.
        tabFragments.add(mTabLayout.getTabCount(), MountainPassItemReportFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Report"));
        if (!cameras.equals("[]")) {
            tabFragments.add(mTabLayout.getTabCount(), MountainPassItemCameraFragment.class);
            mTabLayout.addTab(mTabLayout.newTab().setText("Cameras"));
        }
        if (!forecast.equals("[]")) {
            tabFragments.add(mTabLayout.getTabCount(), MountainPassItemForecastFragment.class);
            mTabLayout.addTab(mTabLayout.newTab().setText("Forecast"));
        }

        mTabsAdapter = new gov.wa.wsdot.android.wsdot.util.TabsAdapter
                (this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount());

        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				mViewPager.setCurrentItem(tab.getPosition());
				// GA tracker
				mTracker.setScreenName("/Mountain Passes/Details/" + tab.getText());
				mTracker.send(new HitBuilders.ScreenViewBuilder().build());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {}
		});

        enableAds();

        if (savedInstanceState != null) {
            TabLayout.Tab tab = mTabLayout.getTabAt(savedInstanceState.getInt("tab", 0));
            tab.select();
			mIsStarred = savedInstanceState.getInt("isStarred") != 0;
        }
	}

    @Override
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(mMountainPassesSyncReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.MOUNTAIN_PASSES_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mMountainPassesSyncReceiver = new MountainPassesSyncReceiver();
        this.registerReceiver(mMountainPassesSyncReceiver, filter);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem menuItem_Star = menu.add(0, MENU_ITEM_STAR, menu.size(), R.string.description_star);
        MenuItemCompat.setShowAsAction(menuItem_Star, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		if (mIsStarred) {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star_on);
			menu.getItem(MENU_ITEM_STAR).setTitle("Favorite checkbox, checked");
		} else {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star);
			menu.getItem(MENU_ITEM_STAR).setTitle("Favorite checkbox, not checked");
		}

        MenuItem menuItem_Refresh = menu.add(1, MENU_ITEM_REFRESH, menu.size(), R.string.description_refresh);
        MenuItemCompat.setShowAsAction(menuItem_Refresh, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        menu.getItem(MENU_ITEM_REFRESH).setIcon(R.drawable.ic_menu_refresh);

        if (mRefreshState){
            startRefreshAnimation();
        }

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
	    		return true;
			case MENU_ITEM_STAR:
				toggleStar(item);
				return true;
			case MENU_ITEM_REFRESH:
                startRefreshAnimation();
                refresh();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void toggleStar(MenuItem item) {
		resolver = getContentResolver();
		Snackbar added_snackbar = Snackbar
				.make(findViewById(R.id.activity_with_tabs), R.string.add_favorite, Snackbar.LENGTH_SHORT);

		Snackbar removed_snackbar = Snackbar
				.make(findViewById(R.id.activity_with_tabs), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

		added_snackbar.setCallback(new Snackbar.Callback() {
			@Override
			public void onShown(Snackbar snackbar) {
				super.onShown(snackbar);
				snackbar.getView().setContentDescription("added to favorites");
				snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
			}
		});

		removed_snackbar.setCallback(new Snackbar.Callback() {
			@Override
			public void onShown(Snackbar snackbar) {
				super.onShown(snackbar);
				snackbar.getView().setContentDescription("removed from favorites");
				snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
			}
		});

		if (mIsStarred) {
			item.setIcon(R.drawable.ic_menu_star);
			item.setTitle("Favorite checkbox, not checked");
			try {
				ContentValues values = new ContentValues();
				values.put(MountainPasses.MOUNTAIN_PASS_IS_STARRED, 0);
				resolver.update(
						MountainPasses.CONTENT_URI,
						values,
						MountainPasses.MOUNTAIN_PASS_ID + "=?",
						new String[] {Integer.toString(mId)}
						);

				removed_snackbar.show();
				mIsStarred = false;
	    	} catch (Exception e) {
	    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("MountainPasItemActivity", "Error: " + e.getMessage());
	    	}
		} else {
			item.setIcon(R.drawable.ic_menu_star_on);
			item.setTitle("Favorite checkbox, checked");
			try {
				ContentValues values = new ContentValues();
				values.put(MountainPasses.MOUNTAIN_PASS_IS_STARRED, 1);
				resolver.update(
						MountainPasses.CONTENT_URI,
						values,
						MountainPasses.MOUNTAIN_PASS_ID + "=?",
						new String[] {Integer.toString(mId)}
						);

				added_snackbar.show();
				mIsStarred = true;
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("MountainPasItemActivity", "Error: " + e.getMessage());
	    	}
		}
	}

    protected void refresh() {

        mTabsAdapter.notifyDataSetChanged();

        Intent intent = new Intent(this, MountainPassesSyncService.class);
        intent.putExtra("forceUpdate", true);
        this.startService(intent);
    }

    private void startRefreshAnimation() {
        MenuItem item = mToolbar.getMenu().findItem(MENU_ITEM_REFRESH);
        if (item == null) {
            Log.e(TAG, "null");
            return;
        }
        // define the animation for rotation
        Animation animation = new RotateAnimation(360.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);

        animation.setRepeatCount(Animation.INFINITE);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                mToolbar.getMenu().getItem(MENU_ITEM_REFRESH).setActionView(null);
                mToolbar.getMenu().getItem(MENU_ITEM_REFRESH).setIcon(R.drawable.ic_menu_refresh);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        ImageView imageView = new ImageView(this, null, android.R.style.Widget_Material_ActionButton);
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_menu_refresh));

        imageView.setPadding(31, imageView.getPaddingTop(), 32, imageView.getPaddingBottom());

        imageView.startAnimation(animation);
        item.setActionView(imageView);
        mRefreshState = true;
    }

    public class MountainPassesSyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("responseString");

            mToolbar.getMenu().getItem(MENU_ITEM_REFRESH).getActionView().getAnimation().setRepeatCount(0);

            mRefreshState = false;

            if (responseString != null) {
                switch (responseString) {
                    case "OK":
                        Toast.makeText(mTabLayout.getContext(), "Updated", Toast.LENGTH_SHORT).show();

                        SparseArray<Fragment> fragments = mTabsAdapter.getFragments();

                        for (int i = 0; i < fragments.size(); i++){
                            if (fragments.get(i) instanceof MountainPassItemReportFragment) {
                                ((MountainPassItemReportFragment) fragments.get(i)).loadReport();
                            } else if (fragments.get(i) instanceof MountainPassItemForecastFragment) {
                                ((MountainPassItemForecastFragment) fragments.get(i)).loadForecast();
                            } else if (fragments.get(i) instanceof MountainPassItemCameraFragment) {
                                ((MountainPassItemCameraFragment) fragments.get(i)).refresh();
                            }
                        }

                        break;
                    case "NOP":
                        Toast.makeText(mTabLayout.getContext(), "Updated", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Log.e("MountPassSyncReceiver", responseString);
                        if (!UIUtils.isNetworkAvailable(context)) {
                            responseString = getString(R.string.no_connection);
                        }
                        Toast.makeText(mTabLayout.getContext(), responseString, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(REFRESHING_KEY, mRefreshState);

        //Save the selected tab in order to restore in screen rotation
        outState.putInt("tab", mTabLayout.getSelectedTabPosition());
		if (mIsStarred){
			outState.putInt("isStarred",  1);
		}else{
			outState.putInt("isStarred",  0);
		}
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mRefreshState = savedInstanceState.getBoolean(REFRESHING_KEY);
    }
}
