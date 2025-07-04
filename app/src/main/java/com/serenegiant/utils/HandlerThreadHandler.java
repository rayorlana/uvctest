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

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class HandlerThreadHandler extends Handler {
    private static final String TAG = "HandlerThreadHandler";
    
    private HandlerThreadHandler(final HandlerThread thread) {
        super(thread.getLooper());
    }
    
    public static final HandlerThreadHandler createHandler(final String name) {
        return createHandler(name, Thread.NORM_PRIORITY);
    }
    
    public static final HandlerThreadHandler createHandler(final String name, final int priority) {
        final HandlerThread thread = new HandlerThread(name, priority);
        thread.start();
        return new HandlerThreadHandler(thread);
    }
    
    public boolean quitSafely() {
        boolean result = false;
        try {
            if (BuildCheck.isAPI18()) {
                if (getLooper().getThread().isAlive()) {
                    getLooper().quitSafely();
                    result = true;
                } else {
                    result = false;
                }
            } else {
                result = getLooper().getThread().isAlive();
                if (result) {
                    getLooper().quit();
                }
            }
        } catch (final Exception e) {
            Log.w(TAG, e);
        }
        return result;
    }
    
    public boolean isAlive() {
        boolean result = false;
        try {
            result = getLooper().getThread().isAlive();
        } catch (final Exception e) {
            Log.w(TAG, e);
        }
        return result;
    }
} 