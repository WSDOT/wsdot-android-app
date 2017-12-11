package gov.wa.wsdot.android.wsdot.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import gov.wa.wsdot.android.wsdot.ui.alert.HighwayAlertViewModel;
import gov.wa.wsdot.android.wsdot.ui.alert.HighwayAlertsInBoundsViewModel;
import gov.wa.wsdot.android.wsdot.ui.alert.MapHighwayAlertViewModel;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitViewModel;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.camera.MapCameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.FerrySchedulesViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.bulletins.FerriesBulletinsViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerryTerminalCameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerryTerminalViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.vesselwatch.VesselWatchViewModel;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassViewModel;
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
    @ViewModelKey(HighwayAlertsInBoundsViewModel.class)
    abstract ViewModel bindHighwayAlertsInBoundsViewModel(HighwayAlertsInBoundsViewModel highwayAlertsInBoundsViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);
}