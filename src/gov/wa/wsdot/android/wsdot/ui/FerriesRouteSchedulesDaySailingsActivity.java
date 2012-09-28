/*
 * Copyright (c) 2012 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.FerriesSchedules;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class FerriesRouteSchedulesDaySailingsActivity extends SherlockFragmentActivity {
	
	private boolean mIsStarred = false;
	private ContentResolver resolver;
	private int mId;
	
	static final private int MENU_ITEM_STAR = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_ferries_route_schedules_day_sailings);
		
		Bundle args = getIntent().getExtras();
		mId = args.getInt("id");
		String title = args.getString("title");
		mIsStarred = args.getInt("isStarred") != 0;
		
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITEM_STAR, menu.size(), R.string.description_star)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		if (mIsStarred) {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star_on);
		} else {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star);
		}
		
		return super.onPrepareOptionsMenu(menu);
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
		
		if (mIsStarred) {
			item.setIcon(R.drawable.ic_menu_star);
			try {
				ContentValues values = new ContentValues();
				values.put(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, 0);
				resolver.update(
						FerriesSchedules.CONTENT_URI,
						values,
						FerriesSchedules.FERRIES_SCHEDULE_ID + "=?",
						new String[] {Integer.toString(mId)}
						);
				
				Toast.makeText(this, R.string.remove_favorite, Toast.LENGTH_SHORT).show();			
				mIsStarred = false;
	    	} catch (Exception e) {
	    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("FerriesRouteSchedulesDaySailingsActivity", "Error: " + e.getMessage());
	    	}
		} else {
			item.setIcon(R.drawable.ic_menu_star_on);
			try {
				ContentValues values = new ContentValues();
				values.put(FerriesSchedules.FERRIES_SCHEDULE_IS_STARRED, 1);
				resolver.update(
						FerriesSchedules.CONTENT_URI,
						values,
						FerriesSchedules.FERRIES_SCHEDULE_ID + "=?",
						new String[] {Integer.toString(mId)}
						);			
				
				Toast.makeText(this, R.string.add_favorite, Toast.LENGTH_SHORT).show();
				mIsStarred = true;
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("FerriesRouteSchedulesDaySailingsActivity", "Error: " + e.getMessage());
	    	}
		}		
	}
}
