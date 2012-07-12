package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

public class HomeActivity extends SherlockFragmentActivity {

    private ViewPagerAdapter mAdapter;
    private static ViewPager mPager;
    private TitlePageIndicator mIndicator;
	private static final String[] mPageTitles = new String[] { "Home", "Favorites" };
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAdapter = new ViewPagerAdapter();
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(0); // Defaults to 0 anyways.

        mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
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
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class ViewPagerAdapter extends PagerAdapter {

        public int getCount() {
                return 2;
        }

        public Object instantiateItem(View collection, int position) {

                LayoutInflater inflater = (LayoutInflater) collection.getContext()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                int resId = 0;
                switch (position) {
                case 0:
                    resId = R.layout.activity_dashboard;
                    break;
                case 1:
                    resId = R.layout.activity_favorites;
                    break;
                }

                View view = inflater.inflate(resId, null);

                ((ViewPager) collection).addView(view, 0);

                return view;
        }

        @Override
		public CharSequence getPageTitle(int position) {
			return mPageTitles[position];
		}
               
		@Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
                ((ViewPager) arg0).removeView((View) arg2);

        }

        @Override
        public void finishUpdate(View arg0) {
                // TODO Auto-generated method stub

        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == ((View) arg1);

        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
                // TODO Auto-generated method stub

        }

        @Override
        public Parcelable saveState() {
                // TODO Auto-generated method stub
                return null;
        }

        @Override
        public void startUpdate(View arg0) {
                // TODO Auto-generated method stub

        }    
    
    }
}