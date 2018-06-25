package gov.wa.wsdot.android.wsdot.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.BaseListFragment;
import gov.wa.wsdot.android.wsdot.ui.amtrakcascades.AmtrakCascadesSchedulesDetailsFragment;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitNorthboundFragment;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitSouthboundFragment;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraImageFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.bulletins.FerriesRouteAlertsBulletinDetailsFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.bulletins.FerriesRouteAlertsBulletinsFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.sailings.departures.FerriesRouteSchedulesDayDeparturesFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.sailings.departures.FerriesTerminalCameraFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.sailings.FerriesRouteSchedulesDaySailingsFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.FerriesRouteSchedulesFragment;
import gov.wa.wsdot.android.wsdot.ui.home.DashboardFragment;
import gov.wa.wsdot.android.wsdot.ui.home.FavoritesFragment;
import gov.wa.wsdot.android.wsdot.ui.home.HighImpactAlertsFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassesFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem.MountainPassItemCameraFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem.MountainPassItemForecastFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem.MountainPassItemReportFragment;
import gov.wa.wsdot.android.wsdot.ui.myroute.MyRouteFragment;
import gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts.MyRouteAlertsListFragment;
import gov.wa.wsdot.android.wsdot.ui.notifications.NotificationsFragment;
import gov.wa.wsdot.android.wsdot.ui.tollrates.I405TollRatesFragment;
import gov.wa.wsdot.android.wsdot.ui.tollrates.SR167TollRatesFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.alertsinarea.HighwayAlertListFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.expresslanes.SeattleExpressLanesFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.news.NewsFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.blog.BlogFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.facebook.FacebookFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.twitter.TwitterFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.youtube.YouTubeFragment;
import gov.wa.wsdot.android.wsdot.ui.traveltimes.TravelTimesFragment;

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

    @ContributesAndroidInjector
    abstract HighwayAlertListFragment contributeAlertsListFragment();

    @ContributesAndroidInjector
    abstract TravelTimesFragment contributeTravelTimesFragment();

    @ContributesAndroidInjector
    abstract SeattleExpressLanesFragment contributeSeattleExpressLanesFragment();

    @ContributesAndroidInjector
    abstract NewsFragment contributeNewsFragment();

    @ContributesAndroidInjector
    abstract TwitterFragment contributeTwitterFragment();

    @ContributesAndroidInjector
    abstract FacebookFragment contributeFacebookFragment();

    @ContributesAndroidInjector
    abstract BlogFragment contributeBlogFragment();

    @ContributesAndroidInjector
    abstract YouTubeFragment contributeYouTubeFragment();

    @ContributesAndroidInjector
    abstract AmtrakCascadesSchedulesDetailsFragment contributeAmtrakCascadesSchedulesDetailsFragment();

    @ContributesAndroidInjector
    abstract MyRouteAlertsListFragment contributeMyRouteAlertsListFragment();

    @ContributesAndroidInjector
    abstract MyRouteFragment contributeMyRouteFragment();

    @ContributesAndroidInjector
    abstract FavoritesFragment contributeFavoritesFragment();

    @ContributesAndroidInjector
    abstract NotificationsFragment contributeNotificationsFragment();

    @ContributesAndroidInjector
    abstract I405TollRatesFragment contributeI405TollRatesFragmentFragment();

    @ContributesAndroidInjector
    abstract SR167TollRatesFragment contributeSR167TollRatesFragmentFragment();

}