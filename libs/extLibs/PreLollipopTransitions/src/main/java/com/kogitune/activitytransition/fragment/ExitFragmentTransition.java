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
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.fragment.app.FragmentManager;

import com.kogitune.activitytransition.core.MoveData;
import com.kogitune.activitytransition.core.TransitionAnimation;

public class ExitFragmentTransition {
    private final MoveData moveData;
    private Fragment fragment;
    private androidx.fragment.app.Fragment supportFragment;
    private TimeInterpolator interpolator;


    public ExitFragmentTransition(Fragment fragment, MoveData moveData) {
        this.fragment = fragment;
        this.moveData = moveData;
    }

    public ExitFragmentTransition(final androidx.fragment.app.Fragment fragment, MoveData moveData) {
        this.supportFragment = fragment;
        this.moveData = moveData;
    }

    public ExitFragmentTransition interpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public void startExitListening() {
        startExitListening(null);
    }

    public void startExitListening(final Runnable popBackStackRunnable) {
        if (interpolator == null) {
            interpolator = new DecelerateInterpolator();
        }
        final View toView = moveData.toView;
        toView.setFocusableInTouchMode(true);
        toView.requestFocus();
        toView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() != KeyEvent.ACTION_UP) {
                    return true;
                }
                TransitionAnimation.startExitAnimation(moveData, interpolator, () -> {
                    if (popBackStackRunnable != null) {
                        popBackStackRunnable.run();
                        return;
                    }
                    if (fragment == null) {
                        if (!supportFragment.isResumed()) {
                            return;
                        }
                        final FragmentManager fragmentManager = supportFragment.getFragmentManager();
                        if (fragmentManager != null) {
                            fragmentManager.popBackStack();
                        }
                    } else {
                        if (!fragment.isResumed()) {
                            return;
                        }
                        final android.app.FragmentManager fragmentManager = fragment.getFragmentManager();
                        if (fragmentManager != null) {
                            fragmentManager.popBackStack();
                        }
                    }
                });
                return true;
            }
            return false;
        });
    }

}
