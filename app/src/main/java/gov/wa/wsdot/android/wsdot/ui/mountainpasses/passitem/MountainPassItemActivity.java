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

package gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassViewModel;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.TabsAdapter;

public class MountainPassItemActivity extends BaseActivity implements Injectable {

    private final String TAG = MountainPassItemActivity.class.getSimpleName();

	private boolean mIsStarred = false;
	private int mId = -1;

    private TabLayout mTabLayout;
    private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
    private ViewPager mViewPager;
    private gov.wa.wsdot.android.wsdot.util.TabsAdapter mTabsAdapter;
    private Toolbar mToolbar;

	static final private int MENU_ITEM_STAR = 0;
	static final private int MENU_ITEM_REFRESH = 1;

    private Boolean mRefreshState = false;

    private static MountainPassViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
	    super.onCreate(savedInstanceState);

	    Bundle b = getIntent().getExtras();
	    if (b != null) {
            mId = b.getInt("id");
        }

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MountainPassViewModel.class);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {

                mRefreshState = false;
                mToolbar = findViewById(R.id.toolbar);

                switch (resourceStatus.status) {
                    case LOADING:
                        startRefreshAnimation();
                        break;
                    case SUCCESS:
                        // If there is a loading animation playing on finish, stop it.
                        if (mToolbar.getMenu().size() > 0) {
                            if (mToolbar.getMenu().getItem(MENU_ITEM_REFRESH).getActionView() != null) {
                                mToolbar.getMenu().getItem(MENU_ITEM_REFRESH).getActionView().getAnimation().setRepeatCount(0);
                                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case ERROR:
                        // If there is a loading animation playing on finish, stop it.
                        if (mToolbar.getMenu().size() > 0) {
                            if (mToolbar.getMenu().getItem(MENU_ITEM_REFRESH).getActionView() != null) {
                                mToolbar.getMenu().getItem(MENU_ITEM_REFRESH).getActionView().getAnimation().setRepeatCount(0);
                                Toast.makeText(this, "connection error", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
            }
        });

        viewModel.getPassFor(mId).observe(this, pass -> {

            if (pass != null) {

                String mountainPassName = pass.getName();
                String cameras = pass.getCamera();
                String forecast = pass.getForecast();
                mIsStarred = pass.getIsStarred() != 0;

                mToolbar = findViewById(R.id.toolbar);
                mToolbar.setTitle(mountainPassName);
                setSupportActionBar(mToolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }

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

                mTabsAdapter = new TabsAdapter
                        (this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount());

                mViewPager.setAdapter(mTabsAdapter);
                mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

                mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {

                        MountainPassItemActivity.this.setFirebaseAnalyticsScreenName(
                                String.format("%s%s", "Pass", tab.getText()).replaceAll("\\W", ""));

                        mViewPager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                    }
                });

                // remove this initial observer and add a new one.
                viewModel.getPassFor(mId).removeObservers(this);
                viewModel.getPassFor(mId).observe(this, passItem -> mIsStarred = passItem.getIsStarred() != 0);

            } else {
                mToolbar = findViewById(R.id.toolbar);
                mToolbar.setTitle("Report Unavailable");
                setSupportActionBar(mToolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }

            }
        });

        setContentView(R.layout.activity_with_tabs);
        mViewPager = findViewById(R.id.pager);

        findViewById(R.id.spinner).setVisibility(View.GONE);

        MyLogger.crashlyticsLog("Mountain Passes", "Screen View", "MountainPassItemActivity " + mId, 1);

        enableAds(getString(R.string.passes_ad_target));

	}

	@Override
    public void onResume(){
	    super.onResume();
        setFirebaseAnalyticsScreenName("PassReport");
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem menuItem_Star = menu.add(0, MENU_ITEM_STAR, menu.size(), R.string.description_star);
        MenuItemCompat.setShowAsAction(menuItem_Star, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		if (mIsStarred) {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star_on);
			menu.getItem(MENU_ITEM_STAR).setTitle("Favorite checkbox, checked");
		} else {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star);
			menu.getItem(MENU_ITEM_STAR).setTitle("Favorite checkbox, not checked");
		}

        MenuItem menuItem_Refresh = menu.add(1, MENU_ITEM_REFRESH, menu.size(), R.string.description_refresh);
        MenuItemCompat.setShowAsAction(menuItem_Refresh, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
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
                MyLogger.crashlyticsLog("Mountain Passes", "Tap", "MountainPassesActivity star", 1);
				toggleStar(item);
				return true;
			case MENU_ITEM_REFRESH:
                MyLogger.crashlyticsLog("Mountain Passes", "Tap", "MountainPassesActivity refresh", 1);
                mRefreshState = true;
                refresh();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void toggleStar(MenuItem item) {
		Snackbar added_snackbar = Snackbar
				.make(findViewById(R.id.activity_with_tabs), R.string.add_favorite, Snackbar.LENGTH_SHORT);

		Snackbar removed_snackbar = Snackbar
				.make(findViewById(R.id.activity_with_tabs), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

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

		if (mIsStarred) {
			item.setIcon(R.drawable.ic_menu_star);
			item.setTitle("Favorite checkbox, not checked");
            viewModel.setIsStarredFor(mId, 0);
            removed_snackbar.show();
            mIsStarred = false;
		} else {
			item.setIcon(R.drawable.ic_menu_star_on);
			item.setTitle("Favorite checkbox, checked");
            viewModel.setIsStarredFor(mId, 1);
            added_snackbar.show();
            mIsStarred = true;
		}
	}

    protected void refresh() {
        mTabsAdapter.notifyDataSetChanged();
        viewModel.forceRefreshPasses();
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

    }
}
