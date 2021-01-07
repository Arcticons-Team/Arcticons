package com.donnnno.arcticons.adapters;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class IconViewHolder extends ViewHolderAdapter.ViewHolder<String> {
    private final ImageView icon;

    public IconViewHolder(View view) {
        super(view);
        icon = (ImageView) view;
    }

    @Override
    protected void setContent(String content, int position, @NonNull ViewHolderAdapter<String, ? extends ViewHolderAdapter.ViewHolder<String>> adapter) {
        //IconAdapter iconAdapter = (IconAdapter) adapter;
        final int resId = icon.getResources().getIdentifier(content, "drawable", icon.getContext().getPackageName());
        icon.setImageResource(resId);
        icon.setAlpha(0f);
        icon.setVisibility(View.VISIBLE);
        icon.animate()
                .alpha(1f)
                .setDuration(1000)
                .setListener(null);
    }
}
