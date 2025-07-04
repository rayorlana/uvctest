/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.serenegiant.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class PermissionCheck {
    
    /**
     * Check if the app has a specific permission
     */
    public static boolean hasPermission(final Context context, final String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if the app has camera permission
     */
    public static boolean hasCamera(final Context context) {
        return hasPermission(context, Manifest.permission.CAMERA);
    }
    
    /**
     * Check if the app has audio recording permission
     */
    public static boolean hasAudio(final Context context) {
        return hasPermission(context, Manifest.permission.RECORD_AUDIO);
    }
    
    /**
     * Check if the app has network access permission
     */
    public static boolean hasNetwork(final Context context) {
        return hasPermission(context, Manifest.permission.INTERNET);
    }
    
    /**
     * Check if the app has external storage write permission
     */
    public static boolean hasWriteExternalStorage(final Context context) {
        if (BuildCheck.isAPI29()) {
            // On Android 10+, scoped storage is used, no need for this permission
            return true;
        }
        return hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    
    /**
     * Check if the app has external storage read permission
     */
    public static boolean hasReadExternalStorage(final Context context) {
        if (BuildCheck.isAPI29()) {
            // On Android 10+, scoped storage is used for most cases
            return true;
        }
        return hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }
} 