/*
 * Copyright (C) 2015 takahirom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.kogitune.activitytransition.core;

import android.content.Context;
import android.os.Bundle;

import com.kogitune.activitytransition.BuildConfigUtils;

public class TransitionData {
    public static final String EXTRA_IMAGE_LEFT = ".left";
    public static final String EXTRA_IMAGE_TOP = ".top";
    public static final String EXTRA_IMAGE_WIDTH = ".width";
    public static final String EXTRA_IMAGE_HEIGHT = ".height";
    public static final String EXTRA_IMAGE_PATH = ".imageFilePath";

    public final int thumbnailTop;
    public final int thumbnailLeft;
    public final int thumbnailWidth;
    public final int thumbnailHeight;
    public final String imageFilePath;
    private String appId;

    public TransitionData(Context context, int thumbnailLeft, int thumbnailTop, int thumbnailWidth, int thumbnailHeight, String imageFilePath) {
        setAppId(context);
        this.thumbnailLeft = thumbnailLeft;
        this.thumbnailTop = thumbnailTop;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
        this.imageFilePath = imageFilePath;
    }

    public TransitionData(Context context, Bundle bundle) {
        setAppId(context);
        thumbnailTop = bundle.getInt(appId + EXTRA_IMAGE_TOP);
        thumbnailLeft = bundle.getInt(appId + EXTRA_IMAGE_LEFT);
        thumbnailWidth = bundle.getInt(appId + EXTRA_IMAGE_WIDTH);
        thumbnailHeight = bundle.getInt(appId + EXTRA_IMAGE_HEIGHT);
        imageFilePath = bundle.getString(appId + EXTRA_IMAGE_PATH);
    }

    private void setAppId(Context context) {
        appId = (String) BuildConfigUtils.getBuildConfigValue(context, "APPLICATION_ID");
    }


    public Bundle getBundle() {
        final Bundle bundle = new Bundle();
        if (imageFilePath != null) {
            bundle.putString(appId + EXTRA_IMAGE_PATH, imageFilePath);
        }
        bundle.putInt(appId + EXTRA_IMAGE_LEFT, thumbnailLeft);
        bundle.putInt(appId + EXTRA_IMAGE_TOP, thumbnailTop);
        bundle.putInt(appId + EXTRA_IMAGE_WIDTH, thumbnailWidth);
        bundle.putInt(appId + EXTRA_IMAGE_HEIGHT, thumbnailHeight);
        return bundle;

    }
}
