package com.donnnno.arcticons.applications;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.donnnno.arcticons.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.Request;

public class CandyBar extends CandyBarApplication {

    @NonNull
    @Override
    public Class<?> getDrawableClass() {
        return R.drawable.class;
    }

    @NonNull
    @Override
    public Configuration onInit() {
        Configuration configuration = new Configuration();

        configuration.setGenerateAppFilter(true);

        configuration.setEmailBodyGenerator(requests -> {
            PackageManager packageManager = getApplicationContext().getPackageManager();
            StringBuilder emailBody = new StringBuilder();
            boolean first = true;

            for (Request request : requests) {
                if (!first) {
                    emailBody.append("\r\n\r\n");
                } else {
                    first = false;
                }

                    emailBody.append(request.getName())
                        .append("\r\n")
                        .append(request.getActivity())
                        .append("\r\n");
            }

            return emailBody.toString();
        });

        configuration.setShowTabAllIcons(true);
        configuration.setCategoryForTabAllIcons(new String[]{
                "New","Folders","Calendar","Google","Microsoft","Emoji","Symbols","Numbers","Letters","0-9","A-Z"
        });

        DonationLink[] donationLinks = new DonationLink[]{
                new DonationLink(
                        // You can use png file (without extension) inside drawable-nodpi folder or url
                        "paypal",
                        "PayPal",
                        "Support me on Paypal",
                        "https://www.paypal.me/onnovdd"),
                new DonationLink(
                        // You can use png file (without extension) inside drawable-nodpi folder or url
                        "liberapay",
                        "Liberapay",
                        "Support me on Liberapay",
                        "https://liberapay.com/Donno/"),
                new DonationLink(
                        // You can use png file (without extension) inside drawable-nodpi folder or url
                        "kofi",
                        "Ko-Fi",
                        "Support me on Ko-Fi",
                        "https://ko-fi.com/donno_")
        };
        configuration.setDonationLinks(donationLinks);

        configuration.setShadowEnabled(false);

        configuration.setFilterRequestHandler((request) -> {
            // Return true to include the request
            // Return false to exclude the request

            String pkg = request.getPackageName();
            if (pkg == null) return true;
            if (pkg.startsWith("org.chromium.webapk") || pkg.startsWith("com.sec.android.app.sbrowser.webapk") || pkg.endsWith("com.google.android.archive.ReactivateActivity") ) {
                request.setAvailableForRequest(false);
                request.setInfoText("This icon is a web shortcut and not associated with an Android app. Unfortunately it cannot be requested at this time.\n\nIn many launchers, you can long-press the app icon in the drawer and pick an existing icon from the icon pack.");
            }
            return true;
        });

        OtherApp[] otherApps = new OtherApp[] {
                new OtherApp(
                        // You can use png file (without extension) inside drawable-nodpi folder or url
                        "arcticons_material_you",
                        "Arcticons Material You",
                        "Arcticons, but with a material you flavor!",
                        "https://play.google.com/store/apps/details?id=com.donnnno.arcticons.you.play"),
                new OtherApp(
                        // You can use png file (without extension) inside drawable-nodpi folder or url
                        "arcticons",
                        "Arcticons",
                        "Arcticons, with white lines",
                        "https://play.google.com/store/apps/details?id=com.donnnno.arcticons"),
                new OtherApp(
                        // You can use png file (without extension) inside drawable-nodpi folder or url
                        "arcticons_black",
                        "Arcticons Black",
                        "Arcticons, with black lines.",
                        "https://play.google.com/store/apps/details?id=com.donnnno.arcticons.light"),
                new OtherApp(
                        // You can use png file (without extension) inside drawable-nodpi folder or url
                        "arcticons_day_night",
                        "Arcticons Day & Night",
                        "An expirimental version of Arcticons that switches between dark & light mode.",
                        "https://github.com/Donnnno/Arcticons/releases")
        };
        configuration.setOtherApps(otherApps);

        return configuration;
    }
}
