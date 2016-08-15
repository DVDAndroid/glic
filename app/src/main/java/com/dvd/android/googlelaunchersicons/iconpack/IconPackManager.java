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

package com.dvd.android.googlelaunchersicons.iconpack;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

import static com.dvd.android.googlelaunchersicons.utils.Utils.ICON_PACK_LIST;

public class IconPackManager {

    public static List<IconPack> getAvailableIconPacks(Context context) {

        String[] sIconPackCategories = new String[]{
                "com.fede.launcher.THEME_ICONPACK",
                "com.anddoes.launcher.THEME",
                "com.teslacoilsw.launcher.THEME"
        };
        String[] sIconPackActions = new String[]{
                "org.adw.launcher.THEMES",
                "com.gau.go.launcherex.theme"
        };

        Intent i = new Intent();
        List<IconPack> packages = new ArrayList<>();
        List<String> packagesName = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        for (String action : sIconPackActions) {
            i.setAction(action);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                IconPack iconPack = new IconPack(context, r);

                if (!packagesName.contains(iconPack.packageName)) {
                    packages.add(iconPack);
                    packagesName.add(iconPack.packageName);
                }
            }
        }

        i = new Intent(Intent.ACTION_MAIN);
        for (String category : sIconPackCategories) {
            i.addCategory(category);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                IconPack iconPack = new IconPack(context, r);

                if (!packagesName.contains(iconPack.packageName)) {
                    packages.add(iconPack);
                    packagesName.add(iconPack.packageName);
                }
            }
            i.removeCategory(category);
        }

        ICON_PACK_LIST = packages;

        return packages;
    }

}