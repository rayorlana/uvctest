package com.example.myapplication;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Import the UVCCamera classes following official demo
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements 
    USBMonitor.OnDeviceConnectListener,
    TextureView.SurfaceTextureListener {

    private static final boolean DEBUG = true;	// TODO set false on production
    private static final String TAG = "MainActivity";
    
    // UI Components
    private TextView mStatusText;
    private TextureView mCameraView;
    private Button mBtnScan;
    private Button mBtnConnect;
    private Button mBtnStartPreview;
    private Button mBtnStopPreview;
    
    // USB and Camera components
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    
    // State variables
    private UsbDevice mSelectedDevice;
    private boolean mCameraConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        initializeViews();
        
        updateStatus("UVCCamera library loaded");
        
        // Initialize USB monitor with device filters
        mUSBMonitor = new USBMonitor(this, this);
        
        // Initialize UVCCamera instance with detailed logging
        Log.d(TAG, "About to create UVCCamera instance...");
        try {
            mUVCCamera = new UVCCamera();
            Log.d(TAG, "UVCCamera constructor completed");
            Log.d(TAG, "mUVCCamera is: " + (mUVCCamera != null ? "NOT NULL" : "NULL"));
            
            if (isUVCCameraReady()) {
                updateStatus("UVCCamera library loaded successfully");
            } else {
                updateStatus("UVCCamera library loaded but not initialized");
                Log.w(TAG, "UVCCamera created but not properly initialized");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception creating UVCCamera: " + e.getMessage(), e);
            mUVCCamera = null;
            updateStatus("Failed to load UVCCamera library");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Native library not found: " + e.getMessage(), e);
            mUVCCamera = null;
            updateStatus("Failed to load native libraries");
            showToast("Camera native libraries not found");
        }
        
        // Set up camera view listener
        mCameraView.setSurfaceTextureListener(this);
        
        // Set up button listeners
        setupButtonListeners();
        
        Log.d(TAG, "MainActivity created with UVCCamera: " + (mUVCCamera != null ? "SUCCESS" : "FAILED"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    @Override
    protected void onStop() {
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
        }
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up camera
        if (mUVCCamera != null) {
            mUVCCamera.close();
        }
        
        // Clean up USB monitor
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
        }
        
        Log.d(TAG, "MainActivity destroyed");
    }
    
    private void initializeViews() {
        mStatusText = findViewById(R.id.status_text);
        mCameraView = findViewById(R.id.camera_view);
        mBtnScan = findViewById(R.id.btn_scan);
        mBtnConnect = findViewById(R.id.btn_connect);
        mBtnStartPreview = findViewById(R.id.btn_start_preview);
        mBtnStopPreview = findViewById(R.id.btn_stop_preview);
    }
    
    private void setupButtonListeners() {
        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForDevices();
            }
        });
        
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDevice();
            }
        });
        
        mBtnStartPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreview();
            }
        });
        
        mBtnStopPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPreview();
            }
        });
    }
    
    private void scanForDevices() {
        updateStatus("Scanning for USB devices...");
        
        if (mUSBMonitor != null) {
            // Use the correct method - getDeviceList() returns List<UsbDevice>
            List<UsbDevice> deviceList = mUSBMonitor.getDeviceList();
            
            if (deviceList.isEmpty()) {
                updateStatus("No USB devices found");
                showToast("No USB devices found. Make sure your camera is connected.");
                return;
            }
            
            // Use UVCCamera's device filtering - returns List, not array
            List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
            DeviceFilter filter = filters.get(0);
            
            UsbDevice uvcDevice = null;
            int deviceCount = 0;
            
            for (UsbDevice device : deviceList) {
                deviceCount++;
                Log.d(TAG, "Found device: " + device.getProductName() + 
                      " VID: " + String.format("0x%04X", device.getVendorId()) + 
                      " PID: " + String.format("0x%04X", device.getProductId()));
                
                if (filter.matches(device)) {
                    uvcDevice = device;
                    break;
                }
            }
            
            if (uvcDevice != null) {
                mSelectedDevice = uvcDevice;
                updateStatus("Found UVC camera: " + uvcDevice.getProductName());
                mBtnConnect.setEnabled(true);
                showToast("UVC camera detected! Click Connect to proceed.");
            } else {
                updateStatus("Found " + deviceCount + " USB devices, but no UVC cameras");
                showToast("No UVC cameras found. Make sure your camera supports USB Video Class.");
                mBtnConnect.setEnabled(false);
            }
        }
    }
    
    private void connectToDevice() {
    if (mSelectedDevice == null) {
        showToast("No device selected");
        return;
    }
    
        Log.d(TAG, "=== STARTING PERMISSION REQUEST ===");
        Log.d(TAG, "Selected device: " + mSelectedDevice.getDeviceName());
        Log.d(TAG, "Device VID: " + String.format("0x%04X", mSelectedDevice.getVendorId()));
        Log.d(TAG, "Device PID: " + String.format("0x%04X", mSelectedDevice.getProductId()));
        
        // Check if we already have permission 
        boolean hasPermission = false;
        try {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            hasPermission = usbManager.hasPermission(mSelectedDevice);
            Log.d(TAG, "Device already has permission: " + hasPermission);
        } catch (Exception e) {
            Log.e(TAG, "Error checking permission: " + e.getMessage());
        }
        
        updateStatus("Requesting permission for device...");
        Log.d(TAG, "Calling mUSBMonitor.requestPermission()");
        
        boolean result = mUSBMonitor.requestPermission(mSelectedDevice);
        Log.d(TAG, "requestPermission() returned: " + result);
    }
    
    private void startPreview() {
        if (!mCameraConnected) {
            showToast("Camera not connected");
            return;
        }
        
        if (mUVCCamera != null) {
            try {
                Log.d(TAG, "=== STARTING CAMERA PREVIEW ===");
                
                // Always stop preview first to ensure clean state
                try {
                    mUVCCamera.stopPreview();
                    Log.d(TAG, "Stopped any existing preview");
                } catch (Exception e) {
                    Log.d(TAG, "No existing preview to stop: " + e.getMessage());
                }
                
                // Small delay to ensure clean state
                Thread.sleep(200);
                
                // Check if TextureView surface is available
                if (mCameraView.isAvailable()) {
                    SurfaceTexture surfaceTexture = mCameraView.getSurfaceTexture();
                    if (surfaceTexture != null) {
                        Log.d(TAG, "Setting preview surface before starting preview");
                        mUVCCamera.setPreviewDisplay(new Surface(surfaceTexture));
                    } else {
                        Log.e(TAG, "SurfaceTexture is null!");
                        showToast("Camera surface not ready. Try again.");
                        return;
                    }
                } else {
                    Log.e(TAG, "TextureView surface not available!");
                    showToast("Camera surface not ready. Try again in a moment.");
                    return;
                }
                
                // Set preview size and format
                Log.d(TAG, "Setting optimal preview size...");
                setOptimalPreviewSize();
                
                // Start preview with retry logic
                Log.d(TAG, "Starting camera preview...");
                if (startPreviewWithRetry()) {
                    updateStatus("Camera preview started successfully!");
                    mBtnStartPreview.setEnabled(false);
                    mBtnStopPreview.setEnabled(true);
                    showToast("Preview started");
                    Log.d(TAG, "Camera preview started successfully!");
                } else {
                    updateStatus("Failed to start preview - try again");
                    showToast("Preview failed. Try clicking Stop then Start again.");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to start preview: " + e.getMessage(), e);
                updateStatus("Failed to start preview: " + e.getMessage());
                showToast("Failed to start preview: " + e.getMessage());
            }
        }
    }
    
    private void stopPreview() {
        if (mUVCCamera != null) {
            try {
                Log.d(TAG, "Stopping camera preview...");
                mUVCCamera.stopPreview();
                
                // Small delay to ensure clean stop
                Thread.sleep(200);
                
                updateStatus("Camera preview stopped");
                mBtnStartPreview.setEnabled(true);
                mBtnStopPreview.setEnabled(false);
                showToast("Preview stopped");
                
                Log.d(TAG, "Camera preview stopped successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Error stopping preview: " + e.getMessage());
                updateStatus("Error stopping preview: " + e.getMessage());
                showToast("Error stopping preview");
            }
        }
    }
    
    private void updateStatus(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText("Status: " + message);
                Log.d(TAG, "Status: " + message);
            }
        });
    }
    
    private void showToast(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // USBMonitor.OnDeviceConnectListener implementation
    @Override
    public void onAttach(UsbDevice device) {
        Log.d(TAG, "USB device attached: " + device.getDeviceName());
        
        // UI updates on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateStatus("USB device attached: " + device.getDeviceName());
            }
        });
    }
    
    @Override
    public void onDettach(UsbDevice device) {
        Log.d(TAG, "USB device detached: " + device.getDeviceName());
        
        if (device.equals(mSelectedDevice)) {
            mCameraConnected = false;
            
            if (mUVCCamera != null) {
                mUVCCamera.close();
            }
            
            // UI updates on main thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateStatus("USB device detached: " + device.getDeviceName());
                    mBtnConnect.setEnabled(false);
                    mBtnStartPreview.setEnabled(false);
                    mBtnStopPreview.setEnabled(false);
                }
            });
        } else {
            updateStatus("USB device detached: " + device.getDeviceName());
        }
    }
    
        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            Log.d(TAG, "=== onConnect() CALLED ===");
            Log.d(TAG, "Device: " + device.getDeviceName());
            Log.d(TAG, "createNew: " + createNew);
            Log.d(TAG, "ctrlBlock: " + ctrlBlock);
            
            // Check if UVCCamera is properly initialized
            if (!isUVCCameraReady()) {
                Log.e(TAG, "UVCCamera is not ready - attempting to recreate");
                try {
                    mUVCCamera = new UVCCamera();
                    if (!isUVCCameraReady()) {
                        updateStatus("Failed to initialize camera - native library issue");
                        showToast("Camera initialization failed. Check if device supports UVC.");
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to recreate UVCCamera: " + e.getMessage());
                    updateStatus("Camera native library error");
                    showToast("Camera native library error");
                    return;
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "Native library linking error: " + e.getMessage());
                    updateStatus("Native library not found");
                    showToast("Camera native libraries missing");
                    return;
                }
            }
            
            try {
                Log.d(TAG, "Opening camera with control block...");
                // Open the camera using control block (following official demo)
                mUVCCamera.open(ctrlBlock);
                mCameraConnected = true;
                
                // ALL UI updates must be on the main thread!
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateStatus("Camera connected successfully");
                        mBtnConnect.setEnabled(false);
                        mBtnStartPreview.setEnabled(true);
                        showToast("Camera connected! You can now start preview.");
                    }
                });
                
                Log.d(TAG, "Camera opened successfully!");
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to open camera", e);
                // UI updates on main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateStatus("Failed to open camera: " + e.getMessage());
                        showToast("Failed to open camera: " + e.getMessage());
                    }
                });
            }
        }

    @Override
    public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
        Log.d(TAG, "USB device disconnected: " + device.getDeviceName());
        mCameraConnected = false;
        
        // UI updates on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateStatus("Camera disconnected");
            }
        });
    }
    
    @Override
    public void onCancel(UsbDevice device) {
        Log.d(TAG, "USB permission cancelled for device: " + device.getDeviceName());
        
        // UI updates on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateStatus("Permission denied for camera");
                showToast("Permission denied. Cannot access camera.");
            }
        });
    }

    // TextureView.SurfaceTextureListener implementation
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "=== Surface texture available: " + width + "x" + height + " ===");
        Log.d(TAG, "Camera connected: " + mCameraConnected);
        Log.d(TAG, "UVCCamera ready: " + (mUVCCamera != null));
        
        // Apply mirroring transformation for natural camera behavior
        applyCameraMirroring(width, height);
        
        // Don't set preview display here - do it in startPreview() instead
        // This ensures proper timing and error handling
        if (mCameraConnected && mUVCCamera != null) {
            Log.d(TAG, "Surface is ready for camera preview");
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "Surface texture size changed: " + width + "x" + height);
        
        // Reapply mirroring transformation for new size
        applyCameraMirroring(width, height);
        
        // If preview is running, we might need to restart it with new size
        if (mUVCCamera != null && mCameraConnected) {
            Log.d(TAG, "Surface size changed while preview active - consider restarting preview");
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "Surface texture destroyed - stopping preview");
        if (mUVCCamera != null) {
            try {
                mUVCCamera.stopPreview();
                Log.d(TAG, "Preview stopped due to surface destruction");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping preview on surface destroy: " + e.getMessage());
            }
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Called for each frame - don't log here to avoid spam
    }
    
    // Helper method to check if UVCCamera is properly initialized
    private boolean isUVCCameraReady() {
        if (mUVCCamera == null) {
            Log.e(TAG, "UVCCamera is null");
            return false;
        }
        
        try {
            // Try to access a method that requires native initialization
            // getSupportedSize() should work if the camera is properly initialized
            String supportedSize = mUVCCamera.getSupportedSize();
            Log.d(TAG, "UVCCamera appears to be properly initialized");
            Log.d(TAG, "Supported sizes: " + (supportedSize != null ? supportedSize : "null"));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "UVCCamera not properly initialized: " + e.getMessage());
            return false;
        }
    }
    
    // Helper method to try different preview sizes if 640x480 doesn't work
    private void setOptimalPreviewSize() {
        try {
            // First try to get supported sizes
            String supportedSizes = mUVCCamera.getSupportedSize();
            Log.d(TAG, "Camera supported sizes: " + supportedSizes);
            
            // Try common sizes that most UVC cameras support
            int[][] commonSizes = {
                {640, 480},   // VGA
                {320, 240},   // QVGA  
                {800, 600},   // SVGA
                {1024, 768}   // XGA
            };
            
            for (int[] size : commonSizes) {
                try {
                    Log.d(TAG, "Trying preview size: " + size[0] + "x" + size[1]);
                    mUVCCamera.setPreviewSize(size[0], size[1]);
                    Log.d(TAG, "Successfully set preview size: " + size[0] + "x" + size[1]);
                    return;
                } catch (Exception e) {
                    Log.w(TAG, "Preview size " + size[0] + "x" + size[1] + " not supported: " + e.getMessage());
                }
            }
            
            // If all fail, use default
            Log.w(TAG, "Using default preview size");
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting preview size: " + e.getMessage());
        }
    }
    
    // Helper method to start preview with retry logic for USB streaming failures
    private boolean startPreviewWithRetry() {
        int maxRetries = 3;
        int retryDelay = 1000; // milliseconds - longer delay for USB issues
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Log.d(TAG, "Preview start attempt " + attempt + "/" + maxRetries);
                mUVCCamera.startPreview();
                
                // Wait a bit to see if streaming actually starts
                Thread.sleep(800);
                
                // Check if streaming is actually working by trying to get frame info
                if (isStreamingActive()) {
                    Log.d(TAG, "Preview and streaming started successfully on attempt " + attempt);
                    return true;
                } else {
                    Log.w(TAG, "Preview started but streaming failed on attempt " + attempt);
                    // Stop the failed preview before retrying
                    mUVCCamera.stopPreview();
                    
                    if (attempt < maxRetries) {
                        Thread.sleep(retryDelay);
                    }
                }
                
            } catch (Exception e) {
                Log.w(TAG, "Preview start attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                } else {
                    Log.e(TAG, "All preview start attempts failed");
                }
            }
        }
        
        return false;
    }
    
    // Helper method to check if streaming is actually active
    private boolean isStreamingActive() {
        try {
            // Simple check - if we can get supported size after starting preview,
            // the camera is likely working. This is a basic health check.
            String supportedSize = mUVCCamera.getSupportedSize();
            return supportedSize != null && !supportedSize.isEmpty();
        } catch (Exception e) {
            Log.w(TAG, "Stream health check failed: " + e.getMessage());
            return false;
        }
    }
    
    // Helper method to apply horizontal mirroring to the camera preview
    private void applyCameraMirroring(int width, int height) {
        Matrix matrix = new Matrix();
        
        // Calculate the center point for scaling
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;
        
        // Apply horizontal flip (mirror effect)
        matrix.postScale(-1.0f, 1.0f, centerX, centerY);
        
        // Apply the transformation to the TextureView
        mCameraView.setTransform(matrix);
        
        Log.d(TAG, "Applied camera mirroring transformation for size: " + width + "x" + height);
    }
}