# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [5.13.4] - 2019-03-28

# Changes
- updates worker manager to 2.0.0

## [5.13.3] - 2019-03-27

# Fixes
- Border wait signs for southbound.

## [5.13.1] - 2019-03-20

# Changes
- My routes update. Now shows a route report with alerts, travel times, and cameras.
- removes adding favorites on route.

## [5.12.2] - 2019-02-25

# Fixes
- crash when changing favorite border waits order

## [5.12.1] - 2019-02-19

# Adds
- Border waits to favorites list

## [5.11.6] - 2019-02-07

# Fixes
- event banner display issues

## [5.11.5] - 2019-02-05

# Fixes
- notification issue

## [5.11.4] - 2019-01-17

# Adds
- performace monitoring lib

# Changes
- updates worker lib

# Fixes
- issue with new ferry schedule date format

## [5.11.3]

# Changes
- Migrates support libs to AndroidX

## [5.11.1] - 2019-01-02

# Changes
- Moves ferry alert bulletin button onto the sailings screen app bar.

## [5.10.1] - 2018-12-17

# Changes
- Migrated to Firebase Analytics.
- Removes unsupported view pager library in favor of native solution.

## [5.9.3] - 2018-11-26

# Fixes
- Display error causing ad banner to cover content.

## [5.9.2] - 2018-11-14

# Changes
- Removes ViewModel processing work done on main thread to workers.
- Refactored various sections to improve performance.

## [5.8.6] - 2018-10-26

# Fixes
- Adds network security config for http camera links on Android 9.

## [5.8.4] - 2018-10-24 

# Fixes
- Crash when loading map on Android 9.

## [5.8.3] - 2018-10-23 

# Added
- ETA and actual departure times to ferries schedule.
- My location button to vessel watch.
- milepost to cameras.

# Fixes
- Vessel Watch opens to correct location for San Juan Island routes.

## [5.8.1] - 2018-09-17

# Added
- Location based sailing selection.

# Changed
- Ferries section navigation hierarchy.
- Sailing screen has been reduced to a spinner on the departures screen. 

# Removed
- Ferries home screen.

## [5.7.0] - 2018-08-15

# Changed
- Removed Static Map API calls in favor of Maps SDK lite mode. New quota on the static maps API prompted this change. The maps SDK for mobile has unlimited use. 

## [5.6.1] - 2018-08-15

# Added
- Analytics for push notification open events
- Everett to the go to location menu

# Changed
- I-405 and SR 167 now display current toll rates. Users can favorite these rates.
- Increased range for ferry terminal cameras, added logic to ensure only ferry cameras are collected.

## [5.5] - unrelased tolling beta

## [5.4.1] - 2018-06-18

## Changed
- Ferries section updated to handle new date format coming in the future. The current format is a .NET style date string returned from the API. The new format that will be added in the future is "yyyy-MM-dd hh:mm a"

## [5.4.0] - 2018-06-07

## Added
- Push notification event tracking

## Fixed
- Express lanes logic no longer expects exactly two status. (I-90 closed)
- Updates analytics event labeling

## [5.3.9] - 2018-05-30

## Fixed
- null Ad identifier after updating to AAPT 2.

## [5.3.8] - 2018-05-29

## Fixed
- Blogger feed. Now uses Blogger API v3.0.

## [5.3.6] - 2018-05-16

## Changed
- Removes leading zero from departure times.

## Fixed
- Crash caused by accessing a null Google Map.

## [5.3.3] - 2018-05-07

## Fixed
- Analytics labeling.
- Null crash in ferries section.

## [5.3.1] - 2018-05-03

## Fixed
- Auto google analytics paths.
- Timing crash with tap target view.

## [5.3.0] - 2018-05-02

## Added
- Adds FCM push notifications.

## [5.2.6] - 2018-04

## Added
- Adds WSF information agent contact info.

## [5.2.4] - 2018-03-12

## Added
- Users can now swipe between cameras in their favorites list.

## [5.2.2] - 2018-03-06

## Fixed
- Crash in DB migration 6 to 7 introduced in last update. 

## [5.2.1] - 2018-02-27

## Added
- New travel times layout. Travel times are no grouped by start and end location, with different routes included in the group.

## [5.1.2] - 2018-02-14

## Fixed
- Crash when renaming saved routes.

## [5.1.1] - 2018-02-12

## Fixed
- Crash in favorites caused by a variable not being setup in time. 

## [5.1.0] - 2018-02-12

## Added
- Past sailings for the current day now display.
- Events section.

## [5.0.9] - 2018-01-23

## Added
- Custom logs for Crashlytics
- Alerts refresh timer on traffic map

## Fixed
- Amtrak Cascades schedule crash
- Vessel Watch - view model access in timer crash
- Error in "Alerts in this area"
- xml error causing My Routes guide to show in settings menu.

## [5.0.6] - 2018-01-18

## Added
- Firebase Crashlytics

## [5.0.5] - 2018-01-17

## Added
- MVVM pattern. (Model, View, View Model)
- Room persistence library.
- Border wait and ferries view model test.
- Better My Routes instructions.

## [5.0.0-4] - unreleased builds

