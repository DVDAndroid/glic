/*
 * Copyright 2016 dvdandroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dvd.android.googlelaunchersicons.ui;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.inquiry.Inquiry;
import com.afollestad.inquiry.callbacks.GetCallback;
import com.dvd.android.googlelaunchersicons.R;
import com.dvd.android.googlelaunchersicons.iconpack.IconPack;
import com.dvd.android.googlelaunchersicons.iconpack.IconPackManager;
import com.dvd.android.googlelaunchersicons.list.AppsAdapter;
import com.dvd.android.googlelaunchersicons.list.DividerItemDecoration;
import com.dvd.android.googlelaunchersicons.list.ItemClickListener;
import com.dvd.android.googlelaunchersicons.model.App;
import com.dvd.android.googlelaunchersicons.utils.RootCallback;
import com.dvd.android.googlelaunchersicons.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.dvd.android.googlelaunchersicons.list.DividerItemDecoration.VERTICAL_LIST;
import static com.dvd.android.googlelaunchersicons.utils.Utils.DATABASE_NAME;
import static com.dvd.android.googlelaunchersicons.utils.Utils.GOOGLE_LAUNCHER_PKG_NAME;
import static com.dvd.android.googlelaunchersicons.utils.Utils.LAUNCHER_PKG_NAME;
import static com.dvd.android.googlelaunchersicons.utils.Utils.NEXUS_LAUNCHER_PKG_NAME;
import static com.dvd.android.googlelaunchersicons.utils.Utils.TAG;

/**
 * @author dvdandroid
 */
public class MainActivity extends AppCompatActivity implements RootCallback {

    public static App selectedApp;

    @BindView(R.id.recyclerView) FastScrollRecyclerView mRecycler;
    @BindView(R.id.error_view) View mErrorView;
    @BindView(R.id.error) TextView mErrorTv;

    private ProgressDialog mProgress;
    private App[] mLoadedApps;
    private List<IconPack> mIconsPacks;
    private List<Integer> mChangedApps = new ArrayList<>();

    private IconPack mSelectedIconPack;

    private MenuItem mSaveChanges;
    private MenuItem mFastPicking;

    private boolean changesMade = false;
    private int lastPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("GLIC");

        mProgress = ProgressDialog.show(this, null, getString(R.string.loading), true, false);
        mIconsPacks = IconPackManager.getAvailableIconPacks(this);

        boolean nexusLauncher = isAppInstalled(NEXUS_LAUNCHER_PKG_NAME);
        boolean googleNowLauncher = isAppInstalled(GOOGLE_LAUNCHER_PKG_NAME);

        // If Nexus Launcher and Google Now Launcher are not installed, skip
        if (!(nexusLauncher || googleNowLauncher)) {
            appendLine(getString(R.string.launcher_not_installed), false);
            return;
        }

