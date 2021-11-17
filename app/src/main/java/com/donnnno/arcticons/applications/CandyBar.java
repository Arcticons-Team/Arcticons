package com.donnnno.arcticons.applications;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.Request;

// TODO: Remove `//` below to enable OneSignal
//import com.onesignal.OneSignal;

public class CandyBar extends CandyBarApplication {


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

                emailBody.append("- [ ] ")
                        .append(request.getName())
                        .append("\r\n")
                        .append(request.getActivity())
                        .append("\r\n");

                String installerPackage = packageManager.getInstallerPackageName(request.getPackageName());

                if (installerPackage != null && installerPackage.equals("com.android.vending")) {
                    emailBody.append("https://play.google.com/store/apps/details?id=")
                            .append(request.getPackageName());
                } else {
                    emailBody.append("https://f-droid.org/en/packages/")
                            .append(request.getPackageName()).append("/");
                }
            }

            return emailBody.toString();
        });
        configuration.setShowTabAllIcons(true);
        configuration.setCategoryForTabAllIcons(new String[] {
                "Folders", "Calendar", "Letters", "Numbers", "A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        });
        return configuration;
    }
}
