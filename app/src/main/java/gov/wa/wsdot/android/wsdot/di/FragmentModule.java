package gov.wa.wsdot.android.wsdot.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitNorthboundFragment;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitSouthboundFragment;

@Module
public abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract BaseFragment contributeBaseFragment();

    @ContributesAndroidInjector
    abstract BaseListFragment contributeBaseListFragment();

    @ContributesAndroidInjector
    abstract BorderWaitNorthboundFragment contributeBorderWaitNorthboundFragment();

    @ContributesAndroidInjector
    abstract BorderWaitSouthboundFragment contributeBorderWaitSouthboundFragment();
}