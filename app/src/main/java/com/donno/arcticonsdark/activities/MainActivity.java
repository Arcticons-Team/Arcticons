package com.donno.arcticonsdark.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.donno.arcticonsdark.R;
import com.donno.arcticonsdark.utils.IntentUtils;
import com.donno.arcticonsdark.views.CenterButton;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLayout();
    }

    private void createLayout() {

        LinearLayout baseLayout = new LinearLayout(this);
        baseLayout.setOrientation(LinearLayout.VERTICAL);
        baseLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        baseLayout.setGravity(Gravity.CENTER);
        baseLayout.setBackgroundColor(getResources().getColor(R.color.colorLight));
        setContentView(baseLayout);

        //Icons
        CenterButton icons = new CenterButton(this);
        icons.setForeground(R.color.textDark);
        icons.setBackground(R.color.colorDark);
        icons.setText(R.string.icons);
        icons.setIcon(R.drawable.ic_icon_button);
        icons.setOnClickListener((v) -> IntentUtils.openActivity(this, IconActivity.class));
        baseLayout.addView(icons);

        //Source
        CenterButton source = new CenterButton(this);
        source.setForeground(R.color.textDark);
        source.setBackground(R.color.colorDark);
        source.setText(R.string.source);
        source.setIcon(R.drawable.ic_source_button);
        source.setOnClickListener((v) -> IntentUtils.openActivity(this, SourceActivity.class));
        baseLayout.addView(source);

        //Support
        CenterButton support = new CenterButton(this);
        support.setForeground(R.color.textDark);
        support.setBackground(R.color.colorDark);
        support.setText(R.string.support);
        support.setIcon(R.drawable.ic_support_button);
        support.setOnClickListener((v) -> IntentUtils.openUrl(this, R.string.url_support));
        baseLayout.addView(support);
    }
}