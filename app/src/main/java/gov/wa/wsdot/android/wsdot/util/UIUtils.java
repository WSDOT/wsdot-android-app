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

package gov.wa.wsdot.android.wsdot.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.wa.wsdot.android.wsdot.R;

public class UIUtils {

	/**
	 * Get the correct icon given the priority and category of alert.
	 *
	 * @param category
	 * @param priority
	 * @return
	 */
	static public int getCategoryIcon(String category, String priority) {

		int alertClosed = R.drawable.closed;
		int alertHighest = R.drawable.alert_highest;
		int alertHigh = R.drawable.alert_high;
		int alertMedium = R.drawable.alert_moderate;
		int alertLow = R.drawable.alert_low;
		int constructionHighest = R.drawable.construction_highest;
		int constructionHigh = R.drawable.construction_high;
		int constructionMedium = R.drawable.construction_moderate;
		int constructionLow = R.drawable.construction_low;
		int weather = R.drawable.weather;

		// Types of categories which result in one icon or another being displayed.
		String[] event_closure = {"closed", "closure"};
		String[] event_construction = {"construction", "maintenance", "lane closure"};
		String[] event_road_report = {"road report"};

		HashMap<String, String[]> eventCategories = new HashMap<>();

		eventCategories.put("closure", event_closure);
		eventCategories.put("construction", event_construction);
		eventCategories.put("road report", event_road_report);

		Set<Map.Entry<String, String[]>> set = eventCategories.entrySet();
		Iterator<Map.Entry<String, String[]>> i = set.iterator();

		if (category.equals("")) return alertMedium;

		while(i.hasNext()) {
			Map.Entry<String, String[]> me = i.next();
			for (String phrase: me.getValue()) {
				String patternStr = phrase;
				Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(category);
				boolean matchFound = matcher.find();

				if (matchFound) {
					String keyWord = me.getKey();

					if (keyWord.equalsIgnoreCase("closure")) {
						return alertClosed;
					} else if (keyWord.equalsIgnoreCase("construction")) {
						if (priority.equalsIgnoreCase("highest")) {
							return constructionHighest;
						} else if (priority.equalsIgnoreCase("high")) {
							return constructionHigh;
						} else if (priority.equalsIgnoreCase("medium")) {
							return constructionMedium;
						} else if (priority.equalsIgnoreCase("low")
								|| priority.equalsIgnoreCase("lowest")) {
							return constructionLow;
						}
					} else if (keyWord.equalsIgnoreCase("road report")) {
						return weather;
					}
				}
			}
		}

		// If we arrive here, it must be an accident or alert item.
		if (priority.equalsIgnoreCase("highest")) {
			return alertHighest;
		} else if (priority.equalsIgnoreCase("high")) {
			return alertHigh;
		} else if (priority.equalsIgnoreCase("medium")) {
			return alertMedium;
		} else if (priority.equalsIgnoreCase("low")
				|| priority.equalsIgnoreCase("lowest")) {
			return alertLow;
		}

		return alertMedium;
	}

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
	
}
