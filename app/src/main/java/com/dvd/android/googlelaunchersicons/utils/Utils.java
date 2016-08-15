package com.dvd.android.googlelaunchersicons.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.dvd.android.googlelaunchersicons.iconpack.IconPack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * @author dvdandroid
 */
@SuppressLint("SdCardPath")
public class Utils {

    public static final String TAG = "GLIC";

    public static final String NEXUS_LAUNCHER_PKG_NAME = "com.google.android.apps.nexuslauncher";
    public static final String GOOGLE_LAUNCHER_PKG_NAME = "com.google.android.launcher";
    public static final String GOOGLE_APP_PKG_NAME = "com.google.android.googlequicksearchbox";

    private static final File NEXUS_LAUNCHER_DB_PATH = new File("/data/data/com.google.android.apps.nexuslauncher/databases/app_icons.db");
    private static final File GOOGLE_LAUNCHER_DB_PATH = new File("/data/data/com.google.android.googlequicksearchbox/databases/app_icons.db");

    private static final File DATABASE_FOLDER = new File("/data/data/com.dvd.android.googlelaunchersicons/databases/");

    public static File THIS_DB_PATH;
    public static File mDatabasePath;
    public static String DATABASE_NAME;
    public static String BAK_DATABASE_NAME;
    public static String LAUNCHER_PKG_NAME;

    public static List<IconPack> ICON_PACK_LIST;

    private static File THIS_DB_PATH_BAK;
    private static Utils sInstance;
    private final Context context;

    private Utils(Context context) {
        this.context = context;
    }

    public static Utils get(Context context) {
        if (sInstance == null) {
            sInstance = new Utils(context);
        }
        return sInstance;
    }

    private static String messageForError(int code) {
        switch (code) {
            case RootCallback.ERROR_TIMEOUT:
                return "Timeout occured";
            case RootCallback.ERROR_SHELL_DIED:
                return "Execution aborted unexpectedly";
            case RootCallback.ERROR_EXEC_FAILED:
            case RootCallback.ERROR_WRONG_UID:
                return "Could not gain root access";
            default:
                return "Error " + code + " occurred";
        }
    }

    private static void triggerError(RootCallback callback, int code) {
        callback.onError(messageForError(code));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public int getDbVersion() {
        THIS_DB_PATH.setReadable(true);
        THIS_DB_PATH.setWritable(true);

        try {
            RandomAccessFile fp = new RandomAccessFile(THIS_DB_PATH, "r");
            fp.seek(60);
            byte[] buff = new byte[4];
            fp.read(buff, 0, 4);
            return ByteBuffer.wrap(buff).getInt();
        } catch (Exception e) {
            return -1;
        }
    }

    public byte[] drawableToByteArray(Drawable d, boolean resize) {
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();

        if (resize) bitmap = Bitmap.createScaledBitmap(bitmap, 32, 32, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public Drawable byteArrayToDrawable(byte[] bytes) {
        return new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
    }

    public void getDatabase(RootCallback callback, boolean nexus) {
        mDatabasePath = nexus ? NEXUS_LAUNCHER_DB_PATH : GOOGLE_LAUNCHER_DB_PATH;

        LAUNCHER_PKG_NAME = nexus ? NEXUS_LAUNCHER_PKG_NAME : GOOGLE_APP_PKG_NAME;

        DATABASE_NAME = nexus ? "nexus_app_icon" : "google_app_icon";
        BAK_DATABASE_NAME = DATABASE_NAME + "_bak.db";

        DATABASE_NAME += ".db";

        THIS_DB_PATH = new File("/data/data/com.dvd.android.googlelaunchersicons/databases/" + DATABASE_NAME);
        THIS_DB_PATH_BAK = new File("/data/data/com.dvd.android.googlelaunchersicons/databases/" + BAK_DATABASE_NAME);

        getPreferences().edit().putString("mDatabasePath", mDatabasePath.getPath())
                .putString("LAUNCHER_PKG_NAME", LAUNCHER_PKG_NAME)
                .putString("DATABASE_NAME", DATABASE_NAME)
                .putString("BAK_DATABASE_NAME", BAK_DATABASE_NAME)
                .putString("THIS_DB_PATH", THIS_DB_PATH.getPath())
                .putString("THIS_DB_PATH_BAK", THIS_DB_PATH_BAK.getPath())
                .apply();

        //noinspection ResultOfMethodCallIgnored
        DATABASE_FOLDER.mkdir();

        Shell.Builder builder = new Shell.Builder().useSU().setOnSTDERRLineListener(new StderrListener(callback));

        Shell.Interactive shell = builder.open(new OpenListener(callback));
        shell.addCommand(String.format("cp -r %1$s %2$s", mDatabasePath, THIS_DB_PATH), 0, new StdoutListener(callback));
        shell.addCommand("chmod 666 " + THIS_DB_PATH);
    }

    public void copyDatabase(RootCallback callback) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(LAUNCHER_PKG_NAME); // Kill launcher

        Shell.Builder builder = new Shell.Builder().useSU().setOnSTDERRLineListener(new StderrListener(callback));

        Shell.Interactive shell = builder.open(new OpenListener(callback));
        shell.addCommand(String.format("cp -r %1$s %2$s", THIS_DB_PATH, mDatabasePath), 0, new StdoutListener(callback));
    }

    public void makeBackup() throws Exception {
        InputStream in = new FileInputStream(THIS_DB_PATH);
        OutputStream out = new FileOutputStream(THIS_DB_PATH_BAK);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public SharedPreferences getPreferences() {
        return context.getSharedPreferences("com.dvd.android.googlelaunchersicons_preferences", Context.MODE_PRIVATE);
    }

    public void loadFromPrefs() {
        BAK_DATABASE_NAME = getPreferences().getString("BAK_DATABASE_NAME", null);
        DATABASE_NAME = getPreferences().getString("DATABASE_NAME", null);
        LAUNCHER_PKG_NAME = getPreferences().getString("LAUNCHER_PKG_NAME", null);
        mDatabasePath = new File(getPreferences().getString("mDatabasePath", ""));
        THIS_DB_PATH = new File(getPreferences().getString("THIS_DB_PATH", ""));
    }

    private static class OpenListener implements Shell.OnCommandResultListener {
        private final RootCallback callback;

        public OpenListener(RootCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
            if (exitCode != SHELL_RUNNING) {
                triggerError(callback, exitCode);
            }
        }
    }

    private static class StdoutListener implements Shell.OnCommandLineListener {
        private final RootCallback callback;

        public StdoutListener(RootCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLine(String line) {
            callback.onLine(line);
        }

        @Override
        public void onCommandResult(int commandCode, int exitCode) {
            if (exitCode == RootCallback.OK) {
                callback.onDone();
            } else {
                triggerError(callback, exitCode);
            }
        }
    }

    private static class StderrListener implements Shell.OnCommandLineListener {
        private final RootCallback callback;

        public StderrListener(RootCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLine(String line) {
            callback.onErrorLine(line);
        }

        @Override
        public void onCommandResult(int commandCode, int exitCode) {
            // Not called for STDERR listener.
        }
    }

}