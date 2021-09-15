package com.candybar.dev.activities;

import androidx.annotation.NonNull;

import candybar.lib.activities.CandyBarSplashActivity;

public class SplashActivity extends CandyBarSplashActivity {
    @NonNull
    @Override
    public Class<?> getMainActivity() {
        return MainActivity.class;
    }
}
