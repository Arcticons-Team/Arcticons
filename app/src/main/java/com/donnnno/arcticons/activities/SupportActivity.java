package com.donnnno.arcticons.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.donnnno.arcticons.R;
import com.donnnno.arcticons.utils.IntentUtils;
import com.donnnno.arcticons.views.CenterButton;

public class SupportActivity extends AppCompatActivity {

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

        //PayPal
        CenterButton paypal = new CenterButton(this);
        paypal.setForeground(R.color.textDark);
        paypal.setBackground(R.color.colorDark);
        paypal.setText(R.string.paypal);
        paypal.setIcon(R.drawable.ic_paypal_button);
        paypal.setOnClickListener((v) -> IntentUtils.openUrl(this, R.string.url_paypal));
        baseLayout.addView(paypal);

        //Liberapay
        CenterButton liberapay = new CenterButton(this);
        liberapay.setForeground(R.color.textDark);
        liberapay.setBackground(R.color.colorDark);
        liberapay.setText(R.string.liberapay);
        liberapay.setIcon(R.drawable.ic_liberapay_button);
        liberapay.setOnClickListener((v) -> IntentUtils.openUrl(this, R.string.url_liberapay));
        baseLayout.addView(liberapay);
    }
}