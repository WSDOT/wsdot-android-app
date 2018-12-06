package gov.wa.wsdot.android.wsdot.ui.widget;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by simsl on 2/16/16.
 *
 * A Custom ViewPager used for the home screen.
 *
 * This class helps allow swipe dismissal of favorite items to
 * function correctly.
 *
 */

public class HomePager extends ViewPager {

    public HomePager(Context context) {
        super(context);
    }

    public HomePager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Gives the right-to-left scroll event to it's child view if that view is a recycler view.
     * If there are no children views HomePager takes it.
     */
    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {

        if ((dx < 0) && v instanceof RecyclerView){
            return true;
        }

        return super.canScroll(v, checkV, dx, x, y);
    }
}
