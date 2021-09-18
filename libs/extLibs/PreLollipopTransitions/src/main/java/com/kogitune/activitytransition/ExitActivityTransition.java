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

package com.kogitune.activitytransition;

import android.animation.TimeInterpolator;
import android.app.Activity;
import android.os.Build;
import android.view.animation.DecelerateInterpolator;

import com.kogitune.activitytransition.core.MoveData;
import com.kogitune.activitytransition.core.TransitionAnimation;

public class ExitActivityTransition {
    private final MoveData moveData;
    private TimeInterpolator interpolator;


    public ExitActivityTransition(MoveData moveData) {
        this.moveData = moveData;
    }

    public ExitActivityTransition interpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public void exit(final Activity activity) {
        if (interpolator == null) {
            interpolator = new DecelerateInterpolator();
        }
        TransitionAnimation.startExitAnimation(moveData, interpolator, () -> {
            if (Build.VERSION.SDK_INT >= 21) {
                activity.finishAfterTransition();
                return;
            }
            activity.finish();
            activity.overridePendingTransition(0, 0);
        });
    }

}
