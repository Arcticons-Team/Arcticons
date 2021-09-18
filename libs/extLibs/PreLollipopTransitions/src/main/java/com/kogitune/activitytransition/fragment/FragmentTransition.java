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

import android.animation.TimeInterpolator;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.kogitune.activitytransition.core.MoveData;
import com.kogitune.activitytransition.core.TransitionAnimation;

public class FragmentTransition {
    private static TimeInterpolator interpolator;
    private int duration = 1000;
    private View toView;
    private androidx.fragment.app.Fragment supportFragment;
    private Fragment fragment;

    private FragmentTransition(Fragment fragment) {
        this.fragment = fragment;
    }

    private FragmentTransition(androidx.fragment.app.Fragment fragment) {
        this.supportFragment = fragment;
    }

    public static FragmentTransition with(Fragment fragment) {
        return new FragmentTransition(fragment);
    }

    public static FragmentTransition with(androidx.fragment.app.Fragment fragment) {
        return new FragmentTransition(fragment);
    }

    public FragmentTransition to(View toView) {
        this.toView = toView;
        return this;
    }

    public FragmentTransition duration(int duration) {
        this.duration = duration;
        return this;
    }


    public FragmentTransition interpolator(TimeInterpolator interpolator) {
        FragmentTransition.interpolator = interpolator;
        return this;
    }

    public ExitFragmentTransition start(Bundle savedInstanceState) {
        if (interpolator == null) {
            interpolator = new DecelerateInterpolator();
        }
        final Context context = toView.getContext();
        final Bundle bundle;
        if (fragment == null) {
            bundle = supportFragment.getArguments();
        } else {
            bundle = fragment.getArguments();
        }
        final MoveData moveData = TransitionAnimation.startAnimation(context, toView, bundle, savedInstanceState, duration, interpolator);
        if (fragment == null) {
            return new ExitFragmentTransition(supportFragment, moveData);
        }
        return new ExitFragmentTransition(fragment, moveData);
    }

}
