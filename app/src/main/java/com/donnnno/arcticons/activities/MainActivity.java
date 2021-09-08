package com.donnnno.arcticons.activities;

import androidx.annotation.NonNull;

import candybar.lib.activities.CandyBarMainActivity;

import com.donnnno.arcticons.licenses.License;

public class MainActivity extends CandyBarMainActivity {

    @NonNull
    @Override
    public ActivityConfiguration onInit() {
        return new ActivityConfiguration()
                .setLicenseCheckerEnabled(License.isLicenseCheckerEnabled())
                .setLicenseKey(License.getLicenseKey())
                .setRandomString(License.getRandomString())
                .setDonationProductsId(License.getDonationProductsId())
                .setPremiumRequestProducts(License.getPremiumRequestProductsId(), License.getPremiumRequestProductsCount());
    }
}
