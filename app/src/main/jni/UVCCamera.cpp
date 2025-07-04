#include <jni.h>
#include <android/log.h>

#define LOG_TAG "UVCCamera"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_example_myapplication_UVCCameraHelper_nativeGetVersion(JNIEnv *env, jobject /* this */) {
    return env->NewStringUTF("UVCCamera Native Library v1.0");
}

JNIEXPORT void JNICALL
Java_com_example_myapplication_UVCCameraHelper_nativeInit(JNIEnv *env, jobject /* this */) {
    LOGI("UVCCamera native library initialized");
}

} 