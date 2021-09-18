package candybar.lib.items;

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

public class Wallpaper {

    private final String mUrl;
    private final String mThumbUrl;
    private final String mAuthor;
    private String mName;
    private int mColor;
    private int mSize;
    private String mMimeType;
    private ImageSize mDimensions;

    private Wallpaper(String name, String author, String url, String thumbUrl) {
        mName = name;
        mAuthor = author;
        mUrl = url;
        mThumbUrl = thumbUrl;
    }

    public String getName() {
        return mName;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public String getURL() {
        return mUrl;
    }

    public int getColor() {
        return mColor;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public ImageSize getDimensions() {
        return mDimensions;
    }

    public int getSize() {
        return mSize;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public void setDimensions(ImageSize dimensions) {
        mDimensions = dimensions;
    }

    public void setSize(int size) {
        mSize = size;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;
        if (object instanceof Wallpaper) {
            equals = mAuthor.equals(((Wallpaper) object).getAuthor()) &&
                    mUrl.equals(((Wallpaper) object).getURL()) &&
                    mThumbUrl.equals(((Wallpaper) object).getThumbUrl());
        }
        return equals;
    }

    public static Builder Builder() {
        return new Builder();
    }

    public static class Builder {

        private String mName;
        private String mAuthor;
        private String mThumbUrl;
        private String mUrl;
        private int mColor;
        private int mSize;
        private String mMimeType;
        private ImageSize mDimensions;

        private Builder() {
            mColor = 0;
            mSize = 0;
        }

        public Builder name(String name) {
            mName = name;
            return this;
        }

        public Builder author(String author) {
            mAuthor = author;
            return this;
        }

        public Builder url(String url) {
            mUrl = url;
            return this;
        }

        public Builder thumbUrl(String thumbUrl) {
            mThumbUrl = thumbUrl;
            return this;
        }

        public Builder dimensions(ImageSize dimensions) {
            mDimensions = dimensions;
            return this;
        }

        public Builder mimeType(String mimeType) {
            mMimeType = mimeType;
            return this;
        }

        public Builder color(int color) {
            mColor = color;
            return this;
        }

        public Builder size(int size) {
            mSize = size;
            return this;
        }

        public Wallpaper build() {
            Wallpaper wallpaper = new Wallpaper(mName, mAuthor, mUrl, mThumbUrl);
            wallpaper.setDimensions(mDimensions);
            wallpaper.setMimeType(mMimeType);
            wallpaper.setColor(mColor);
            wallpaper.setSize(mSize);
            return wallpaper;
        }
    }
}
