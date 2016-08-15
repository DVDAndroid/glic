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