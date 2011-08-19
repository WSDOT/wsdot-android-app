/*
 * Copyright (c) 2011 Washington State Department of Transportation
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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class SplashScreen extends Activity {
	private static final String DEBUG_TAG = "SplashScreen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
	    String versionName = "";
	    PackageInfo packageInfo;
		
	    try {
	        packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	    	versionName = "v" + packageInfo.versionName;
	    } catch (NameNotFoundException e) {
	        Log.e(DEBUG_TAG, "Not found", e);
	    }

	    ((TextView)findViewById(R.id.version_name)).setText(versionName);
	    
		new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent intent = new Intent(SplashScreen.this, WSDOT.class);
				SplashScreen.this.startActivity(intent);
				SplashScreen.this.finish();
			}
		}, 2000); // 2 seconds
	}
}
