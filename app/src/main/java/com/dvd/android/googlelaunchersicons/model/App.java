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

package com.dvd.android.googlelaunchersicons.model;

import android.content.Context;
import android.content.pm.PackageManager;

import com.afollestad.inquiry.annotations.Column;
import com.dvd.android.googlelaunchersicons.utils.Utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author dvdandroid
 */
public class App {

    public String packageName;
    public byte[] originalIcon;

    @Column public String componentName;
    @Column public long lastUpdated;
    @Column public long version;
    @Column public byte[] icon;
    @Column public byte[] icon_low_res;
    @Column public CharSequence label;

    public App() { }

    public void loadIcon(Context context) throws PackageManager.NameNotFoundException {
        originalIcon = Utils.get(context).drawableToByteArray(context.getPackageManager().getApplicationIcon(packageName), false);
    }

    public void loadPackageName() {
        packageName = componentName.split("/")[0];
    }

    public String getFormattedDate() {
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

        return f.format(new Date(lastUpdated));
    }

}