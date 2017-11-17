package gov.wa.wsdot.android.wsdot.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import gov.wa.wsdot.android.wsdot.ui.alert.HighwayAlertViewModel;
import gov.wa.wsdot.android.wsdot.viewmodal.ViewModelFactory;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitViewModel;

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
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);
}