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

package com.dvd.android.googlelaunchersicons.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.afollestad.inquiry.Inquiry;
import com.dvd.android.googlelaunchersicons.model.App;
import com.dvd.android.googlelaunchersicons.utils.Utils;

import eu.chainfire.libsuperuser.Shell;

import static com.dvd.android.googlelaunchersicons.utils.Utils.BAK_DATABASE_NAME;
import static com.dvd.android.googlelaunchersicons.utils.Utils.DATABASE_NAME;
import static com.dvd.android.googlelaunchersicons.utils.Utils.LAUNCHER_PKG_NAME;
import static com.dvd.android.googlelaunchersicons.utils.Utils.TAG;
import static com.dvd.android.googlelaunchersicons.utils.Utils.THIS_DB_PATH;
import static com.dvd.android.googlelaunchersicons.utils.Utils.mDatabasePath;

/**
 * @author dvdandroid
 */
public class PackageUpdatedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.get(context).loadFromPrefs();

        if (LAUNCHER_PKG_NAME == null) {
            Log.w(TAG, "No compatible launchers installed!");
            return;
        }

        try {
            Log.i(TAG, "Broadcast received");

            Uri uri = intent.getData();
            String packageName = (uri != null) ? uri.getSchemeSpecificPart() : null;

            if (packageName == null) return;

            Log.i(TAG, "Package name: " + packageName);

            Utils.get(context);

            Inquiry.newInstance(context, BAK_DATABASE_NAME).databaseVersion(Utils.get(context).getDbVersion()).build();
            App backupApp = Inquiry.get(context).selectFrom("icons", App.class).where("componentName NOT LIKE ?", packageName + "%.").one();
            Inquiry.destroy(context);

            if (backupApp == null) {
                Log.d(TAG, "No compatible backup app found with package name " + packageName);
                return;
            }

            Inquiry.newInstance(context, DATABASE_NAME).databaseVersion(Utils.get(context).getDbVersion()).build();

            Inquiry.get(context)
                    .update("icons", App.class)
                    .where("componentName = ?", backupApp.componentName)
                    .values(backupApp)
                    .projection("icon", "icon_low_res")
                    .run();

            Inquiry.destroy(context);

            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            am.killBackgroundProcesses(LAUNCHER_PKG_NAME); // Kill launcher

            Shell.Builder builder = new Shell.Builder().useSU();

            Shell.Interactive shell = builder.open();
            shell.addCommand(String.format("cp -r %1$s %2$s", THIS_DB_PATH, mDatabasePath));

            Log.i(TAG, packageName + ": icon replaced");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
