#include <string>
#include <string.h>
#include <android/log.h>
#include <jni.h>
#include <iostream>
#include <dirent.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <map>
#include <iostream>
#include <cassert>

using namespace std;

#define TAG "MY_TAG"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,    TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,     TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,     TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,    TAG, __VA_ARGS__)



extern "C" {

using namespace std;

std::map<std::string, std::string> _m;

static int get_file_size_time(const char *filename) {
    struct stat statbuf;

    if (stat(filename, &statbuf) == -1) {

        return (-1);
    }

    if(strstr(filename, ".dm")){
        return (-1);
    }

    if(strstr(filename, ".apk")){
        return (-1);
    }

    if (S_ISDIR(statbuf.st_mode))return (1);
    if (S_ISREG(statbuf.st_mode)){
        char buf[1024];
        memset(buf,0,1024);
        
        sprintf(buf, "%s", ctime(&statbuf.st_mtime));

        if(strstr(buf, "2020") || strstr(buf, "2021")){
                _m[buf] = filename;
        }

    }

    return (0);
}


void showAllFiles(string dir_name) {
    if (dir_name.empty()) {
        
        return;
    }
    DIR *dir = opendir(dir_name.c_str());
    if (NULL == dir) {
        
        return;
    }
    struct dirent *file;
    while ((file = readdir(dir)) != NULL) {
        if (strcmp(file->d_name, ".") == 0 || strcmp(file->d_name, "..") == 0) {
            continue;
        }
        if (file->d_type == DT_DIR) {
            string filePath = dir_name + "/" + file->d_name;
            showAllFiles(filePath);
        } else {
            
            get_file_size_time((dir_name + "/" + file->d_name).c_str());
        }
    }
    closedir(dir);
}
}


extern "C" JNIEXPORT jobject JNICALL
Java_com_jingdong_dawnslab_1vulscanner_SecondFragment_stringFromJNI(JNIEnv* env,jobject obj ,jstring dirPath_)
{
    _m.clear();

    const char *dirPath = env->GetStringUTFChars(dirPath_, 0);
    string filename(dirPath);
    string directory;
    const size_t last_slash_idx = filename.rfind('/');
    if (std::string::npos != last_slash_idx)
    {
        directory = filename.substr(0, last_slash_idx);
    }
    

    showAllFiles(directory);
    env->ReleaseStringUTFChars(dirPath_, dirPath);

    jclass java_cls_HashMap = env->FindClass("java/util/HashMap");
    jmethodID java_mid_HashMap = env->GetMethodID(java_cls_HashMap, "<init>", "()V");
    jobject  java_obj_HashMap = env->NewObject(java_cls_HashMap, java_mid_HashMap, "");
    jmethodID java_mid_HashMap_put = env->GetMethodID(java_cls_HashMap, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    env->CallObjectMethod(java_obj_HashMap, java_mid_HashMap_put, env->NewStringUTF("Thu Jul 01 00:10:45 1970"), env->NewStringUTF("default"));

    if(_m.size()>=1){

        map<string, string>::iterator iter;
        for(iter = _m.begin(); iter != _m.end(); iter++) {
            

            env->CallObjectMethod(java_obj_HashMap, java_mid_HashMap_put, env->NewStringUTF(iter->first.c_str()), env->NewStringUTF(iter->second.c_str()));

        }
    }


    return java_obj_HashMap;

}
