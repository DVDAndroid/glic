package com.dvd.android.googlelaunchersicons.utils;

import eu.chainfire.libsuperuser.Shell;

/**
 * @author dvdandroid
 */
public interface RootCallback {

    int OK = 0;

    // SU errors
    int ERROR_TIMEOUT = Shell.OnCommandResultListener.WATCHDOG_EXIT;
    int ERROR_SHELL_DIED = Shell.OnCommandResultListener.SHELL_DIED;
    int ERROR_EXEC_FAILED = Shell.OnCommandResultListener.SHELL_EXEC_FAILED;
    int ERROR_WRONG_UID = Shell.OnCommandResultListener.SHELL_WRONG_UID;

    void onLine(String line);

    void onErrorLine(String line);

    void onDone();

    void onError(String error);

}