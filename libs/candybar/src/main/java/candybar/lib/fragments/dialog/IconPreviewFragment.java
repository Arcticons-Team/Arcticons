package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;

import java.util.concurrent.atomic.AtomicBoolean;

import candybar.lib.R;
import candybar.lib.databases.Database;
import candybar.lib.fragments.IconsFragment;
import candybar.lib.helpers.TypefaceHelper;

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

public class IconPreviewFragment extends DialogFragment {

    private String mIconTitle;
    private String mDrawableName;
    private int mIconId;

    private boolean prevIsBookmarked, currentIsBookmarked;

    private static final String TITLE = "title";
    private static final String DRAWABLE_NAME = "drawable_name";
    private static final String ID = "id";

    private static final String TAG = "candybar.dialog.icon.preview";

    private static IconPreviewFragment newInstance(String title, int id, String drawableName) {
        IconPreviewFragment fragment = new IconPreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
        bundle.putString(DRAWABLE_NAME, drawableName);
        bundle.putInt(ID, id);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showIconPreview(@NonNull FragmentManager fm, @NonNull String title, int id, @Nullable String drawableName) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = IconPreviewFragment.newInstance(title, id, drawableName);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIconTitle = requireArguments().getString(TITLE);
        mDrawableName = requireArguments().getString(DRAWABLE_NAME);
        mIconId = requireArguments().getInt(ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog dialog = new MaterialDialog.Builder(requireActivity())
                .customView(R.layout.fragment_icon_preview, false)
                .typeface(TypefaceHelper.getMedium(requireActivity()), TypefaceHelper.getRegular(requireActivity()))
                .positiveText(R.string.close)
                .build();

        dialog.show();

        if (savedInstanceState != null) {
            mIconTitle = savedInstanceState.getString(TITLE);
            mDrawableName = savedInstanceState.getString(DRAWABLE_NAME);
            mIconId = savedInstanceState.getInt(ID);
        }

        TextView name = (TextView) dialog.findViewById(R.id.name);
        ImageView icon = (ImageView) dialog.findViewById(R.id.icon);
        ImageView bookmark = (ImageView) dialog.findViewById(R.id.bookmark_button);

        name.setText(mIconTitle);

        Glide.with(this)
                .load("drawable://" + mIconId)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(icon);

        if (mDrawableName == null) {
            bookmark.setVisibility(View.INVISIBLE);
        } else {
            AtomicBoolean isBookmarked = new AtomicBoolean(Database.get(requireActivity()).isIconBookmarked(mDrawableName));
            prevIsBookmarked = currentIsBookmarked = isBookmarked.get();

            final Runnable updateBookmark = () -> {
                @DrawableRes int drawableRes;
                @AttrRes int colorAttr;
                if (isBookmarked.get()) {
                    drawableRes = R.drawable.ic_bookmark_filled;
                    colorAttr = R.attr.colorSecondary;
                } else {
                    drawableRes = R.drawable.ic_bookmark;
                    colorAttr = android.R.attr.textColorSecondary;
                }
                bookmark.setImageDrawable(DrawableHelper.getTintedDrawable(requireActivity(),
                        drawableRes, ColorHelper.getAttributeColor(requireActivity(), colorAttr)));
            };

            updateBookmark.run();

            bookmark.setOnClickListener(view -> {
                if (isBookmarked.get()) {
                    Database.get(requireActivity()).deleteBookmarkedIcon(mDrawableName);
                } else {
                    Database.get(requireActivity()).addBookmarkedIcon(mDrawableName, mIconTitle);
                }
                isBookmarked.set(!isBookmarked.get());
                updateBookmark.run();
                currentIsBookmarked = isBookmarked.get();
            });
        }

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE, mIconTitle);
        outState.putInt(ID, mIconId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (prevIsBookmarked != currentIsBookmarked) {
            IconsFragment.reloadBookmarks();
        }
    }
}
