package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import candybar.lib.R;
import candybar.lib.adapters.dialog.OtherAppsAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.TypefaceHelper;

public class DonationLinksFragment extends DialogFragment {

    private static final String TAG = "candybar.dialog.donationlinks";

    private static DonationLinksFragment newInstance() {
        return new DonationLinksFragment();
    }

    public static void showDonationLinksDialog(@NonNull FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = DonationLinksFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalStateException | IllegalArgumentException ignored) {
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog dialog = new MaterialDialog.Builder(requireActivity())
                .customView(R.layout.fragment_other_apps, false)
                .typeface(TypefaceHelper.getMedium(requireActivity()), TypefaceHelper.getRegular(requireActivity()))
                .title(R.string.donate)
                .positiveText(R.string.close)
                .build();
        dialog.show();

        ListView listView = (ListView) dialog.findViewById(R.id.listview);
        List<CandyBarApplication.DonationLink> donationLinks = CandyBarApplication.getConfiguration().getDonationLinks();
        if (donationLinks != null) {
            listView.setAdapter(new OtherAppsAdapter(requireActivity(), donationLinks));
        } else {
            dismiss();
        }

        return dialog;
    }
}
