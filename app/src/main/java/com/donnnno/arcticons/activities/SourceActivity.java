package com.donnnno.arcticons.activities;


import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.donnnno.arcticons.utils.ImageUtils;
import com.donnnno.arcticons.utils.IntentUtils;
import com.donnnno.arcticons.utils.ScreenUtils;
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

        //Code
        CenterButton code = new CenterButton(this);
        code.setForeground(R.color.textDark);
        code.setBackground(R.color.colorDark);
        code.setText(R.string.code);
        code.setIcon(R.drawable.ic_code_button);
        code.setOnClickListener((v) -> IntentUtils.openUrl(this, R.string.url_repository));
        baseLayout.addView(code);

        //License
        CenterButton license = new CenterButton(this);
        license.setForeground(R.color.textDark);
        license.setBackground(R.color.colorDark);
        license.setText(R.string.license);
        license.setIcon(R.drawable.ic_license_button);
        license.setOnClickListener((v) -> IntentUtils.openActivity(this, LicenseActivity.class));
        baseLayout.addView(license);
    }
}