## [4.11.3] - 2017-09-19

## Fixed
- Crash caused by referencing id not in travel charts fragment view.
- Updated US 97 camera url.

## [4.11.2] - 2017-10-18

## Added
- New Amtrak train numbers.

## Changed
- Moved My Routes button to home screen.
- Updated to Gradle 3.0.0

## [4.11.1] - 2017-09-19

## Fixed
- Happening now section Google Analytics

## [4.11.0] - 2017-09-19

## Changed
- Social media feeds moved into "Happening Now" section accessible from the traffic map.
- Map layer settings added to FAB menu for the traffic map

## Removed
- JBLM callout.
- Flickr feed.
- Jobs and Good To Go twitter feeds.

## [4.10.5] - 2017-09-07
## Changed
- Updated app with new WSDOT api key.

## [4.10.4] - 2017-28-07
## Added 
- HERO report contact info.
## Changed
- Added "traffic" advertisement topic.

## [4.10.3] - unreleased

## [4.10.2] - 2017-08-07
## Changed
- Made My Routes tracking service a foreground service. This prevents Android (especially Android O) from reducing location updates or killing the service while routes are recording.

## [4.10.1] - 2017-08-02
## Added
- Travel Charts section. App checks data from a JSON file, if there are travel charts a new app bar item will be created that opens to a menu with the travel chart information form the JSON file.

## [4.9.2] - 2017-06-28
## Changed
- Updated Toll Rates for 2017

## [4.9.1] - 2017-06-26
## Added
- Support for targeted advertisements 

## [4.8.6] - 2017-05-08
## Added
- MyGoodToGo.com link in Toll Rates.

## Changed
- Updated mission statement. 

## [4.8.5] - 2017-04-24
## Fixed
- Potential crash caused by calling unregisterReceiver() twice in AlertsListFragment.

## [4.8.4] - 2017-04-20
## Fixed
- Crash caused by trying to update the map camera location before the map loaded in NewRouteActivity
- Crash in the AlertsListFragment caused by a invalid cursor.

## [4.8.3] - 2017-04-17
## Fixed
- Added null check for refresh icon animation for pass reports. App saves refresh state on configuration change to add animation again, but still seeing crashes.

## [4.8.2] - 2017-04-13
## Fixed
- Temp fix for crash caused by tap target view.

## [4.8.1] - 2017-04-11
## Fixed
- Analytics for My Route section.

## [4.8.0] - 2017-04-10
## Added
- My Routes Feature. Lets users log their routes to receive a list of active alerts on their route auto populate their favorites.
- Option to sort favorites lists.

## [4.7.2] - 2017-02-28
## Added
- Option to toggle JBLM marker on/off.

## [4.7.1] - 2017-02-27
### Fixed
- timestamp issue

## [4.7.0] - 2017-02-21
### Added
- Refresh button to pass report page.
- Tip View for traffic map overflow menu.
- Version number to feedback email

### Changed
- Link to Ferry reservation site now uses mobile link.

### Fixed
- Fixed timestamp time zone issue.

## [4.6.2] - 2017-01-18
### Added
- WSDOT East Twitter Account.

## [4.6.1] - 2016-12-05
### Changed
- Updated Play Services. 

### Fixed
- Index out of bounds exception when viewing a cameras list caused by threading issues. 
- Crash when rotating the app while requesting permissions in the Amtrak Cascades and Vessel Watch sections.
- Updated front loaded pass camera data.

## [4.6.0] - 2016-11-30
### Added
- Can now turn the traffic alerts map overlay off.
- Alert dialog when app detects speeds above ~20mph. Reminds user to not use the app while driving.

### Fixed
- Crash when rotating the app while requesting permissions on the Traffic Map. 

## [4.5.0] - 2016-11-15
### Added
- Traffic Map camera clustering.
- Added Firebase analytics and crash reporting.

## [4.4.5] - 2016-10-31
### Added
- Better Talk Back support for Ferries, Mountain Passes and Amtrak Cascades sections.
- WSDOT North Traffic Twitter account.

### Changed
- Updated Google Play Services and Android Support Libraries.
- Bumped minSDK to 14. Dropping Support for Android < 4.0.
- New YouTube icon.

