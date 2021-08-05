package com.donno.arcticons.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.res.ResourcesCompat;

import com.donno.arcticons.R;
import com.donno.arcticons.utils.ScreenUtils;

public class CenterButton extends LinearLayoutCompat {
    public Context context;
    public LinearLayout button;

    public ImageView imageView;
    public TextView textView;

    public CenterButton(Context context) {
        super(context);

        float scale = ScreenUtils.densityScale(context);
        int padding = Math.round(16 * scale);

        this.context = context;
        this.button = new LinearLayout(context);

        this.imageView = new ImageView(context);
        this.textView = new TextView(context);

        textView.setTextSize(24);
        textView.setPadding(padding, padding, padding, padding);

        button.addView(imageView);
        button.addView(textView);

        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        button.setGravity(Gravity.CENTER);

        setForeground(R.color.textLight);
        setBackground(R.color.colorLight);
        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        setGravity(Gravity.CENTER);
        addView(button);
    }

    public void setText(int resId) {
        textView.setText(resId);
    }

    public void setIcon(int resId) {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resId, null);

        imageView.setImageDrawable(drawable);
    }

    public void setForeground(int resId) {
        int color = context.getResources().getColor(resId);

        textView.setTextColor(color);
        imageView.setColorFilter(color);
    }

    public void setBackground(int resId) {
        setBackgroundColor(context.getResources().getColor(resId));
    }

    public void disableIcon() {
        int padding = textView.getPaddingLeft() * 4;

        textView.setPadding(padding, padding, padding, padding);
        imageView.setImageDrawable(null);
    }
}