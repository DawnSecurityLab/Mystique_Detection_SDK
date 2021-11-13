APP_ABI := all
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_LDLIBS :=-llog
LOCAL_MODULE := native-lib
LOCAL_SRC_FILES := com_jingdong_dawnslab_1vulscanner_SecondFragment.cpp
include $(BUILD_SHARED_LIBRARY)
