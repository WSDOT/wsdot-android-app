package gov.wa.wsdot.android.wsdot.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;

/**
 *  Root component for Dagger 2.
 *  Assembles other components in graph
 */
@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        AppModule.class,
        ActivityModule.class,
        FragmentModule.class,
        ServiceModule.class
})

public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance Builder application(Application application);
        AppComponent build();
    }
    void inject(WsdotApplication wsdotApp);
}