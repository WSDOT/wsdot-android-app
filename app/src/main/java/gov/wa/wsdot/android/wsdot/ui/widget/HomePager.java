package gov.wa.wsdot.android.wsdot.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by simsl on 2/16/16.
 *
 * A Custom ViewPager used for the home screen.
 *
 * Disables scrolling the ViewPager from right to left when
 * on the last page (favorites fragment) so favorites list can use
 * swipe dismiss for it's items.
 */

public class HomePager extends ViewPager {

    public HomePager(Context context) {
        super(context);
    }

    public HomePager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * If delta x is negative user is scrolling
     */
    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return dx < 0 || super.canScroll(v, checkV, dx, x, y);
    }
}
