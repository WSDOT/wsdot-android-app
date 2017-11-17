package gov.wa.wsdot.android.wsdot.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitNorthboundFragment;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitSouthboundFragment;
import gov.wa.wsdot.android.wsdot.ui.home.DashboardFragment;
import gov.wa.wsdot.android.wsdot.ui.home.HighImpactAlertsFragment;

/**
 *  For Dagger 2. A list of classes passed to the Object Graph that
 *  must be able to inject.
 *
 *  ContributesAndroidInjector lets dagger 2 build a basic injector
 *  for each class.
 */

@Module
public abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract BaseFragment contributeBaseFragment();

    @ContributesAndroidInjector
    abstract BaseListFragment contributeBaseListFragment();

    @ContributesAndroidInjector
    abstract DashboardFragment contributeBaseDashboardFragment();

    @ContributesAndroidInjector
    abstract BorderWaitNorthboundFragment contributeBorderWaitNorthboundFragment();

    @ContributesAndroidInjector
    abstract BorderWaitSouthboundFragment contributeBorderWaitSouthboundFragment();

    @ContributesAndroidInjector
    abstract HighImpactAlertsFragment contributeHighImpactAlertsFragment();
}