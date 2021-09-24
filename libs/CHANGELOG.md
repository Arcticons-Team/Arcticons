# v3.13.0
- Added support for previewing Kustom presets
- Fixed `Broken drawable` in bug reporter

# v3.12.1
- Use English date in icon requests' ZIP name
- Fix issue with launcher apply
- Upgraded to OneSignal 4

# v3.12.0
- Update Gradle and AGP
- Added Hyperion launcher
- Use CRLF in email related strings
- Added support to search wallpapers

# v3.11.7
- Fixed crash
- Added basic Kustom support
- Improved themes

# v3.11.6
- Fixed icon search not working

# v3.11.5
- Fixed icon bookmarking not working when `show_icon_name` is set to false

# v3.11.4
- Added support for bookmarking icons

# v3.11.1
- Fixed link not working in markup

# v3.11.0
- Internal code refactorings
- Removed deprecated stuffs
- Now uses Material Components

# v3.10.1
- Fixed in-app billings
- Fixed icon request limit not resetting when license checker is enabled

# v3.10.0
- Updated Gradle wrapper and plugin
- Migrated away from JCenter
- Added Hungarian language
- Now launches clock app when tapped on the clock widget
- Updated to In-app Billing v3
- Added `System default` icon shape
- Fixed icon shape of the random icon viewer
- Improved wallpaper sorting
- Update FAQs

# v3.9.2
- Add Ukrainian language
- Updated translations
- Updated Gradle wrapper and plugin
- Now hides home title if it's empty
- Upgrade storage system to support Android 11
- Now targets Android 11

# v3.9.1
- Fix Gmail not showing up when sending icon request in Android 11

# v3.9.0
- Fix new wallpaper count badge
- Add Indonesian language
- Added option to set default theme
- Now shows CandyBar version in the About section
- Remove duplicates in icon search, fixed back press behaviour
- Now sorts wallpapers in ascending order
- Fixed locale not applying after a relaunch
- Added configuration to customize email body
- Updated translations

# v3.8.1
- Brought back support for Android Jelly Bean
- Fixed issue with premium request email
- Minor fixes

# v3.8.0
- Rewrote configuration JSON checkers
- Added option to use different email, method or Arctic API key
  for regular and premium icon requests
- Fixed Flick Launcher applier
- Added support for auto light-dark mode
- Fixed issues with icon requests
- Fixed the wallpaper loading issue
- Updated translations
- Improved documentation comments
- Improved wiki documentation
- Lots of minor fixes

# v3.7.4
- Updated translations
- Fix unable to disable regular icon request
- Fix error when building icon requests on some devices

# v3.7.2
- Fix icon name generation

# v3.7.0
- Added Czech language
- Updated translations
- Updated Gradle
- Improved Adaptive Icon support
- Bumped minSdk to 21. Now the minimum supported Android version is 5.0 (Lollipop)
- Minor design refreshment
- Migrated to Glide
- Improved fast scrollbar for icons section
- Removed UIL completely
- Now properly fixes icon name in Icon Requests
- Redesigned Apply section
- Redesigned Wallpaper section
- Added Niagara and Square launcher
- Fixed Microsoft launcher apply behaviour

# v3.6.3
- Updated Gradle Plugin.
- Option to hide missing app count.
- Fixed new wallpaper count badge.
- Updated translations.
- Added new translations.

# v3.6.2
- Support for customizing background color of navigation view header.
- Fixed white status bar showing on pre Android Oreo devices.
- Fixed proguard error.
- Fixed icons being count even when automatic icon counting is disabled.
- Now app names in icon request are normalized, Ex. `ů` is normalized to `u`.

# v3.6.0
- Better icon generation for requests.
- Improved icon preview.
- Added support for [Arctic Manager](https://arcticmanager.com).
- Updated Gradle plugin.
- Removed app lock.
- Updated translations.
- Minor Fixes.

# v3.5.0-beta.6.7
- Fixed Icon Request Limit not resetting after update.
- Updated Gradle Versions.
- Fixed Crash on Tablets.
- Added Better Support for OnePlus Launcher.
- Added BlackBerry Launcher.
- Added Flick Launcher.


# v3.5.0-beta.6.6
- Fix wallpaper download issue with Android 10.
- Show Loader while icon are being loaded.
- Minor Fixes

# v3.5.0-beta.6.5
- Fix Playstore check.
- New Outline Styled Card.
- Hide `Reset Tutorial` in Settings if `show_intro` disabled.
- Minor Fixes.
- Fix Statusbar Color.

# v3.5.0-beta.6.4
- Enabled MultiDex
- Support for Custom Drawable name, like this:
```xml
<item drawable="my_icon" name="Custom Name"/>
```
- Fixed Icon Search
- Support for Android 10
- Lots of Fixes

# v3.5.0-beta.6.3
- Now you can exclude launchers.
- Fixed Many Small Bugs.


# v3.5.0-beta.6.1
- Added support for Adaptive Icons.
- Updated Gradle.
- Fixed error when rebuilding Premium Request.
- New NavigationView.
- Updated Translations.

# v3.5.0-beta.6
- Added Ability to request app from non-mailing apps.
- Added option to Disable Icon Request from JSON stored in Cloud.
- Now shows Request Available, Used even for Regular Requests.
- Option to Close app if installed outside of Playstore.
- Option to Enable/Disable AppUpdater and Checking JSON Before Requesting.
- Uses New Muzei API.
- Now you can set Your Own Email Subject for Icon Requests.
- Now it Loads Right Icon and Name from ComponentName instead of PackageName.
- Fix to AndroidManifest.xml.
- Updated Translators List.
- Updated Italian Translations by Ciao Studio.
- Added POCO, Posidon, Pixel Launcher. Minor Fixes to Old Launchers.
- Added Support for Desktop Shortcuts(Sesame Shortcuts too).
- Added Boolean to Disable Intros.
- Fix Crash on Pre-Oreo Devices.
- Better Stability.
- Added Analog Clock Widget.

# v3.5.0-b5
- Updated Gradle Distribution and Gradle Plugin
- Added Adaptive Icon Generation for Requests
- Support for Android P
- Use of New JSON Format for Wallpaper
- Added Lawnchair Launcher to Apply Section
- Added Turkish and Traditional Chinese Languages
- Support for OneSignal Notifications
- Support for [AppUpdater](https://github.com/javiersantos/AppUpdater)
- Option to Edit Links of "Rate and Review" and "Share". Now You can Even Remove Them
- Migrate to AndroidX
- Improved Adaptive Icon Generation
- Improved Icon Request App Image Quality
- Update CandyBar Translators List
- Added Support for Linking "Privacy Policy" and "Terms and Conditions"
- Now shows from where the app installed in request
