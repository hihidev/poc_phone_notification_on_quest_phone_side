package dev.hihi.questphoneapp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

// Copy from https://www.stacktips.com/tutorials/android/how-to-get-list-of-installed-apps-in-android
public class BlacklistActivity extends ListActivity {

    private static final String TAG = "BlacklistActivity";

    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private ApplicationAdapter listadaptor = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        packageManager = getPackageManager();

        new LoadApplications().execute();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ApplicationInfo app = applist.get(position);
        try {
            Intent intent = packageManager
                    .getLaunchIntentForPackage(app.packageName);

            if (null != intent) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(BlacklistActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(BlacklistActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            Set<String> blacklist = BlacklistUtils.getBlacklist(BlacklistActivity.this);
            List<ApplicationInfo> appInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            ArrayList<Pair<ApplicationInfo, Boolean>> appInfoBlacklistPairList = new ArrayList<>();
            for (String pkg : blacklist) {
                Log.i(TAG, "Blacklist pkg: " + pkg);
            }
            for (ApplicationInfo info : appInfoList) {
                appInfoBlacklistPairList.add(Pair.create(info, blacklist.contains(info.packageName)));
            }
            // TODO: Sort it by app name not package name...
            Collections.sort(appInfoBlacklistPairList, new Comparator<Pair<ApplicationInfo, Boolean>>() {
                @Override
                public int compare(Pair<ApplicationInfo, Boolean> a, Pair<ApplicationInfo, Boolean> b) {
                    if (a.second) {
                        return -1;
                    } else if (b.second) {
                        return 1;
                    } else {
                        return a.first.packageName.compareTo(b.first.packageName);
                    }
                }
            });
            listadaptor = new ApplicationAdapter(BlacklistActivity.this,
                    R.layout.snippet_list_row, appInfoBlacklistPairList);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(listadaptor);
            progress.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BlacklistActivity.this, null,
                    "Loading application info...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}