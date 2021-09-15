package candybar.lib.helpers;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import candybar.lib.R;

public class PlayStoreCheckHelper {

    private final Context mContext;
    private final Runnable mCallback;

    public PlayStoreCheckHelper(Context context, Runnable callback) {
        mContext = context;
        mCallback = callback;
    }

    public void run() {
        PackageManager pm = mContext.getPackageManager();
        String installerPackage = pm.getInstallerPackageName(mContext.getPackageName());
        boolean fromPlayStore = installerPackage != null && installerPackage.contentEquals("com.android.vending");

        MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
                .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                .title(R.string.playstore_check)
                .content(fromPlayStore ? R.string.playstore_check_success : R.string.playstore_check_failed)
                .positiveText(R.string.close)
                .cancelable(false)
                .canceledOnTouchOutside(false);

        if (fromPlayStore) {
            dialog.onPositive((dialog1, which) -> {
                dialog1.dismiss();
                mCallback.run();
            });
        } else {
            dialog.onPositive((dial, which) -> ((AppCompatActivity) mContext).finish());
        }

        dialog.show();
    }
}
