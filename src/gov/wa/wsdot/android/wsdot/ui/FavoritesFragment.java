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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class FavoritesFragment extends SherlockListFragment {

	private View mLoadingSpinner;
	private View mEmptyView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
            
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Favorites");
	}
	
    @SuppressWarnings("deprecation")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        mEmptyView = root.findViewById( R.id.empty_list_view );

        return root;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		TextView t = (TextView) mEmptyView;
		t.setText(R.string.no_favorites);
		mEmptyView.setVisibility(View.VISIBLE);
	}
    
}
