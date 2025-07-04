LOCAL_PATH := $(call my-dir)

# Include the UVCCamera prebuilt library
include $(CLEAR_VARS)
LOCAL_MODULE := UVCCamera
LOCAL_SRC_FILES := UVCCamera.cpp
LOCAL_LDLIBS := -llog -ljnigraphics -lz -landroid
LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_CPPFLAGS := -std=c++11
include $(BUILD_SHARED_LIBRARY) 