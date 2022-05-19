package com.donnnno.arcticons.applications;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.Request;

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

                    emailBody.append(request.getName())
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
        configuration.setCategoryForTabAllIcons(new String[]{
                "Folders", "Calendar", "Letters", "Numbers", "A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
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

        return configuration;
    }
}
