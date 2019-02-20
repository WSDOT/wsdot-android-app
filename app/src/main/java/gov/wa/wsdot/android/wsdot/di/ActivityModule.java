package gov.wa.wsdot.android.wsdot.di;

/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.alert.detail.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.ui.borderwait.BorderWaitActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraViewPagerActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.FerriesRouteSchedulesActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.FerriesRouteSchedulesDayDeparturesActivity;
import gov.wa.wsdot.android.wsdot.ui.home.HomeActivity;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassesActivity;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem.MountainPassItemActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.newroute.NewRouteActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.report.MyRouteReportActivity;
import gov.wa.wsdot.android.wsdot.ui.notifications.NotificationsActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;

/**
 *  For Dagger 2. A list of classes passed to the Object Graph that
 *  must be able to inject.
 *
 *  ContrubutesAndroidInjector lets dagger 2 build a basic injector
 *  for each class.
 */

@Module
public abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract BaseActivity contributeBaseActivity();

    @ContributesAndroidInjector
    abstract BorderWaitActivity contributeBorderWaitActivity();

    @ContributesAndroidInjector
    abstract HomeActivity contributeHomeActivity();

    @ContributesAndroidInjector
    abstract MountainPassesActivity contributeMountainPassesActivity();

    @ContributesAndroidInjector
    abstract MountainPassItemActivity contributeMountainPassItemActivity();

    @ContributesAndroidInjector
    abstract FerriesRouteSchedulesActivity contributeFerriesRouteSchedulesActivity();

    @ContributesAndroidInjector
    abstract FerriesRouteSchedulesDayDeparturesActivity contributeFerriesRouteSchedulesDayDeparturesActivity();

    @ContributesAndroidInjector
    abstract CameraActivity contributeCameraActivity();

    @ContributesAndroidInjector
    abstract TrafficMapActivity contributeTrafficMapActivity();

    @ContributesAndroidInjector
    abstract NewRouteActivity contributeNewRouteActivity();

    @ContributesAndroidInjector
    abstract HighwayAlertDetailsActivity contributeHighwayAlertDetailsActivity();

    @ContributesAndroidInjector
    abstract MyRouteReportActivity contrubuteMyRouteReportActivity();

    @ContributesAndroidInjector
    abstract CameraViewPagerActivity contributeCameraViewPagerActivity();

    @ContributesAndroidInjector
    abstract NotificationsActivity contributeNotificationsActivity();

}