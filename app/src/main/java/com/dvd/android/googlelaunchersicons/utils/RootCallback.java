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