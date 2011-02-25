/*
 * Copyright (c) 2010 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class WSDOT extends MainMenu {
	void prepareMenu() {
		addMenuItem("News", News.class);
		addMenuItem("Twitter", Twitter.class);
		addMenuItem("Photos", Photos.class);
		addMenuItem("Blog", Blog.class);
		addMenuItem("Traffic & Travel", TrafficTravel.class);
		addMenuItem("Video", Video.class);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		menu.findItem(R.id.about).setIntent(new Intent(this, About.class));	
		menu.findItem(R.id.preferences).setIntent(new Intent(this, EditPreferences.class));
		super.onCreateOptionsMenu(menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(item.getIntent());
		super.onOptionsItemSelected(item);
		return true;
	}
} 