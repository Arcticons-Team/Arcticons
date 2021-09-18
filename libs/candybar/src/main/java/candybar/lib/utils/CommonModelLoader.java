package candybar.lib.utils;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

public final class CommonModelLoader implements ModelLoader<String, Bitmap> {
    private final Context mContext;

    CommonModelLoader(Context context) {
        mContext = context;
    }

    @Override
    public boolean handles(@NonNull String model) {
        return model.startsWith("drawable://") || model.startsWith("package://");
    }

    @Override
    public LoadData<Bitmap> buildLoadData(@NonNull String model, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model), new CommonDataFetcher(mContext, model));
    }
}
