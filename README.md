<img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/Donnnno/Arcticons"> [<img alt="F-Droid" src="https://img.shields.io/f-droid/v/com.donnnno.arcticons">](https://f-droid.org/packages/com.donnnno.arcticons)

<img src='https://raw.githubusercontent.com/Donnnno/frost-lines/main/metadata/en-US/images/header-background.png'>

Arcticons (Arctic icons) is a line-based icon pack forked from [Frost](https://github.com/dkanada/frost) but rebuild with consistent sizing and adjustments specifically for lines in mind. Arcticons is FOSS and provides mostly icons for open-source apps on F-Droid (but there are many other apps in this pack too). The design is simple and clean with minimal lines that give you a nice see-trough effect

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="90">](https://f-droid.org/packages/com.donnnno.arcticons)

Arcticons includes support for Trebuchet, KISS, Nova, Apex, Holo, ZenUI, ADW, and many more launchers. **There are over 1000 icons.**

Submitting icon requests helps a lot, but maintaining the project (as you may imagine) takes a lot of time and effort, so please be patient about adding new icons. Contributions for new icons are always welcome, and no experience with programming is necessary.

<img src='https://raw.githubusercontent.com/Donnnno/frost-lines/main/metadata/en-US/images/screenshots.png'>

## Icon Requests

This app doesn't have an icon request feature, so you'll have to do the following steps.

1. **Install [Turtl](https://f-droid.org/packages/org.xphnx.iconsubmit)** which can extract the icons and class names of your apps.
2. **Grant the storage access permission to Turtl** in your system settings, this will allow it to save the zip file (Only required in Android 6 "Marshmallow" or newer, older versions grant this permission on installation).
3. **Open Turtl** and follow the process to save the new zip file to your phone.
4. **[Make an issue](https://github.com/Donnnno/frost-lines/issues/new)** titled "Icon Request" and attach the zip file.

If Turtl is not working on your device, you can also use [Applications Info](https://f-droid.org/packages/com.majeur.applicationsinfo) or any equivalent app to collect the **package name** of the app ($PACKAGE_NAME) and the **main activity name** of the app ($ACTIVITY_NAME) launchable in Applications Info with no errors. Work is being done to simplify this process.

## Contributing

Help with any aspect of the app is much appreciated! **You don't have to know how to code!** If you know how to work with vectors in a program that supports the SVG format. [Inkscape](https://inkscape.org/en/) is recommended but not necessary. Please read the [guide on contributing](CONTRIBUTING.md) before making big changes! It also includes some personal preferences on how to work with Inkscape. If you don't use Inkscape you can just ignore them.

When you bring an icon to Arcticons try to make it minimal. The easiest and most noticeable way to do so is to set an icon free of its background. Twitter is a good example! There are two parts: a bird and a circle surrounding it. The bird is the center of the icon and the part associated with Twitter while the circle is not unique to the platform and can be removed. Don't take this as a ground rule because there are cases when a background shape is necessary. Adobe products are much more recognizable in a square and GitHub uses the background shape as a meaningful part of their icon.

**Note:** I want to accept as many contributions as possible, but will only merge icons that have been manually created using shapes and paths in a vector editing program. I don't think the quality is high enough from tools that automatically generate vector images.

Quick Guide:
1. Fork the repository to your GitHub account
2. Download the templates provided [here](templates) and design the new icons
3. Upload the SVG icons to your repository's **/other** folder
4. Create a pull request and explain your changes
5. Provide the activity names of the new apps
   - These should look something like `com.donnnno.arcticons/com.donnnno.arcticons.MainActivity`
   - If you want to help even more you can add your new icons to the appfilter.xml file

## Building

Before building the app you will need to convert the vector (SVG) files to rasterized (PNG) files. This can be done automatically by running `make`. If you have a multi-core CPU then you can speed this up by running for example `make -j5` to run 5 processes in parallel.

This will also clean the SVG files, if needed recolor them, and generate the XML files needed for the app to compile. To change the color of the icons, change the corresponding variable in the Makefile. Separate make targets are available: `make raster`, `make cleanall`, `make xml`. Finally, `make clean` removes all generated files.

After this you can compile the app as normal. For example, by importing it into Android Studio and building it.

## Donate
|Paypal|Librapay|
|---|---|
|<a href="https://www.paypal.com/paypalme/onnovdd"><img alt="Donate using Paypal" src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif"></a>|<a href="https://liberapay.com/Donno/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg"></a>|

## The icons

<img src='https://raw.githubusercontent.com/Donnnno/frost-lines/main/metadata/en-US/images/complete-background.png'/>
