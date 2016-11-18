# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

### Added
- Alert dialog when app detects speeds above ~20mph. Reminds user to not use the app while driving.

### Fixed
- Crash when rotating the app while requesting permissions. 

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
