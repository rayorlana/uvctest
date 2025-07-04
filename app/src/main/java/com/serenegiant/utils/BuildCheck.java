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

import android.os.Build;

public final class BuildCheck {
    private static final boolean check(final int API_LEVEL) {
        return Build.VERSION.SDK_INT >= API_LEVEL;
    }

    public static boolean isAPI14() {
        return check(Build.VERSION_CODES.ICE_CREAM_SANDWICH);
    }

    public static boolean isAPI15() {
        return check(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1);
    }

    public static boolean isAPI16() {
        return check(Build.VERSION_CODES.JELLY_BEAN);
    }

    public static boolean isAPI17() {
        return check(Build.VERSION_CODES.JELLY_BEAN_MR1);
    }

    public static boolean isAPI18() {
        return check(Build.VERSION_CODES.JELLY_BEAN_MR2);
    }

    public static boolean isKitKat() {
        return check(Build.VERSION_CODES.KITKAT);
    }

    public static boolean isLollipop() {
        return check(Build.VERSION_CODES.LOLLIPOP);
    }

    public static boolean isAndroid5() {
        return check(Build.VERSION_CODES.LOLLIPOP);
    }

    public static boolean isMarshmallow() {
        return check(Build.VERSION_CODES.M);
    }

    public static boolean isNougat() {
        return check(Build.VERSION_CODES.N);
    }

    public static boolean isOreo() {
        return check(Build.VERSION_CODES.O);
    }

    public static boolean isAPI26() {
        return check(Build.VERSION_CODES.O);
    }

    public static boolean isAPI28() {
        return check(Build.VERSION_CODES.P);
    }

    public static boolean isAPI29() {
        return check(Build.VERSION_CODES.Q);
    }

    public static boolean isAPI30() {
        return check(Build.VERSION_CODES.R);
    }
} 