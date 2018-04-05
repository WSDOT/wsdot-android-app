package gov.wa.wsdot.android.wsdot.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import gov.wa.wsdot.android.wsdot.service.TopicSyncService;

@Module
abstract class ServiceModule  {

    @ContributesAndroidInjector
    abstract TopicSyncService contributeTopicSyncService();

}