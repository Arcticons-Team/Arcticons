package candybar.lib.adapters;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.google.android.material.card.MaterialCardView;

import org.kustom.api.preset.AssetPresetFile;
import org.kustom.api.preset.PresetInfoLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.Preset;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.views.HeaderView;

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

public class PresetsAdapter extends RecyclerView.Adapter<PresetsAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Preset> mPresets;
    private Drawable wallpaperDrawable = null;

    private final int TYPE_HEADER = 0;
    private final int TYPE_CONTENT = 1;

    public PresetsAdapter(@NonNull Context context, List<Preset> presets) {
        mContext = context;
        mPresets = presets;
        try {
            wallpaperDrawable = WallpaperManager.getInstance(context).getDrawable();
        } catch (Exception ignored) {
            LogUtil.e("Unable to load wallpaper. Storage permission is not granted.");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_presets_item_header, parent, false);
        } else if (viewType == TYPE_CONTENT) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_presets_item_grid, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Preset preset = mPresets.get(position);

        if (holder.getItemViewType() == TYPE_HEADER) {
            holder.name.setText(preset.getHeaderText());
            holder.setType(preset.getHeaderText());
        } else if (holder.getItemViewType() == TYPE_CONTENT) {
            PresetInfoLoader.create(new AssetPresetFile(preset.getPath()))
                    .load(mContext, info -> holder.name.setText(info.getTitle()));

            Glide.with(mContext)
                    .asBitmap()
                    .load(new AssetPresetFile(preset.getPath()))
                    .transition(BitmapTransitionOptions.withCrossFade(300))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.image);
        }
    }

    public boolean isHeader(int position) {
        return mPresets.get(position).isHeader();
    }

    @Override
    public int getItemCount() {
        return mPresets.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) return TYPE_HEADER;
        return TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private HeaderView image;
        private TextView name;
        private final MaterialCardView card;

        ViewHolder(View itemView, int viewType) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            card = itemView.findViewById(R.id.card);

            if (viewType == TYPE_HEADER) {
                if (mContext.getResources().getBoolean(R.bool.use_flat_card)) {
                    card.setStrokeWidth(mContext.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
                    card.setCardElevation(0);
                    card.setUseCompatPadding(false);
                    int marginTop = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_top);
                    int marginLeft = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_left);
                    int marginRight = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_right);
                    int marginBottom = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_bottom);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();
                    params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
                }
            } else if (viewType == TYPE_CONTENT) {
                name = itemView.findViewById(R.id.name);
                image = itemView.findViewById(R.id.image);

                if (CandyBarApplication.getConfiguration().getWallpapersGrid() == CandyBarApplication.GridStyle.FLAT) {
                    card.setCardElevation(0);
                    card.setMaxCardElevation(0);
                }

                if (!Preferences.get(mContext).isCardShadowEnabled()) {
                    card.setCardElevation(0);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    StateListAnimator stateListAnimator = AnimatorInflater
                            .loadStateListAnimator(mContext, R.animator.card_lift);
                    card.setStateListAnimator(stateListAnimator);
                }

                if (wallpaperDrawable != null) {
                    ((HeaderView) itemView.findViewById(R.id.wallpaper_bg)).setImageDrawable(wallpaperDrawable);
                }

                card.setOnClickListener(this);
            }
        }

        public boolean isPackageInstalled(String pkgName) {
            try {
                mContext.getPackageManager().getPackageInfo(pkgName, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        public List<String[]> getRequiredApps(String type) {
            type = type.toLowerCase(Locale.ROOT);
            String nameBase = "";
            String pkgBase = "";

            switch (type) {
                case "lockscreens":
                    nameBase = "KLCK";
                    pkgBase = "org.kustom.lockscreen";
                    break;
                case "wallpapers":
                    nameBase = "KLWP";
                    pkgBase = "org.kustom.wallpaper";
                    break;
                case "widgets":
                    nameBase = "KWGT";
                    pkgBase = "org.kustom.widget";
                    break;
            }

            String namePro = nameBase + " Pro";
            String pkgPro = pkgBase + ".pro";

            List<String[]> requiredApps = new ArrayList<>();

            if (!isPackageInstalled(pkgBase)) {
                requiredApps.add(new String[]{nameBase, pkgBase});
            }
            if (!isPackageInstalled(pkgPro)) {
                requiredApps.add(new String[]{namePro, pkgPro});
            }

            return requiredApps;
        }

        public void setType(String type) {
            List<String[]> requiredApps = getRequiredApps(type);
            LinearLayout linearLayout = itemView.findViewById(R.id.container);

            if (requiredApps.size() > 0) {
                for (String[] requiredApp : requiredApps) {
                    View item = LayoutInflater.from(mContext).inflate(R.layout.fragment_presets_item_header_list, linearLayout, false);
                    ((TextView) item.findViewById(R.id.name)).setText(requiredApp[0]);
                    int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                    ((ImageView) item.findViewById(R.id.kustom_icon)).setImageDrawable(
                            DrawableHelper.getTintedDrawable(mContext, R.drawable.ic_drawer_presets, color));
                    item.setOnClickListener(v -> {
                        try {
                            Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "https://play.google.com/store/apps/details?id=" + requiredApp[1]));
                            mContext.startActivity(store);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(mContext, mContext.getResources().getString(
                                    R.string.no_browser), Toast.LENGTH_LONG).show();
                        }
                    });
                    ((ImageView) item.findViewById(R.id.forward_icon)).setImageDrawable(
                            DrawableHelper.getTintedDrawable(mContext, R.drawable.ic_arrow_forward, color));
                    linearLayout.addView(item);
                }
            } else {
                card.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getBindingAdapterPosition();
            if (id == R.id.card) {
                Preset preset = mPresets.get(position);
                String type = preset.getPath().split("/")[0];

                if (!type.equals("komponents")) {
                    String pkg = null, cls = null;

                    switch (type) {
                        case "lockscreens":
                            pkg = "org.kustom.lockscreen";
                            cls = "org.kustom.lib.editor.LockAdvancedEditorActivity";
                            break;
                        case "wallpapers":
                            pkg = "org.kustom.wallpaper";
                            cls = "org.kustom.lib.editor.WpAdvancedEditorActivity";
                            break;
                        case "widgets":
                            pkg = "org.kustom.widget";
                            cls = "org.kustom.widget.picker.WidgetPicker";
                            break;
                    }

                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(pkg, cls));

                    try {
                        intent.setData(new Uri.Builder()
                                .scheme("kfile")
                                .authority(mContext.getPackageName() + ".kustom.provider")
                                .appendPath(preset.getPath())
                                .build());
                    } catch (Exception ignored) {
                        intent.setData(Uri.parse("kfile://" + mContext.getPackageName() + "/" + preset.getPath()));
                    }

                    if (getRequiredApps(type).size() > 0) {
                        new MaterialDialog.Builder(mContext)
                                .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                                .content(R.string.presets_required_apps_not_installed)
                                .positiveText(R.string.close)
                                .show();
                    } else {
                        mContext.startActivity(intent);
                    }
                } else {
                    // TODO: Handle Komponent click
                }
            }
        }
    }
}
