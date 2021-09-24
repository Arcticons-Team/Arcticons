package candybar.lib.utils;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

public final class CommonModelLoaderFactory implements ModelLoaderFactory<String, Bitmap> {
    private final Context mContext;

    CommonModelLoaderFactory(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ModelLoader<String, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new CommonModelLoader(mContext);
    }

    @Override
    public void teardown() {
    }
}
