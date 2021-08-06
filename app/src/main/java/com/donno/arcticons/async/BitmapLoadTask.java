package com.donno.arcticons.async;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.donno.arcticons.interfaces.BitmapListener;
import com.donno.arcticons.utils.ImageUtils;

public class BitmapLoadTask extends AsyncTask<Integer, Void, Bitmap> {

    private BitmapListener bitmapListener;
    private Resources resources;
    private int resId;
    private int width;
    private int height;

    public BitmapLoadTask(Resources resources, int resId, int width, int height, BitmapListener bitmapListener) {
        this.resources = resources;
        this.resId = resId;
        this.width = width;
        this.height = height;
        this.bitmapListener = bitmapListener;
    }

    // decode image in background
    @Override
    protected Bitmap doInBackground(Integer... parameters) {
        return ImageUtils.bitmapLoad(resources, resId, width, height);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        bitmapListener.onBitmap(bitmap);
    }
}
