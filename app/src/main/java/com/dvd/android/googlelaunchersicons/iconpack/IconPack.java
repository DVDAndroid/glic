package com.dvd.android.googlelaunchersicons.iconpack;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.dvd.android.googlelaunchersicons.model.App;
import com.dvd.android.googlelaunchersicons.utils.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.dvd.android.googlelaunchersicons.utils.Utils.TAG;

public class IconPack extends App {

    private final Context mContext;
    private HashMap<String, String> mPackagesDrawables;
    private List<String> mAllDrawables;
    private Resources mIconPackRes = null;
    private boolean mLoaded = false;

    public IconPack(Context context, ResolveInfo resolveInfo) {
        this.mContext = context;
        this.label = resolveInfo.loadLabel(mContext.getPackageManager());
        this.packageName = resolveInfo.activityInfo.packageName;
        this.icon = Utils.get(mContext).drawableToByteArray(resolveInfo.loadIcon(mContext.getPackageManager()), false);
    }

    private void load() {
        PackageManager pm = mContext.getPackageManager();
        mAllDrawables = new ArrayList<>();
        mPackagesDrawables = new HashMap<>();
        try {
            XmlPullParser xpp = null;

            mIconPackRes = pm.getResourcesForApplication(packageName);
            int appFilterId = mIconPackRes.getIdentifier("appfilter", "xml", packageName);
            if (appFilterId > 0) {
                xpp = mIconPackRes.getXml(appFilterId);
            } else {
                try {
                    InputStream appFilterStream = mIconPackRes.getAssets().open("appfilter.xml");

                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    xpp = factory.newPullParser();
                    xpp.setInput(appFilterStream, "utf-8");
                } catch (IOException e1) {
                    Log.d(TAG, "No appfilter.xml file");
                }
            }

            if (xpp != null) {
                int eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("item")) {
                        String componentName = xpp.getAttributeValue(null, "component");
                        String drawableName = xpp.getAttributeValue(null, "drawable");

                        if (!mAllDrawables.contains(drawableName)) {
                            mAllDrawables.add(drawableName);
                        }

                        if (!mPackagesDrawables.containsKey(componentName))
                            mPackagesDrawables.put(componentName, drawableName);
                    }
                    eventType = xpp.next();
                }
            }
            mLoaded = true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Cannot load icon pack");
        } catch (XmlPullParserException e) {
            Log.d(TAG, "Cannot parse icon pack appfilter.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllDrawables() {
        if (!mLoaded) load();

        Collections.sort(mAllDrawables, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                if (s == null || t1 == null) return -1;

                return s.compareTo(t1);

            }
        });
        return mAllDrawables;
    }

    public Drawable loadDrawable(String drawableName) {
        int id = mIconPackRes.getIdentifier(drawableName, "drawable", packageName);
        if (id > 0) {
            return mIconPackRes.getDrawable(id);
        }
        return null;
    }

    public Drawable getDrawableIconForPackage(String appPackageName) {
        if (!mLoaded) load();

        mPackagesDrawables = new HashMap<>();
        PackageManager pm = mContext.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(appPackageName);
        String componentName = null;
        if (launchIntent != null)
            componentName = pm.getLaunchIntentForPackage(appPackageName).getComponent().toString();
        String drawable = mPackagesDrawables.get(componentName);
        if (drawable != null) {
            return loadDrawable(drawable);
        } else {
            // try to get a resource with the component filename
            if (componentName != null) {
                int start = componentName.indexOf("{") + 1;
                int end = componentName.indexOf("}", start);
                if (end > start) {
                    drawable = componentName.substring(start, end).toLowerCase(Locale.getDefault()).replace(".", "_").replace("/", "_");
                    if (mIconPackRes.getIdentifier(drawable, "drawable", packageName) > 0)
                        return loadDrawable(drawable);
                }
            }
        }
        return null;
    }

    public void unload() {
        mPackagesDrawables = null;
        mAllDrawables = null;
        mIconPackRes = null;
        mLoaded = false;
    }
}