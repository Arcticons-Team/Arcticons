package com.donnnno.arcticons.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.donnnno.arcticons.utils.IntentUtils;
import com.donnnno.arcticons.views.CenterButton;
import com.donnnno.arcticons.R;

public class SourceActivity extends BaseActivity {
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

        CenterButton code = new CenterButton(this);
        code.setText(R.string.code);
        code.setIcon(R.drawable.ic_code);
        code.setOnClickListener((v) -> IntentUtils.openUrl(this, R.string.url_repository));
        baseLayout.addView(code);

        CenterButton support = new CenterButton(this);
        support.setForeground(R.color.textDark);
        support.setBackground(R.color.colorDark);
        support.setText(R.string.support);
        support.setIcon(R.drawable.ic_favorite);
        support.setOnClickListener((v) -> IntentUtils.openUrl(this, R.string.url_support));
        baseLayout.addView(support);
    }
}