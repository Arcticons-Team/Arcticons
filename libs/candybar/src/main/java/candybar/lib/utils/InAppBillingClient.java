package candybar.lib.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.lang.ref.WeakReference;
import java.util.List;

import candybar.lib.items.InAppBilling;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.listeners.InAppBillingListener;

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

public class InAppBillingClient implements PurchasesUpdatedListener, BillingClientStateListener {

    private final Context mContext;
    private boolean mIsInitialized;
    private BillingClient mBillingClient;

    private static WeakReference<InAppBillingClient> mInAppBilling;

    private InAppBillingClient(Context context) {
        mContext = context;
    }

    public static InAppBillingClient get(@NonNull Context context) {
        if (mInAppBilling == null || mInAppBilling.get() == null) {
            mInAppBilling = new WeakReference<>(new InAppBillingClient(context));
        }
        return mInAppBilling.get();
    }

    public void init() {
        getClient();
    }

    public BillingClient getClient() {
        if (mInAppBilling.get().mBillingClient == null || !mInAppBilling.get().mIsInitialized) {
            mInAppBilling.get().mBillingClient = BillingClient.newBuilder(mInAppBilling.get().mContext)
                    .setListener(this)
                    .enablePendingPurchases()
                    .build();
            mInAppBilling.get().mBillingClient.startConnection(this);
        }

        return mInAppBilling.get().mBillingClient;
    }

    public void destroy() {
        if (mInAppBilling != null && mInAppBilling.get() != null) {
            if (mInAppBilling.get().mBillingClient != null) {
                mInAppBilling.get().getClient().endConnection();
            }
            mInAppBilling.clear();
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (mInAppBilling == null || mInAppBilling.get() == null) {
            LogUtil.e("InAppBillingClient error: not initialized");
            return;
        }

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (purchases != null && purchases.size() > 0) {
                try {
                    ((InAppBillingListener) mInAppBilling.get().mContext)
                            .onProcessPurchase(purchases.get(0));
                } catch (Exception ignored) {
                }
            }
        } else {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.USER_CANCELED) {
                LogUtil.e("onPurchaseUpdated: " + billingResult.getResponseCode());
            }

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                mInAppBilling.get().mIsInitialized = false;
            }

            if (Preferences.get(mInAppBilling.get().mContext).getInAppBillingType()
                    == InAppBilling.PREMIUM_REQUEST) {
                Preferences.get(mInAppBilling.get().mContext).setPremiumRequestCount(0);
                Preferences.get(mInAppBilling.get().mContext).setPremiumRequestTotal(0);
            }
            Preferences.get(mInAppBilling.get().mContext).setInAppBillingType(-1);
        }
    }

    public void checkForUnprocessedPurchases() {
        mInAppBilling.get().getClient()
                .queryPurchasesAsync(BillingClient.SkuType.INAPP, (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (purchases.size() > 0) {
                            try {
                                ((InAppBillingListener) mInAppBilling.get().mContext)
                                        .onProcessPurchase(purchases.get(0));
                            } catch (Exception ignored) {
                            }
                        }
                    } else {
                        LogUtil.e("Failed to query purchases. Response Code: " + billingResult.getResponseCode());
                    }
                });
    }

    @Override
    public void onBillingServiceDisconnected() {
        if (mInAppBilling.get() == null) return;
        mInAppBilling.get().mIsInitialized = true;
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (mInAppBilling.get() == null) return;
        mInAppBilling.get().mIsInitialized = true;
        checkForUnprocessedPurchases();
    }
}
