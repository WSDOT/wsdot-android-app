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