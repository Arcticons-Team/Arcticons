package com.donnnno.arcticons.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.lang.ref.WeakReference;

public class IconViewHolder extends ViewHolderAdapter.ViewHolder<String> {
    private static final String TAG = "IVH";

    private final ImageView icon;
    private AsyncTask<String, Void, Drawable> asyncLoad = null;
    private final static View.OnLongClickListener longClickListener = v -> {
        CharSequence content = null;
        if (v instanceof ImageView)
            content = v.getContentDescription();
        if (content != null)
            Toast.makeText(v.getContext(), content, Toast.LENGTH_SHORT).show();
        return true;
    };

    public IconViewHolder(View view) {
        super(view);
        icon = (ImageView) view;
    }

    @Override
    protected void setContent(String content, int position, @NonNull ViewHolderAdapter<String, ? extends ViewHolderAdapter.ViewHolder<String>> adapter) {
        //IconAdapter iconAdapter = (IconAdapter) adapter;
        if (asyncLoad != null)
            asyncLoad.cancel(false);

        icon.setContentDescription(content);
        icon.setOnLongClickListener(longClickListener);
        icon.animate().cancel();
        icon.setAlpha(0f);

        asyncLoad = new AsyncLoad(this).execute(content);
    }

    private static class AsyncLoad extends AsyncTask<String, Void, Drawable> {
        private final WeakReference<IconViewHolder> weakHolder;

        public AsyncLoad(IconViewHolder holder) {
            super();
            weakHolder = new WeakReference<>(holder);
        }

        @Override
        protected Drawable doInBackground(String... strings) {
            IconViewHolder holder = weakHolder.get();
            if (holder == null || strings.length == 0)
                return null;
            String resIdName = strings[0];
            Context ctx = holder.icon.getContext();
            final int resId = ctx.getResources().getIdentifier(resIdName, "drawable", ctx.getPackageName());
            if (resId == Resources.ID_NULL) {
                Log.e(TAG, "`" + resIdName + "` not found");
                return null;
            }
            try {
                return ResourcesCompat.getDrawable(ctx.getResources(), resId, null);
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "`" + resIdName + "` not found", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            IconViewHolder holder = weakHolder.get();
            if (holder != null && this.equals(holder.asyncLoad)) {
                holder.asyncLoad = null;
                holder.icon.setImageDrawable(drawable);
                holder.icon.animate()
                        .alpha(1f)
                        .setDuration(1000);
            }
        }
    }
}
