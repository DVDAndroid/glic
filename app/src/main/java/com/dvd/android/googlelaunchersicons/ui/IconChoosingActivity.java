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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.dvd.android.googlelaunchersicons.R;
import com.dvd.android.googlelaunchersicons.iconpack.IconPack;
import com.dvd.android.googlelaunchersicons.list.IconsAdapter;
import com.dvd.android.googlelaunchersicons.list.ItemClickListener;
import com.dvd.android.googlelaunchersicons.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dvdandroid
 */
public class IconChoosingActivity extends AppCompatActivity {

    private Context mContext;
    private RecyclerView recyclerView;
    private List<Drawable> listIcons;
    private IconPack iconPack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerView = new RecyclerView(this);
        setContentView(recyclerView);

        if (getIntent().getExtras() == null) finish();

        mContext = this;

        int pos = getIntent().getExtras().getInt("position");

        iconPack = Utils.ICON_PACK_LIST.get(pos);
        listIcons = new ArrayList<>();

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.pick_icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new LoadIcons().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadIcons extends AsyncTask<IconPack, Integer, Boolean> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = ProgressDialog.show(mContext, null, getString(R.string.loading), true, false);
        }

        @Override
        protected Boolean doInBackground(IconPack... iconPacks) {
            List<String> allDrawables = iconPack.getAllDrawables();
            for (int i = 0; i < allDrawables.size(); i++) {
                String drawableName = allDrawables.get(i);

                if (drawableName == null) continue;

                Drawable drawable = iconPack.loadDrawable(drawableName);

                if (drawable == null) continue;

                listIcons.add(drawable);

                publishProgress(i, allDrawables.size());
            }

            iconPack.unload();
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            int percentage = (int) (100 * (float) values[0] / (float) values[1]);

            progress.setMessage(getString(R.string.loading__, percentage + "%"));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            progress.dismiss();

            recyclerView.setLayoutManager(new GridLayoutManager(mContext, 6));
            recyclerView.setAdapter(new IconsAdapter(listIcons, new ItemClickListener() {
                @Override
                public void onItemClick(final int position) {
                    showDialog(position);
                }
            }));

        }

        private void showDialog(int position) {
            final Drawable icon = listIcons.get(position);

            ImageView iv = (ImageView) LayoutInflater.from(IconChoosingActivity.this).inflate(R.layout.icon, null);

            iv.setImageDrawable(icon);

            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.selected_icon)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.putExtra("icon", Utils.get(mContext).drawableToByteArray(icon, false));
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setView(iv)
                    .show();
        }
    }
}
