package gov.wa.wsdot.android.wsdot.ui.myroute.report;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.myroute.MyRouteViewModel;
import gov.wa.wsdot.android.wsdot.ui.myroute.report.alerts.MyRouteAlertsListFragment;
import gov.wa.wsdot.android.wsdot.ui.myroute.report.cameras.MyRouteCamerasListFragment;
import gov.wa.wsdot.android.wsdot.ui.myroute.report.traveltimes.MyRouteTravelTimesFragment;
import gov.wa.wsdot.android.wsdot.ui.traveltimes.TravelTimesFragment;

public class MyRouteReportFragment extends BaseFragment implements Injectable {

    private String TAG = MyRouteReportFragment.class.getSimpleName();
    private long mRouteId = -1;

    private MyRouteViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_tabs, null);

        // Setting ViewPager for each Tabs
        ViewPager viewPager = root.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = root.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(viewPager);

        Bundle args = getActivity().getIntent().getExtras();

        if (args != null) {
            mRouteId = args.getLong("route_id");
        }

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteViewModel.class);

        viewModel.loadMyRoute(mRouteId).observe(this, myRoute -> {
            if (myRoute != null){
                Log.e(TAG, myRoute.getTitle());






            }
        });

        return root;
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {

        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new MyRouteAlertsListFragment(), "Alerts");
        adapter.addFragment(new MyRouteTravelTimesFragment(), "Travel Times");
        adapter.addFragment(new MyRouteCamerasListFragment(), "Cameras");
        viewPager.setAdapter(adapter);

    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
