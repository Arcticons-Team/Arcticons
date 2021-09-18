package candybar.lib.adapters.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import candybar.lib.R;
import candybar.lib.items.License;

public class LicensesAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<License> mLicenses;

    public LicensesAdapter(@NonNull Context context, @NonNull List<License> licenses) {
        mContext = context;
        mLicenses = licenses;
    }

    @Override
    public int getCount() {
        return mLicenses.size();
    }

    @Override
    public Object getItem(int position) {
        return mLicenses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_licenses_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        License license = mLicenses.get(position);
        holder.title.setText(license.getName());
        holder.body.setText(license.getLicenseText());

        return view;
    }

    private static class ViewHolder {

        private final TextView title;
        private final TextView body;

        ViewHolder(View view) {
            title = view.findViewById(R.id.title);
            body = view.findViewById(R.id.body);
        }
    }
}
