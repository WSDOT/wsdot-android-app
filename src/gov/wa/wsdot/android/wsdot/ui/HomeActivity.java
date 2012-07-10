package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class HomeActivity extends SherlockFragmentActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.options, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
            	startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.menu_preferences:
                startActivity(new Intent(this, PreferencesActivity.class));
                break;
                
            case R.id.menu_favorites:
            	startActivity(new Intent(this, FavoritesActivity.class));
            	break;
        }
        return super.onOptionsItemSelected(item);
    }
}