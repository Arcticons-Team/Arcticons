package com.donnnno.arcticons.activities;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.EditText;

import com.donnnno.arcticons.R;
import com.donnnno.arcticons.utils.ImageUtils;
import com.donnnno.arcticons.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class IconActivity extends com.donnnno.arcticons.activities.BaseActivity {
    private ArrayList<LinearLayout> layoutList = new ArrayList<>();
    private ArrayList<ImageView> imageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ScreenUtils.isPortrait(getApplicationContext())) {
            createLayout(7);
        } else {
            createLayout(12);
        }
    }

    private void createLayout(int width) {
        float scale = ScreenUtils.densityScale(getApplicationContext());
        int margin = 16 * Math.round(scale);

        LinearLayout.LayoutParams baseParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ScrollView baseScroller = new ScrollView(this);
        baseScroller.setLayoutParams(baseParams);
        baseScroller.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setContentView(baseScroller);
        baseScroller.setVisibility(View.VISIBLE);

        // display width hack
        Rect windowRect = new Rect();
        baseScroller.getWindowVisibleDisplayFrame(windowRect);
        int windowWidth = windowRect.right - windowRect.left;

        LinearLayout baseLayout = new LinearLayout(this);
        baseLayout.setOrientation(LinearLayout.VERTICAL);
        baseLayout.setLayoutParams(layoutParams);
        baseLayout.setPadding(margin, margin, 0, 0);
        baseScroller.addView(baseLayout);

        LinearLayout imageLayout = new LinearLayout(this);
        imageLayout.setOrientation(LinearLayout.VERTICAL);
        imageLayout.setLayoutParams(containerParams);
        imageLayout.setPadding(margin, margin, 0, 0);

        EditText searchBar = new EditText(this);
        searchBar.setLayoutParams(layoutParams);
        searchBar.setPadding(margin, margin, 0, 0);
        searchBar.setMaxLines(1);
        searchBar.setLines(1);
        searchBar.setInputType(InputType.TYPE_CLASS_TEXT);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                selectImages(windowWidth, margin, imageLayout, width, charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
        baseLayout.addView(searchBar);
        baseLayout.addView(imageLayout);

        selectImages(windowWidth, margin, imageLayout, width, "");
    }

    private void selectImages(int windowWidth, int margin, LinearLayout baseLayout, int width, String searchString) {
        layoutList = new ArrayList<>();
        imageList = new ArrayList<>();
        baseLayout.removeAllViews();

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        String[] images = getResources().getStringArray(R.array.icon_pack);
        images = Arrays.stream(images).filter(s -> s.contains(searchString.toLowerCase())).toArray(String[]::new);

        for (int i = 0; i < images.length; i++) {
            if ((i % width) == 0) {
                layoutList.add((i / width), new LinearLayout(this));
                layoutList.get(i / width).setOrientation(LinearLayout.HORIZONTAL);
                layoutList.get(i / width).setGravity(Gravity.START);
                layoutList.get(i / width).setLayoutParams(containerParams);

                baseLayout.addView(layoutList.get(i / width));
            }
            imageList.add(i, new ImageView(this));
            imageList.get(i).setLayoutParams(imageParams);
            imageList.get(i).setScaleType(ImageView.ScaleType.FIT_XY);
            imageList.get(i).setPadding(0, 0, margin, margin);
            imageList.get(i).setAdjustViewBounds(true);

            final int resId = getResources().getIdentifier(images[i], "drawable", getPackageName());
            ImageUtils.bitmapLoadAsync(imageList.get(i), getApplicationContext().getResources(), resId, (windowWidth / width) - (margin * width + margin) / width, (windowWidth / width) - (margin * width + margin) / width);

            layoutList.get(i / width).addView(imageList.get(i));
        }
    }
}
