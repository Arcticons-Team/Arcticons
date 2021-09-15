package candybar.lib.utils.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import candybar.lib.R;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
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
 */

public class HeaderView extends AppCompatImageView {

    private int mWidthRatio;
    private int mHeightRatio;

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeaderView);

        try {
            mWidthRatio = typedArray.getInteger(R.styleable.HeaderView_widthRatio, 16);
            mHeightRatio = typedArray.getInteger(R.styleable.HeaderView_heightRatio, 9);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        double height = (widthMeasureSpec / (double) mWidthRatio) * mHeightRatio;
        setMeasuredDimension(widthMeasureSpec, Double.valueOf(height).intValue());
    }

    public void setRatio(int widthRatio, int heightRatio) {
        mWidthRatio = widthRatio;
        mHeightRatio = heightRatio;
        double height = (getMeasuredWidth() / (double) mWidthRatio) * mHeightRatio;
        setMeasuredDimension(getMeasuredWidth(), Double.valueOf(height).intValue());
    }
}
