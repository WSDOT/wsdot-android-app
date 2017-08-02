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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

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
    private final Context mContext;
    private final List<Class<? extends Fragment>> mFragmentClasses;
    private final SparseArray<Fragment> mFragments = new SparseArray<>();
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
        mContext = activity;
        mFragmentClasses = tabFrags;
        mArgs = null;

    }

    /**
     * Constructor when fragment needs args.
     */
    public TabsAdapter(BaseActivity activity, List<Class<? extends Fragment>> tabFrags, FragmentManager fm, int NumOfTabs, Bundle args) {
        super(fm);
        mContext = activity;
        mFragmentClasses = tabFrags;
        mArgs = args;
    }

    @Override
    public Fragment getItem(int position) {
        return Fragment.instantiate(mContext,  mFragmentClasses.get(position).getName(), mArgs);
    }

    @Override
    public int getCount() {
        return mFragmentClasses.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public SparseArray<Fragment> getFragments() {
        return mFragments;
    }
}