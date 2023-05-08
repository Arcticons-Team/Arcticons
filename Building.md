# Building the app

**Prerequisites**: Android Studio, python3 and Inkscape installed on your device

1. Place the created icons in the `/other` folder and add the new ComponentInfo codes in the `appfilter.xml`, you can gather it with [icon request](https://github.com/Kaiserdragon2/IconRequest/releases/). (See [CONTRIBUTING.md](CONTRIBUTING.md))
2. Run the `preparerelease.py` script with the appropriate directories and wait until finished.

        python preparerelease.py SVG_DIR APP_SRC_DIR ICONS_DIR

3. Change the `versionCode` and `versionName` in the `/app/build.gradle` file.
4. Open the repo in Android Studio. It will take a while to sync/load everything for the first time.
5. Plug your phone in (allow USB debugging), choose your build flavor and click the green play button to run it on your phone.
6. You can also generate an APK file in Android studio:
    - `Toolbar > Build > Make project (the hammer icon)`
    - For a [normal APK](https://developer.android.com/studio/run/): `Toolbar > Build > Build bundle(s)/APK(s) > Build APK(s)`
    - For a [signed APK](https://developer.android.com/studio/publish/app-signing): `Toolbar > Build > Build bundle(s)/APK(s) > Generate signed bundle/APK`

## Choosing your build flavors

The app is built as a debug version (`com.donnnno.arcticons.debug`) by default. 

To choose another variant: `Build Variants`(on the left sidebar) `> *Active Build Variant* > select from the menu.`

You can also [create your own variant(s)](guides/Create_Variants_Guide.md).