## [4.4.4] - 2016-09-28
### Changed
- Updated permissions in manifest for new [location policy.](https://developer.android.com/guide/topics/location/strategies.html#Permission)

### Fixed
- More weather phrases for icon matching in mountain pass weather reports.
- Missing weather icons when reports did not include forecast in the first sentence. 

## [4.4.3] - 2016-09-15
### Fixed
- Missing last train of the day in Amtrak Schedules.
- Camera Toolbar icon in Vessel Watch will no longer get out of sync with actual camera display setting.

## [4.4.2] - 2016-08-17
### Fixed
- Issue causing sailing spaces for some Anacortes routes to be unavailable.

## [4.4.1] - 2016-08-01
### Fixed
- Null pointer exception in `TrafficMapActivity$RestAreasOverlayTask.onPostExecute`. Logic allowed markers to be place before map was ready.

## [4.4.0] - 2016-07-13
### Added
- Rest Area map overlay.

### Changed 
- New icons. Camera icon now changes appearance depending on camera visibility.

## [4.3.1] - 2016-07-06
### Changed
- New toll rates effective July 1, 2016.

## [4.3.0] - 2016-06-22
### Added
- Favorite Map Locations.

### Fixed
- App crashes when viewing ferry departures from a new schedule. No longer lets users select a day with no data. 
  See this [issue](https://github.com/WSDOT/wsdot-wsf-schedule/issues/2) in our wsdot-wsf-schedule repo.

## [4.2.1] - 2016-04-21
- Edited Google Analytics tracking name for Alerts in this Area.

## [4.2.0] - 2016-04-19
### Added
- WSDOTjobs twitter account to Twitter feeds.

### Changed
- Seattle Alerts feature has been removed. Replacing it is "Alerts in This Area".

### Fixed
- Appbar star state saved on configuration change.
- "&"s now correctly display in twitter feeds.

## [4.1.3] - 2016-03-18
### Fixed
- The first train departures of the day now display.
- App no longer crashes when rotating device while viewing train departures with no destination set.

## [4.1.2] - 2016-03-16
### Fixed
- Ferry camera images disappearing on orientation change.
- App bar title now set to the pass name when viewing pass reports.
- Added Amtrack trains 502 and 504.

## [4.1.0] - 2016-02-19
### Added
- Updated favorites section. Now supports 'swipe-to-dismiss' gesture to remove starred items.
- Added link to the Express Lanes schedule.

## [4.0.0] - 2016-02-08
### Added
- New Material Design look and feel.

### Known Issues
- resource IDs changed, if these IDs were stored in database app cashes when trying to get resource.

## [3.5.2] - 2015-12-30
### Changed
- Updated Google support library. Hopefully this fixes crashes for users with Samsung 4.2 devices.

## [3.5.1] - 2015-12-29
### Changed
- Social Media - tapping details for Blogger, Facebook, News and Twitter now redirect you to the respective sites.
- Updated analytics code.

## [3.5.0] - 2015-12-17
### Added
- Better analytics integration and reporting.

### Fixed
- Ferries Schedules - sailing times between midnight and 3:00 a.m. no longer disappear.

## [3.4.2] - 2015-12-11
### Fixed
- App crashing when accessing the Traffic Map.

## [3.4.1] - 2015-12-09
### Fixed
- App crashing when accessing Amtrak Cascades schedules.

## [3.4.0] - 2015-12-08
### Added
New Amtrak Cascades activity - check schedules and status of trains.
- Added available cameras at each terminal to Ferries schedule departure times.

### Fixed
Spinning status indicator not showing on views.
WSDOT YouTube video feed not working.

## [3.3.2] - 2015-11-05

## [3.3.1] - 2015-11-05
### Fixed
- "Can't install error code: -103" message some users were reporting.

## [3.3.0] - 2015-11-04
### Changed
- Improved ad location and experience.

### Fixed
- Fix for app crashing on Android 6.0 (Marshmallow) devices when accessing Traffic and Vessel Watch maps.

### Known Issues
"Can't install" error code: -103 - We're aware of this and looking into a fix

## [3.2.6] - 2015-09-30
### Added
- Provide basic information about the I-405 toll rates.
- The toll rates are set by congestion levels which can change frequently and often. We are looking into what would be needed to provide real time rates.

### Known Issues
Android 6.0
- If app crashes when accessing the maps, check that "Location" is enabled in "Settings > Apps > WSDOT > Permissions"
- Fix will be out the week of October 26th

## [3.2.5] - 2015-09-01
### Fixed
- Minor bug fixes

## [3.2.4] - 2015-08-24
### Added
- Joint Base Lewis-McChord (JBLM) traffic flow map

## [3.2.3] - 2015-07-23
### Fixed
- error causing app to crash on startup for those with favorited Ferry Route Schedules. Also fixes crash for some when first accessing Ferry Route Schedule page.

## [3.2.2] - 2015-07-22
### Added
- Ferry crossing times to Route Schedules and Favorites display.

### Fixed
- Fix layout alignment of favorite star on Ferries Route Schedules page.

### Changed
- Updated new toll rates as of July 1, 2015.

## [3.2.1] - 2015-03-24
### Fixed
- white text on grey background issue.

## [3.2.0] - 2015-03-10
### Changed
- Return of the native app for those who had the 3.1.3 - 3.1.6 versions previously.
- Build updated to use Android 5.0.1 (Lollipop).

### Removed
- Amtrak Cascades activity will return in a future update shortly.

## [3.1.6] - 2015-03-06
### Fixed 
- blank menu icons on devices below Android 4.4 (KitKat)

## [3.1.5] - 2015-03-05
### Added 
- mobile platform and version to contact email subject.

### Fixed
- blank high impact alerts box on initial install.

## [3.1.4] - 2015-03-04
### Fixed
- traffic videos (yellow camera images) play now.

## [3.1.3] - 2015-03-02
### Added
- Amtrak Cascades train schedules and status.

### Changed
- Updated look and feel of the app.
- Colored traffic lines on the map are thicker and should be easier to see.

[Unreleased]: 
