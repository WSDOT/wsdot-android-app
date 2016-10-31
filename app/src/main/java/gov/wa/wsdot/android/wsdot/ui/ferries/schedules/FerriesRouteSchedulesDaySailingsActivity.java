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

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesSchedules;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;

public class FerriesRouteSchedulesDaySailingsActivity extends BaseActivity {
	
	private boolean mIsStarred;
	private ContentResolver resolver;
	private int mId;
	private Toolbar mToolbar;
    private String mTitle;

	static final private int MENU_ITEM_STAR = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_ferries_route_schedules_day_sailings);

		Bundle args = getIntent().getExtras();
		mId = args.getInt("id");
        mTitle = args.getString("title");

        if (savedInstanceState == null) {
            mIsStarred = args.getInt("isStarred") != 0;
        }else{
            mIsStarred = savedInstanceState.getInt("isStarred") != 0;
        }

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle(mTitle);
		setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
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
		}

		return super.onOptionsItemSelected(item);
	}

	private void toggleStar(MenuItem item) {

		resolver = getContentResolver();

		Snackbar added_snackbar = Snackbar
				.make(findViewById(R.id.day_sailings), "Added to favorites", Snackbar.LENGTH_SHORT);

		Snackbar removed_snackbar = Snackbar
				.make(findViewById(R.id.day_sailings), "Removed from favorites", Snackbar.LENGTH_SHORT);

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
				values.put(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, 0);
				resolver.update(
                        FerriesSchedules.CONTENT_URI,
                        values,
                        FerriesSchedules.FERRIES_SCHEDULE_ID + "=?",
                        new String[]{Integer.toString(mId)}
                );

				removed_snackbar.show();
				mIsStarred = false;
	    	} catch (Exception e) {
	    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("DaySailings", "Error: " + e.getMessage());
	    	}
		} else {
			item.setIcon(R.drawable.ic_menu_star_on);
			item.setTitle("Favorite checkbox, checked");
			try {
				ContentValues values = new ContentValues();
				values.put(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, 1);
				resolver.update(
                        FerriesSchedules.CONTENT_URI,
                        values,
                        FerriesSchedules.FERRIES_SCHEDULE_ID + "=?",
                        new String[]{Integer.toString(mId)}
                );

				added_snackbar.show();
				mIsStarred = true;
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("DaySailings", "Error: " + e.getMessage());
	    	}
		}

	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mIsStarred){
            outState.putInt("isStarred", 1);
        }else{
            outState.putInt("isStarred", 0);
        }
        super.onSaveInstanceState(outState);
    }
}
