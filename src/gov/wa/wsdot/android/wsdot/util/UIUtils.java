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

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

public class UIUtils {

    public static boolean isHoneycomb() {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
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
    public static void refreshActionBarMenu(Activity activity) {
        if (isHoneycomb()) {
        	activity.invalidateOptionsMenu();
        }
    }	
	
}
