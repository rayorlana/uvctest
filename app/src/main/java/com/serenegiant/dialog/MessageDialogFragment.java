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

package com.serenegiant.dialog;

import android.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class MessageDialogFragment extends DialogFragment {
    
    public interface MessageDialogListener {
        void onMessageDialogResult(final MessageDialogFragment dialog, final int requestCode, 
                                 final String[] permissions, final boolean result);
    }
    
    private static final String ARGS_KEY_REQUEST_CODE = "request_code";
    private static final String ARGS_KEY_TITLE_ID = "title_id";
    private static final String ARGS_KEY_MESSAGE_ID = "message_id";
    private static final String ARGS_KEY_PERMISSIONS = "permissions";
    
    public static MessageDialogFragment showDialog(final android.app.Fragment parent, final int requestCode,
                                                 final int title, final int message, final String... permissions) {
        final MessageDialogFragment dialog = newInstance(requestCode, title, message, permissions);
        dialog.setTargetFragment(parent, requestCode);
        return dialog;
    }
    
    public static MessageDialogFragment newInstance(final int requestCode, final int title, final int message, 
                                                   final String... permissions) {
        final MessageDialogFragment fragment = new MessageDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(ARGS_KEY_REQUEST_CODE, requestCode);
        args.putInt(ARGS_KEY_TITLE_ID, title);
        args.putInt(ARGS_KEY_MESSAGE_ID, message);
        args.putStringArray(ARGS_KEY_PERMISSIONS, permissions);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final int requestCode = args.getInt(ARGS_KEY_REQUEST_CODE);
        final int title = args.getInt(ARGS_KEY_TITLE_ID);
        final int message = args.getInt(ARGS_KEY_MESSAGE_ID);
        final String[] permissions = args.getStringArray(ARGS_KEY_PERMISSIONS);
        
        return new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callOnMessageDialogResult(requestCode, permissions, true);
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callOnMessageDialogResult(requestCode, permissions, false);
                }
            })
            .create();
    }
    
    private void callOnMessageDialogResult(final int requestCode, final String[] permissions, 
                                         final boolean result) {
        final android.app.Fragment target = getTargetFragment();
        if (target instanceof MessageDialogListener) {
            ((MessageDialogListener) target).onMessageDialogResult(this, requestCode, permissions, result);
        }
    }
} 