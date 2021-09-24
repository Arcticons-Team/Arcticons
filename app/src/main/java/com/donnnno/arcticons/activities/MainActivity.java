package com.donnnno.arcticons.activities;

import androidx.annotation.NonNull;

import candybar.lib.activities.CandyBarMainActivity;

public class MainActivity extends CandyBarMainActivity {

    @NonNull
    @Override
    public ActivityConfiguration onInit() {
        return new ActivityConfiguration();
    }
}