        // If no icon packs are installed, skip
        if (mIconsPacks.size() == 0) {
            appendLine(getString(R.string.no_icons_pack), false);
            return;
        }

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, VERTICAL_LIST));

        int prefLauncher = Integer.parseInt(Utils.get(this).getPreferences().getString("pref_launcher", "0"));

        if (prefLauncher == 1 || !googleNowLauncher) {
            // If preferred launcher is Nexus Launcher or Google Now Launcher is not installed
            // load Nexus Launcher
            Utils.get(this).getDatabase(this, true);
        } else if (prefLauncher == 2 || !nexusLauncher) {
            // If preferred launcher is Google Now Launcher or Nexus Launcher is not installed
            // load Google Now Launcher
            Utils.get(this).getDatabase(this, false);
        } else if (prefLauncher == 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.choose_launcher)
                    .setMessage(R.string.choose_launcher_)
                    .setCancelable(false)
                    .setPositiveButton(R.string.nexus_launcher, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Utils.get(MainActivity.this).getDatabase(MainActivity.this, true);
                        }
                    })
                    .setNegativeButton(R.string.google_launcher, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Utils.get(MainActivity.this).getDatabase(MainActivity.this, false);
                        }
                    })
                    .show();
        }

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFastPicking.setVisible(true);
            }
        }, 1000);
    }

    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) {
            Inquiry.destroy(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mSaveChanges = menu.findItem(R.id.save_changes);
        mSaveChanges.setVisible(false);

        mFastPicking = menu.findItem(R.id.fast_picking);
        mFastPicking.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    @SuppressLint("InflateParams")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fast_picking:
                mSelectedIconPack = null;

                View view = LayoutInflater.from(this).inflate(R.layout.pick_icon_pack_dialog, null);

                RecyclerView iconPackRv = ButterKnife.findById(view, R.id.iconsPacks_rv);
                final TextView tv = ButterKnife.findById(view, R.id.selectedIconPack);

                iconPackRv.setAdapter(new AppsAdapter(this, mIconsPacks, new ItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        mSelectedIconPack = mIconsPacks.get(position);
                        tv.setText(getString(R.string.selected_icon_pack, mSelectedIconPack.label));
                    }
                }));

                final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

                dialog.setTitle(R.string.select_icon_pack);
                dialog.setView(view);
                dialog.setCancelable(false);
                dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mSelectedIconPack == null) {
                            Toast.makeText(MainActivity.this, R.string.no_icon_pack_selected, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new SetDefaultIcons().execute(mSelectedIconPack);
                    }
                });
                dialog.setNegativeButton(android.R.string.cancel, null);
                dialog.show();

                return true;
            case R.id.save_changes:
                mSaveChanges.setVisible(false);
                new SaveChanges().execute();
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.app_info:
                AlertDialog d = new AlertDialog.Builder(this)
                        .setTitle(R.string.app_info)
                        .setMessage(R.string.app_info_)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();

                d.show();

                //noinspection ConstantConditions
                ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLine(String line) {
        appendLine(line, true);
    }

    @Override
    public void onErrorLine(String line) {
        appendLine(line, true);
        Log.e(TAG, "Error: " + line);
    }

    @Override
    public void onDone() {
        if (Utils.get(this).getDbVersion() == -1) {
            onDone();
            return;
        }

        Inquiry.newInstance(this, DATABASE_NAME).databaseVersion(Utils.get(this).getDbVersion()).build();

        Inquiry.get(this)                                                       // Get db
                .selectFrom("icons", App.class)                                 // Get table "icons"
                .where("componentName NOT LIKE ?", "%.")                        // Get all app with component name that not ends with "."
                .sortByAsc("label")                                             // Sort them by label ascending
                .all(new GetCallback<App>() {
                    @Override
                    public void result(@Nullable App[] apps) {
                        if (apps == null) {
                            appendLine(getString(R.string.apps_not_found), false);
                            return;
                        }

                        mLoadedApps = apps;
                        new LoadApps().execute();
                    }
                });
    }

    @Override
    public void onBackPressed() {

        if (changesMade) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.changes_made)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveChanges();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setCancelable(false).show();
        } else {
            super.onBackPressed();
        }

    }

    private void saveChanges() {
        ProgressDialog d = ProgressDialog.show(this, null, getString(R.string.saving_changes), true, false);

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(LAUNCHER_PKG_NAME); // Kill launcher

        try {
            Utils.get(this).makeBackup();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error: " + e.getMessage());
        }

        Utils.get(this).copyDatabase(this);

        d.dismiss();
        changesMade = false;

        Toast.makeText(this, R.string.changes_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK) {
            mRecycler.getAdapter().notifyDataSetChanged();
            changesMade = true;
            mSaveChanges.setVisible(true);

            mChangedApps.add(lastPosition);
        }
    }

    @Override
    public void onError(String error) {
        appendLine(error, true);
    }

    @SuppressLint("SetTextI18n")
    private void appendLine(String s, boolean append) {
        mProgress.dismiss();
        mErrorView.setVisibility(View.VISIBLE);
        mErrorTv.setText(append ? mErrorTv.getText() + "\n" + s : s);
    }

    private class LoadApps extends AsyncTask<App, Integer, App[]> {

        @Override
        protected App[] doInBackground(App... apps) {
            for (int i = 0; i < mLoadedApps.length; i++) {
                App a = mLoadedApps[i];
                a.loadPackageName();

                publishProgress(i, apps.length);
            }

            return mLoadedApps;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            mProgress.setMessage(getString(R.string.loading_, values[0], mLoadedApps.length));
        }

        @Override
        protected void onPostExecute(final App[] result) {
            super.onPostExecute(result);

            mRecycler.setAdapter(new AppsAdapter(MainActivity.this, result, new ItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    selectedApp = result[position];
                    lastPosition = position;

                    startActivityForResult(new Intent(MainActivity.this, DetailsAppActivity.class), 123);
                }
            }));

            mProgress.dismiss();
        }
    }

    private class SetDefaultIcons extends AsyncTask<App, Integer, Boolean> {

        private int nullIcons = 0;
        private int appliedIcons = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgress.show();
        }

        @Override
        protected Boolean doInBackground(App... iconPacks) {
            IconPack iconPack = (IconPack) iconPacks[0];

            for (int i = 0; i < mLoadedApps.length; i++) {
                App app = mLoadedApps[i];
                Drawable customIcon = iconPack.getDrawableIconForPackage(app.packageName);

                if (customIcon == null) {
                    nullIcons++;
                } else {
                    appliedIcons++;

                    app.icon = Utils.get(MainActivity.this).drawableToByteArray(customIcon, false);
                    app.icon_low_res = Utils.get(MainActivity.this).drawableToByteArray(customIcon, true);

                    mChangedApps.add(i);
                }

                publishProgress(i);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            mProgress.setMessage(getString(R.string.loading_, values[0], mLoadedApps.length));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mProgress.dismiss();
            mRecycler.getAdapter().notifyDataSetChanged();

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.result)
                    .setMessage(getString(R.string.result_, appliedIcons, nullIcons))
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .show();

            mSaveChanges.setVisible(true);
        }
    }

    private class SaveChanges extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgress.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for (int i = 0; i < mChangedApps.size(); i++) {
                Integer app = mChangedApps.get(i);
                Inquiry.get(MainActivity.this)
                        .update("icons", App.class)
                        .where("componentName = ?", mLoadedApps[app].componentName)
                        .projection("icon", "icon_low_res")
                        .values(mLoadedApps[app])
                        .run();

                publishProgress(i);
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            mProgress.setMessage(getString(R.string.loading_, values[0], mChangedApps.size()));
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            mProgress.dismiss();

            saveChanges();
            mChangedApps = new ArrayList<>();
        }
    }
}