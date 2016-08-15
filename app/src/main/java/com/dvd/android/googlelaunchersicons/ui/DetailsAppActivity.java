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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.inquiry.Inquiry;
import com.dvd.android.googlelaunchersicons.R;
import com.dvd.android.googlelaunchersicons.iconpack.IconPack;
import com.dvd.android.googlelaunchersicons.list.AppsAdapter;
import com.dvd.android.googlelaunchersicons.list.IconsAdapter;
import com.dvd.android.googlelaunchersicons.list.ItemClickListener;
import com.dvd.android.googlelaunchersicons.model.App;
import com.dvd.android.googlelaunchersicons.utils.RowItem;
import com.dvd.android.googlelaunchersicons.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnLongClick;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;
import static com.dvd.android.googlelaunchersicons.utils.Utils.DATABASE_NAME;

/**
 * @author dvdandroid
 */
public class DetailsAppActivity extends AppCompatActivity {

    static final ButterKnife.Action<View> SHOW = new ButterKnife.Action<View>() {
        @Override
        public void apply(@NonNull View view, int index) {
            if (index == 0) view.setVisibility(View.GONE);
            else view.setVisibility(View.VISIBLE);
        }
    };

    static final ButterKnife.Action<View> HIDE = new ButterKnife.Action<View>() {
        @Override
        public void apply(@NonNull View view, int index) {
            if (index == 0) view.setVisibility(View.VISIBLE);
            else view.setVisibility(View.GONE);
        }
    };

    @BindView(R.id.label_row) RowItem labelRow;
    @BindView(R.id.pkg_name_row) RowItem pkgNameRow;
    @BindView(R.id.version_row) RowItem versionRow;
    @BindView(R.id.last_updated_row) RowItem lastUpdatedRow;
    @BindView(R.id.components_row) RowItem componentsRow;

    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.original_icon) ImageView originalIcon;

    @BindViews({R.id.suggestedIcons_loading, R.id.suggestedIcons_rv, R.id.header_suggested_icons})
    List<View> views;

    @BindView(R.id.suggestedIcons_loading) ProgressBar loadingSuggestedIcons;

    @BindView(R.id.suggestedIcons_rv) RecyclerView rvSuggestedIcons;
    @BindView(R.id.iconsPacks_rv) RecyclerView rvIconsPack;

    @BindView(R.id.header_suggested_icons) TextView tvSuggestedIcons;
    @BindView(R.id.header_iconpacks) TextView tvIconPacks;

    private App mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.pick_icon);

        mApp = MainActivity.selectedApp;
        try {
            mApp.loadIcon(this);
        } catch (NullPointerException e) {
            finish();
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, R.string.app_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }

        try {
            Inquiry.newInstance(this, DATABASE_NAME).databaseVersion(Utils.get(this).getDbVersion()).build();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

        labelRow.setSummary(mApp.label.toString());
        pkgNameRow.setSummary(mApp.packageName);
        versionRow.setSummary(mApp.version);
        lastUpdatedRow.setSummary(mApp.getFormattedDate());
        componentsRow.setSummary(mApp.componentName);

        icon.setImageDrawable(Utils.get(this).byteArrayToDrawable(mApp.icon));
        originalIcon.setImageDrawable(Utils.get(this).byteArrayToDrawable(mApp.originalIcon));

        ButterKnife.apply(views, HIDE);

        rvSuggestedIcons.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
        rvIconsPack.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        rvIconsPack.setAdapter(new AppsAdapter(DetailsAppActivity.this, Utils.ICON_PACK_LIST, new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(DetailsAppActivity.this, IconChoosingActivity.class);
                intent.putExtra("position", position);

                startActivityForResult(intent, 1234);
            }
        }));

        new GetSuggestedIcons().execute();
    }

    @OnLongClick(R.id.icon)
    boolean setDefaultIcon() {
        setupIcons(mApp.originalIcon);

        icon.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        return true;
    }

    private void setupIcons(byte[] bytes) {
        setResult(RESULT_OK);

        Drawable d = Utils.get(this).byteArrayToDrawable(bytes);

        mApp.icon = bytes;
        mApp.icon_low_res = Utils.get(this).drawableToByteArray(d, true);
        icon.setImageDrawable(d);
    }

    private void setupIcons(Drawable d) {
        setResult(RESULT_OK);

        mApp.icon = Utils.get(this).drawableToByteArray(d, false);
        mApp.icon_low_res = Utils.get(this).drawableToByteArray(d, true);
        icon.setImageDrawable(d);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) Inquiry.destroy(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.manage_app:
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mApp.packageName, null);
                intent.setData(uri);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1234 && resultCode == RESULT_OK && data != null) {
            setupIcons(data.getByteArrayExtra("icon"));
        }
    }

    private class GetSuggestedIcons extends AsyncTask<Void, Void, List<Drawable>> {

        @Override
        protected List<Drawable> doInBackground(Void... voids) {
            List<Drawable> icons = new ArrayList<>();

            for (IconPack iconPack : Utils.ICON_PACK_LIST) {
                Drawable icon = iconPack.getDrawableIconForPackage(mApp.packageName);
                if (icon != null) {
                    icons.add(icon);
                }

                iconPack.unload();
            }

            return icons;
        }

        @Override
        protected void onPostExecute(final List<Drawable> drawables) {
            super.onPostExecute(drawables);

            ButterKnife.apply(views, SHOW);

            loadingSuggestedIcons.setVisibility(View.GONE);

            if (drawables.size() == 0) tvSuggestedIcons.setVisibility(View.GONE);

            rvSuggestedIcons.setAdapter(new IconsAdapter(drawables, new ItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    setupIcons(drawables.get(position));
                }
            }));
        }
    }
}