package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import candybar.lib.R;
import candybar.lib.adapters.dialog.InAppBillingAdapter;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.InAppBilling;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.AsyncTaskBase;
import candybar.lib.utils.InAppBillingClient;
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

public class InAppBillingFragment extends DialogFragment {

    private ListView mInAppList;
    private ProgressBar mProgress;

    private int mType;
    private String mKey;
    private String[] mProductsId;
    private int[] mProductsCount;

    private InAppBillingAdapter mAdapter;
    private AsyncTaskBase mAsyncTask;

    private static final String TYPE = "type";
    private static final String KEY = "key";
    private static final String PRODUCT_ID = "product_id";
    private static final String PRODUCT_COUNT = "product_count";

    private static final String TAG = "candybar.dialog.inapp.billing";

    private static InAppBillingFragment newInstance(int type, String key, String[] productId, int[] productCount) {
        InAppBillingFragment fragment = new InAppBillingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        bundle.putString(KEY, key);
        bundle.putStringArray(PRODUCT_ID, productId);
        if (productCount != null)
            bundle.putIntArray(PRODUCT_COUNT, productCount);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showInAppBillingDialog(@NonNull FragmentManager fm,
                                              int type, @NonNull String key, @NonNull String[] productId,
                                              int[] productCount) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = InAppBillingFragment.newInstance(type, key, productId, productCount);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(TYPE);
            mKey = getArguments().getString(KEY);
            mProductsId = getArguments().getStringArray(PRODUCT_ID);
            mProductsCount = getArguments().getIntArray(PRODUCT_COUNT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(requireActivity());
        builder.title(mType == InAppBilling.DONATE ?
                R.string.navigation_view_donate : R.string.premium_request)
                .customView(R.layout.fragment_inapp_dialog, false)
                .typeface(TypefaceHelper.getMedium(requireActivity()), TypefaceHelper.getRegular(requireActivity()))
                .positiveText(mType == InAppBilling.DONATE ? R.string.donate : R.string.premium_request_buy)
                .negativeText(R.string.close)
                .onPositive((dialog, which) -> {
                    if (mAsyncTask == null) {
                        try {
                            InAppBillingListener listener = (InAppBillingListener) requireActivity();
                            listener.onInAppBillingSelected(
                                    mType, mAdapter.getSelectedProduct());
                        } catch (Exception ignored) {
                        }
                        dismiss();
                    }
                })
                .onNegative((dialog, which) ->
                        Preferences.get(requireActivity()).setInAppBillingType(-1));
        MaterialDialog dialog = builder.build();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        setCancelable(false);

        mInAppList = (ListView) dialog.findViewById(R.id.inapp_list);
        mProgress = (ProgressBar) dialog.findViewById(R.id.progress);

        if (savedInstanceState != null) {
            mType = savedInstanceState.getInt(TYPE);
            mKey = savedInstanceState.getString(KEY);
            mProductsId = savedInstanceState.getStringArray(PRODUCT_ID);
            mProductsCount = savedInstanceState.getIntArray(PRODUCT_COUNT);
        }

        mAsyncTask = new InAppProductsLoader().execute();

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TYPE, mType);
        outState.putString(KEY, mKey);
        outState.putStringArray(PRODUCT_ID, mProductsId);
        outState.putIntArray(PRODUCT_COUNT, mProductsCount);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDismiss(dialog);
    }

    private class InAppProductsLoader extends AsyncTaskBase {

        private List<InAppBilling> inAppBillings;

        @Override
        protected void preRun() {
            mProgress.setVisibility(View.VISIBLE);
            inAppBillings = new ArrayList<>();
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);

                    AtomicBoolean isSuccess = new AtomicBoolean(false);
                    CountDownLatch doneSignal = new CountDownLatch(1);

                    InAppBillingClient.get(requireActivity()).getClient().querySkuDetailsAsync(
                            SkuDetailsParams.newBuilder()
                                    .setSkusList(Arrays.asList(mProductsId))
                                    .setType(BillingClient.SkuType.INAPP)
                                    .build(),
                            (billingResult, skuDetailsList) -> {
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    if (skuDetailsList != null) {
                                        Map<String, SkuDetails> skuDetailsMap = new HashMap<>();
                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            skuDetailsMap.put(skuDetails.getSku(), skuDetails);
                                        }

                                        for (int i = 0; i < mProductsId.length; i++) {
                                            String productId = mProductsId[i];
                                            SkuDetails skuDetails = skuDetailsMap.get(productId);
                                            if (skuDetails != null) {
                                                inAppBillings.add(mProductsCount != null
                                                        ? new InAppBilling(skuDetails, productId, mProductsCount[i])
                                                        : new InAppBilling(skuDetails, productId));
                                            } else {
                                                LogUtil.e("Found invalid product ID - " + productId);
                                            }
                                        }

                                        isSuccess.set(true);
                                    }
                                } else {
                                    LogUtil.e("Failed to load SKU details. Response Code: " + billingResult.getResponseCode());
                                }

                                doneSignal.countDown();
                            });

                    doneSignal.await();

                    return isSuccess.get();
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void postRun(boolean ok) {
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            mProgress.setVisibility(View.GONE);

            if (ok) {
                mAdapter = new InAppBillingAdapter(getActivity(), inAppBillings);
                mInAppList.setAdapter(mAdapter);
            } else {
                dismiss();
                Preferences.get(getActivity()).setInAppBillingType(-1);

                Toast.makeText(getActivity(), R.string.billing_load_product_failed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
