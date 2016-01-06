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

package gov.wa.wsdot.android.wsdot.util;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.ViewConfiguration;

public class UIUtils {

    public static boolean isHoneycomb() {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    
    public static boolean isIceCreamSandwich() {
    	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
	
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return isHoneycomb() && isTablet(context);
    }
    
	/*
	 * On Android 2.3.x and lower, the system calls onPrepareOptionsMenu() each
	 * time the user opens the options menu (presses the Menu button).
	 * 
	 * On Android 3.0 and higher, the options menu is considered to always be open
	 * when menu items are presented in the action bar. When an event occurs and 
	 * you want to perform a menu update, you must call invalidateOptionsMenu() 
	 * to request that the system call onPrepareOptionsMenu().
	 * 
	 * http://developer.android.com/guide/topics/ui/menus.html#ChangingTheMenu
	 */
    @SuppressLint("NewApi")
	public static void refreshActionBarMenu(Activity activity) {
        if (isHoneycomb()) {
        	activity.invalidateOptionsMenu();
        }
    }
    
    /*
     * Force use of overflow menu on devices with ICS and menu button.
     * 
     * Use Java reflection to access private field in android.view.ViewConfiguration
     * class. Force the app to show the overflow menu. Devices with a menu hardware
     * key will still work, however, it will open the menu in the top right corner
     * rather than at bottom of the screen.
     * 
     * This is a HACK and contrary to what Google suggests in the Compatibility section
     * of the Android Design Guidelines, 
     * 
     * http://developer.android.com/design/patterns/compatibility.html
     * 
     * "Android phones with traditional navigation hardware keys don't display the
     * virtual navigation bar at the bottom of the screen. Instead, the action overflow
     * is available from the menu hardware key. The resulting actions popup has the same
     * style as in the previous example, but is displayed at the bottom of the screen."
     * 
     * In Google's Android Developers blog they have a post titled, "Say Goodbye to the
     * Menu Button",
     * 
     * http://android-developers.blogspot.com/2012/01/say-goodbye-to-menu-button.html
     * 
     * where they tell developers that apps should stop relying on the hardware Menu
     * button and they should stop thinking about activities using a "menu button" at all.
     * Also, "In order to provide the most intuitive and consistent user experience in your
     * apps, you should migrate your designs away from using the Menu button and toward using
     * the action bar."
     * 
     * I believe it was a design mistake not have the menu button act as a redundant "action
     * overflow" button in menu button enabled devices. Having a consistent user experience
     * across all device configurations is a better way to transition the user away from
     * the hardware menu key.
     * 
     */
	public static void setHasPermanentMenuKey(Context context, boolean value) {
		if (isIceCreamSandwich()) {
			try {
				ViewConfiguration config = ViewConfiguration.get(context);
				Field menuKeyField = ViewConfiguration.class
						.getDeclaredField("sHasPermanentMenuKey");
				if (menuKeyField != null) {
					menuKeyField.setAccessible(true);
					menuKeyField.setBoolean(config, value);
				}
			} catch (Exception ex) {
				// Simply ignore. This is a hack anyways.
			}
		}
	}
	
	public static boolean isNetworkAvailable(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = cm.getActiveNetworkInfo();

	    if (networkInfo != null && networkInfo.isConnected()) {
	        return true;
	    }

	    return false;
	}
	
}
