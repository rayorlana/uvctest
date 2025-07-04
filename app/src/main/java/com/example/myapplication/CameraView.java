package com.example.myapplication;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

public class CameraView extends TextureView implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CameraView";
    
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private OnCameraViewListener mListener;
    
    public interface OnCameraViewListener {
        void onSurfaceCreated(Surface surface);
        void onSurfaceChanged(Surface surface, int width, int height);
        void onSurfaceDestroyed(Surface surface);
    }
    
    public CameraView(Context context) {
        super(context);
        init();
    }
    
    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        Log.d(TAG, "CameraView created");
        // Don't set surface texture listener here - do it later when needed
    }
    
    public void setupSurfaceListener() {
        try {
            Log.d(TAG, "Setting up surface texture listener");
            setSurfaceTextureListener(this);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up surface texture listener", e);
        }
    }
    
    public void setOnCameraViewListener(OnCameraViewListener listener) {
        mListener = listener;
    }
    
    public Surface getSurface() {
        return mSurface;
    }
    
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: " + width + "x" + height);
        mSurfaceTexture = surface;
        mSurface = new Surface(surface);
        
        if (mListener != null) {
            mListener.onSurfaceCreated(mSurface);
        }
    }
    
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged: " + width + "x" + height);
        
        if (mListener != null) {
            mListener.onSurfaceChanged(mSurface, width, height);
        }
    }
    
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed");
        
        if (mListener != null) {
            mListener.onSurfaceDestroyed(mSurface);
        }
        
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        
        return true;
    }
    
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Called when the SurfaceTexture's surface has been updated
        // We don't need to do anything here for basic preview
    }
} 