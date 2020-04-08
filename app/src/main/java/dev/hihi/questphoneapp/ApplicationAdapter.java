package dev.hihi.questphoneapp;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

// Copy from https://www.stacktips.com/tutorials/android/how-to-get-list-of-installed-apps-in-android
public class ApplicationAdapter extends ArrayAdapter<Pair<ApplicationInfo, Boolean>> {
    private List<Pair<ApplicationInfo, Boolean>> appsList = null;
    private Context context;
    private PackageManager packageManager;

    public ApplicationAdapter(Context context, int textViewResourceId,
            List<Pair<ApplicationInfo, Boolean>> appsList) {
        super(context, textViewResourceId, appsList);
        this.context = context;
        this.appsList = appsList;
        packageManager = context.getPackageManager();
    }

    @Override
    public int getCount() {
        return ((null != appsList) ? appsList.size() : 0);
    }

    @Override
    public Pair<ApplicationInfo, Boolean> getItem(int position) {
        return ((null != appsList) ? appsList.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.snippet_list_row, null);
        }

        final Pair<ApplicationInfo, Boolean> applicationInfo = appsList.get(position);
        if (null != applicationInfo) {
            TextView appName = (TextView) view.findViewById(R.id.app_name);
            TextView packageName = (TextView) view.findViewById(R.id.app_paackage);
            ImageView iconview = (ImageView) view.findViewById(R.id.app_icon);
            // Seriously...use convertView and don't findviewbyid everytime..
            CheckBox checkbox = view.findViewById(R.id.whitelisted_checkbox);

            appName.setText(applicationInfo.first.loadLabel(packageManager));
            packageName.setText(applicationInfo.first.packageName);
            iconview.setImageDrawable(applicationInfo.first.loadIcon(packageManager));
            checkbox.setChecked(applicationInfo.second);
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!compoundButton.isPressed()) {
                        return;
                    }
                    if (b) {
                        BlacklistUtils.addBlacklist(getContext(), applicationInfo.first.packageName);
                    } else {
                        BlacklistUtils.removeBlacklist(getContext(), applicationInfo.first.packageName);
                    }
                    appsList.set(position, Pair.create(applicationInfo.first, b));
                }
            });
        }
        return view;
    }
};