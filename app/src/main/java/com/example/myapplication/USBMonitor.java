package com.example.myapplication;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

public class USBMonitor {
    private static final String TAG = "USBMonitor";
    private static final String ACTION_USB_PERMISSION = "com.example.myapplication.USB_PERMISSION";
    
    private final Context mContext;
    private final UsbManager mUsbManager;
    private final OnDeviceConnectListener mOnDeviceConnectListener;
    private PendingIntent mPermissionIntent;
    
    public interface OnDeviceConnectListener {
        void onAttach(UsbDevice device);
        void onDetach(UsbDevice device);
        void onConnect(UsbDevice device, boolean createNew);
        void onDisconnect(UsbDevice device);
        void onCancel(UsbDevice device);
    }
    
    public USBMonitor(Context context, OnDeviceConnectListener listener) {
        mContext = context;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mOnDeviceConnectListener = listener;
        
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, 
            new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        
        // Register receiver with proper export flag for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(mUsbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(mUsbReceiver, filter);
        }
    }
    
    public void destroy() {
        try {
            mContext.unregisterReceiver(mUsbReceiver);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }
    
    public void requestPermission(UsbDevice device) {
        if (device != null) {
            if (mUsbManager.hasPermission(device)) {
                processConnect(device);
            } else {
                mUsbManager.requestPermission(device, mPermissionIntent);
            }
        }
    }
    
    public HashMap<String, UsbDevice> getDeviceList() {
        return mUsbManager.getDeviceList();
    }
    
    public void dumpDevices() {
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.d(TAG, "Found " + deviceList.size() + " USB devices:");
        
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.d(TAG, "Device: " + device.getDeviceName() + 
                  " VID: " + String.format("0x%04X", device.getVendorId()) +
                  " PID: " + String.format("0x%04X", device.getProductId()) +
                  " Class: " + device.getDeviceClass());
        }
    }
    
    private void processConnect(UsbDevice device) {
        if (mOnDeviceConnectListener != null) {
            mOnDeviceConnectListener.onConnect(device, true);
        }
    }
    
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "BroadcastReceiver received: " + action);
            
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            processConnect(device);
                        }
                    } else {
                        Log.d(TAG, "Permission denied for device " + device);
                        if (mOnDeviceConnectListener != null) {
                            mOnDeviceConnectListener.onCancel(device);
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (mOnDeviceConnectListener != null) {
                    mOnDeviceConnectListener.onAttach(device);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (mOnDeviceConnectListener != null) {
                    mOnDeviceConnectListener.onDetach(device);
                }
            }
        }
    };
} 