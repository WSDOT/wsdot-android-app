package gov.wa.wsdot.android.wsdot.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitNorthboundFragment;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitSouthboundFragment;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraImageFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.bulletins.FerriesRouteAlertsBulletinDetailsFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.bulletins.FerriesRouteAlertsBulletinsFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerriesRouteSchedulesDayDeparturesFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerriesTerminalCameraFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.sailings.FerriesRouteSchedulesDaySailingsFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.FerriesRouteSchedulesFragment;
import gov.wa.wsdot.android.wsdot.ui.home.DashboardFragment;
import gov.wa.wsdot.android.wsdot.ui.home.HighImpactAlertsFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassesFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem.MountainPassItemCameraFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem.MountainPassItemForecastFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem.MountainPassItemReportFragment;

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

    @ContributesAndroidInjector
    abstract MountainPassesFragment contributeMountainPassesFragment();

    @ContributesAndroidInjector
    abstract MountainPassItemReportFragment contributeMountainPassItemReportFragment();

    @ContributesAndroidInjector
    abstract MountainPassItemCameraFragment contributeMountainPassItemCameraFragment();

    @ContributesAndroidInjector
    abstract MountainPassItemForecastFragment contributeMountainPassItemForecastFragment();

    @ContributesAndroidInjector
    abstract FerriesRouteSchedulesFragment contributeFerriesRouteSchedulesFragment();

    @ContributesAndroidInjector
    abstract FerriesRouteSchedulesDaySailingsFragment contributeFerriesRouteSchedulesDaySailingsFragment();

    @ContributesAndroidInjector
    abstract FerriesRouteSchedulesDayDeparturesFragment contributeFerriesRouteSchedulesDayDeparturesFragment();

    @ContributesAndroidInjector
    abstract FerriesRouteAlertsBulletinsFragment contributeFerriesRouteAlertsBulletinsFragment();

    @ContributesAndroidInjector
    abstract FerriesRouteAlertsBulletinDetailsFragment contributeFerriesRouteAlertsBulletinDetailsFragment();

    @ContributesAndroidInjector
    abstract FerriesTerminalCameraFragment contributeFerriesTerminalCameraFragment();

    @ContributesAndroidInjector
    abstract CameraImageFragment contributeCameraImageFragment();

}