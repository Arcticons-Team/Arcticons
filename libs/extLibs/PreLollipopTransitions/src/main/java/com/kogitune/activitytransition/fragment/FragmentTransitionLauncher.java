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

package com.kogitune.activitytransition.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.view.View;

import com.kogitune.activitytransition.core.TransitionBundleFactory;

public class FragmentTransitionLauncher {

    private final Context context;
    private View fromView;
    private Bitmap bitmap;


    private FragmentTransitionLauncher(Context context) {
        this.context = context;
    }

    public static FragmentTransitionLauncher with(Context context) {
        return new FragmentTransitionLauncher(context);
    }

    public FragmentTransitionLauncher from(View fromView) {
        this.fromView = fromView;
        return this;
    }

    public FragmentTransitionLauncher image(final Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public void prepare(Fragment toFragment) {
        final Bundle transitionBundle = TransitionBundleFactory.createTransitionBundle(context, fromView, bitmap);
        if (Build.VERSION.SDK_INT >= 21) {
            toFragment.setSharedElementEnterTransition(new ChangeBounds());
            toFragment.setSharedElementReturnTransition(new ChangeBounds());
        }
        toFragment.setArguments(transitionBundle);
    }

    public void prepare(androidx.fragment.app.Fragment toFragment) {
        final Bundle transitionBundle = TransitionBundleFactory.createTransitionBundle(context, fromView, bitmap);
        if (Build.VERSION.SDK_INT >= 21) {
            toFragment.setSharedElementEnterTransition(new ChangeBounds());
            toFragment.setSharedElementReturnTransition(new ChangeBounds());
        }
        toFragment.setArguments(transitionBundle);
    }
}
