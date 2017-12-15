package gov.wa.wsdot.android.wsdot.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.v4.view.ViewCompat;
import android.view.ViewDebug;

import javax.inject.Inject;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts.MyRouteAlertListViewModel;
import gov.wa.wsdot.android.wsdot.ui.myroute.newroute.NewRouteViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.alertsinarea.HighwayAlertListViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.alertsinarea.HighwayAlertViewModel;
import gov.wa.wsdot.android.wsdot.ui.alert.map.MapHighwayAlertViewModel;
import gov.wa.wsdot.android.wsdot.ui.amtrakcascades.AmtrakCascadesSchedulesDetailsViewModel;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitViewModel;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.camera.MapCameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.FerrySchedulesViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.bulletins.FerriesBulletinsViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerryTerminalCameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerryTerminalViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.vesselwatch.VesselWatchViewModel;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.expresslanes.ExpressLanesViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.news.NewsViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.blog.BlogViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.facebook.FacebookViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.twitter.TwitterViewModel;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.youtube.YouTubeViewModel;
import gov.wa.wsdot.android.wsdot.ui.traveltimes.TravelTimesViewModel;
import gov.wa.wsdot.android.wsdot.viewmodal.ViewModelFactory;

/**
 *  For Dagger 2. A list of classes passed to the Object Graph that
 *  must be able to inject.
 */
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(BorderWaitViewModel.class)
    abstract ViewModel bindBorderWaitViewModel(BorderWaitViewModel borderWaitViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HighwayAlertViewModel.class)
    abstract ViewModel bindHighwayAlertViewModel(HighwayAlertViewModel highwayAlertViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MountainPassViewModel.class)
    abstract ViewModel bindMountainPassViewModal(MountainPassViewModel mountainPassViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(FerrySchedulesViewModel.class)
    abstract ViewModel bindFerryScheduelsViewModel(FerrySchedulesViewModel ferrySchedulesViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(FerryTerminalViewModel.class)
    abstract ViewModel bindFerryTerminalSpacesViewModel(FerryTerminalViewModel ferryTerminalSpacesViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(FerriesBulletinsViewModel.class)
    abstract ViewModel bindFerriesBulletinsViewModel(FerriesBulletinsViewModel ferriesBulletinsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(VesselWatchViewModel.class)
    abstract ViewModel bindVesselWatchViewModel(VesselWatchViewModel vesselWatchViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MapCameraViewModel.class)
    abstract ViewModel bindMapCameraViewModel(MapCameraViewModel mapCameraViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(FerryTerminalCameraViewModel.class)
    abstract ViewModel bindFerryTerminalCameraViewModel(FerryTerminalCameraViewModel ferryTerminalCameraViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CameraViewModel.class)
    abstract ViewModel bindCameraViewModel(CameraViewModel cameraViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MapHighwayAlertViewModel.class)
    abstract ViewModel bindMapHighwayAlertViewModel(MapHighwayAlertViewModel mapHighwayAlertViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HighwayAlertListViewModel.class)
    abstract ViewModel bindHighwayAlertsInBoundsViewModel(HighwayAlertListViewModel highwayAlertsInBoundsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(TravelTimesViewModel.class)
    abstract ViewModel bindTravelTimesViewModel(TravelTimesViewModel travelTimesViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ExpressLanesViewModel.class)
    abstract ViewModel bindExpressLanesViewModel(ExpressLanesViewModel expressLanesViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(NewsViewModel.class)
    abstract ViewModel bindNewsViewModel(NewsViewModel newsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(TwitterViewModel.class)
    abstract ViewModel bindTwitterViewModel(TwitterViewModel twitterViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(FacebookViewModel.class)
    abstract ViewModel bindFacebookViewModel(FacebookViewModel facebookViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(BlogViewModel.class)
    abstract ViewModel bindBlogViewModel(BlogViewModel blogViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(YouTubeViewModel.class)
    abstract ViewModel bindYouTubeViewModel(YouTubeViewModel youTubeViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AmtrakCascadesSchedulesDetailsViewModel.class)
    abstract ViewModel bindAmtrakCascadesSchedulesViewModel(AmtrakCascadesSchedulesDetailsViewModel amtrakCascadesSchedulesViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(NewRouteViewModel.class)
    abstract ViewModel bindNewRouteViewModel(NewRouteViewModel newRouteViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MyRouteAlertListViewModel.class)
    abstract ViewModel bindMyRouteAlertListViewModel(MyRouteAlertListViewModel myRouteAlertListViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);
}