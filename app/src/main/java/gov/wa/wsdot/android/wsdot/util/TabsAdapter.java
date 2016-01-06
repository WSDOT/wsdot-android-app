package gov.wa.wsdot.android.wsdot.util;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import gov.wa.wsdot.android.wsdot.ui.BaseActivity;

/**
 * Created by simsl on 1/5/16.
 *
 * Generalized Tabs Adapter for toolbars with tabs.
 * Each tab has a corresponding fragment that will
 * be created when the tab is selected.
 */
public class TabsAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    private final Context mContext;
    private final List<Class<? extends Fragment>> mFragments;
    private final Bundle mArgs;

    /**
     *
     * @param activity The Current activity using tabs.
     * @param tabFrags A list of fragment classes for each tab.
     *                 The index of each fragment in the list MUST correspond to the tab position.
     * @param fm FragmentManager
     * @param NumOfTabs The number of tabs
     */
    public TabsAdapter(BaseActivity activity, List<Class<? extends Fragment>> tabFrags, FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        mContext = activity;
        mFragments = tabFrags;
        mArgs = null;
    }

    /**
     * Constructor when fragment needs args.
     */
    public TabsAdapter(BaseActivity activity, List<Class<? extends Fragment>> tabFrags, FragmentManager fm, int NumOfTabs, Bundle args) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        mContext = activity;
        mFragments = tabFrags;
        mArgs = args;
    }

    @Override
    public Fragment getItem(int position) {
        return Fragment.instantiate(mContext, mFragments.get(position).getName(), mArgs);
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}