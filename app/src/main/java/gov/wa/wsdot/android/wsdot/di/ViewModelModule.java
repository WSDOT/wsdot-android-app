package gov.wa.wsdot.android.wsdot.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import gov.wa.wsdot.android.wsdot.viewmodal.ViewModelFactory;
import gov.wa.wsdot.android.wsdot.viewmodal.borderwait.BorderWaitViewModel;

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(BorderWaitViewModel.class)
    abstract ViewModel bindBorderWaitViewModel(BorderWaitViewModel borderWaitViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);
}