package candybar.lib.items;

import android.content.ComponentName;

import androidx.annotation.Nullable;

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

public class Request {

    private final String mName;
    private final String mActivity;
    private String mPackageName;
    private String mOrderId;
    private String mProductId;
    private String mRequestedOn;
    private boolean mRequested;

    private Request(String name, String activity) {
        mName = name;
        mActivity = activity;
    }

    public String getName() {
        return mName;
    }

    @Nullable
    public String getPackageName() {
        if (mPackageName == null) {
            if (mActivity.length() > 0) {
                return mActivity.substring(0, mActivity.lastIndexOf("/"));
            }
        }
        return mPackageName;
    }

    public String getActivity() {
        return mActivity;
    }

    public boolean isRequested() {
        return mRequested;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public String getProductId() {
        return mProductId;
    }

    public String getRequestedOn() {
        return mRequestedOn;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public void setOrderId(String orderId) {
        mOrderId = orderId;
    }

    public void setProductId(String productId) {
        mProductId = productId;
    }

    public void setRequestedOn(String requestedOn) {
        mRequestedOn = requestedOn;
    }

    public void setRequested(boolean requested) {
        mRequested = requested;
    }

    public static Builder Builder() {
        return new Builder();
    }

    public static class Builder {

        private String mName;
        private String mActivity;
        private String mPackageName;
        private String mOrderId;
        private String mProductId;
        private String mRequestedOn;
        private boolean mRequested;

        private Builder() {
            mName = "";
            mActivity = "";
            mRequested = false;
        }

        public Builder name(String name) {
            mName = name;
            return this;
        }

        public Builder activity(String activity) {
            mActivity = activity;
            return this;
        }

        public Builder packageName(String packageName) {
            mPackageName = packageName;
            return this;
        }

        public Builder orderId(String orderId) {
            mOrderId = orderId;
            return this;
        }

        public Builder productId(String productId) {
            mProductId = productId;
            return this;
        }

        public Builder requestedOn(String requestedOn) {
            mRequestedOn = requestedOn;
            return this;
        }

        public Builder requested(boolean requested) {
            mRequested = requested;
            return this;
        }

        public Request build() {
            Request request = new Request(mName, mActivity);
            request.setPackageName(mPackageName);
            request.setRequestedOn(mRequestedOn);
            request.setRequested(mRequested);
            request.setOrderId(mOrderId);
            request.setProductId(mProductId);
            return request;
        }
    }

    public static class Property {

        private ComponentName componentName;
        private final String orderId;
        private final String productId;

        public Property(ComponentName componentName, String orderId, String productId) {
            this.componentName = componentName;
            this.orderId = orderId;
            this.productId = productId;
        }

        @Nullable
        public ComponentName getComponentName() {
            return componentName;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getProductId() {
            return productId;
        }

        public void setComponentName(ComponentName componentName) {
            this.componentName = componentName;
        }
    }
}
