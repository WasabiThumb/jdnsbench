#include <stdlib.h>
#include <string.h>
#include "../headers/api.h"
#include "../headers/logging.h"
#include "../headers/main.h"

// Simply wraps api.h into JNI

struct jni_params {
    unsigned char init;
    jclass c_callback;
    jmethodID m_callback_open;
    jmethodID m_callback_add_v4;
    jmethodID m_callback_add_v6;
    jmethodID m_callback_close;
};
static struct jni_params JNI_PARAMS = {
        0,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL
};

JNIEXPORT jstring JNICALL Java_io_github_wasabithumb_jdnsbench_jni_JNI_strerror(JNIEnv *env, jobject obj, jlong code) {
    const char *cstr = logging_strerror((uint64_t) code);
    if (cstr == NULL) return NULL;
    return (*env)->NewStringUTF(env, cstr);
}

JNIEXPORT jlong JNICALL Java_io_github_wasabithumb_jdnsbench_jni_JNI_init(JNIEnv *env, jobject obj) {
    struct jni_params params;

    params.c_callback = (*env)->FindClass(env, "io/github/wasabithumb/jdnsbench/jni/JNIQueryCallback");
    if (params.c_callback == NULL) return (jlong) E_LINKAGE;

    params.m_callback_open = (*env)->GetMethodID(env, params.c_callback, "open", "(JI)V");
    if (params.m_callback_open == NULL) return (jlong) E_LINKAGE;

    params.m_callback_add_v4 = (*env)->GetMethodID(env, params.c_callback, "addV4", "(Ljava/lang/String;)V");
    if (params.m_callback_add_v4 == NULL) return (jlong) E_LINKAGE;

    params.m_callback_add_v6 = (*env)->GetMethodID(env, params.c_callback, "addV6", "(Ljava/lang/String;)V");
    if (params.m_callback_add_v6 == NULL) return (jlong) E_LINKAGE;

    params.m_callback_close = (*env)->GetMethodID(env, params.c_callback, "close", "()V");
    if (params.m_callback_close == NULL) return (jlong) E_LINKAGE;

    params.init = 1;
    memcpy(&JNI_PARAMS, &params, sizeof(struct jni_params));

    return (jlong) api_init();
}

JNIEXPORT jlong JNICALL Java_io_github_wasabithumb_jdnsbench_jni_JNI_destroy(JNIEnv *env, jobject obj) {
    if (!JNI_PARAMS.init) return (jlong) E_STATE;
    return (jlong) api_destroy();
}

static void query_callback_open(dnsbench_query_callback_t self, uint64_t status, int32_t timeouts) {
    JavaVM *vm = (JavaVM*) (self->reserved1);
    JNIEnv *env = NULL;
    if ((*vm)->AttachCurrentThread(vm, (void**) &env, NULL) < 0) {
        fprintf(stderr, "Failed to attach thread to JVM\n");
        exit(1);
    }
    self->reserved3 = (void*) env;

    jobject handle = (jobject) (self->reserved2);
    jmethodID method = JNI_PARAMS.m_callback_open;

    jvalue args[2];
    args[0] = (jvalue) ((jlong) status);
    args[1] = (jvalue) ((jint) timeouts);
    (*env)->CallVoidMethodA(env, handle, method, args);
}

static void query_callback_addV4(dnsbench_query_callback_t self, const char *v4, size_t v4len) {
    JNIEnv *env = (JNIEnv*) (self->reserved3);
    jobject handle = (jobject) (self->reserved2);
    jmethodID method = JNI_PARAMS.m_callback_add_v4;

    jstring str = (*env)->NewStringUTF(env, v4);
    (*env)->CallVoidMethodA(env, handle, method, (jvalue*) &str);
    (*env)->DeleteLocalRef(env, str);
}

static void query_callback_addV6(dnsbench_query_callback_t self, const char *v6, size_t v6len) {
    JNIEnv *env = (JNIEnv*) (self->reserved3);
    jobject handle = (jobject) (self->reserved2);
    jmethodID method = JNI_PARAMS.m_callback_add_v6;

    jstring str = (*env)->NewStringUTF(env, v6);
    (*env)->CallVoidMethodA(env, handle, method, (jvalue*) &str);
    (*env)->DeleteLocalRef(env, str);
}

static void query_callback_close(dnsbench_query_callback_t self) {
    JavaVM *vm = (JavaVM*) (self->reserved1);
    JNIEnv *env = (JNIEnv*) (self->reserved3);
    jobject handle = (jobject) (self->reserved2);
    free((void*) self);
    jmethodID method = JNI_PARAMS.m_callback_close;
    (*env)->CallVoidMethod(env, handle, method);
    (*env)->DeleteGlobalRef(env, handle);
    (*vm)->DetachCurrentThread(vm);
}

static const struct dnsbench_query_callback QUERY_CALLBACK_TEMPLATE = {
        NULL,
        NULL,
        NULL,
        query_callback_open,
        query_callback_addV4,
        query_callback_addV6,
        query_callback_close
};

JNIEXPORT jlong JNICALL Java_io_github_wasabithumb_jdnsbench_jni_JNI_query(JNIEnv *env, jobject obj, jstring server, jstring domain, jobject callback) {
    if (!JNI_PARAMS.init) return (jlong) E_STATE;

    JavaVM *vm;
    if ((*env)->GetJavaVM(env, &vm) != 0) {
        return (jlong) E_LINKAGE;
    }

    jobject global_callback = (*env)->NewGlobalRef(env, callback);
    if (global_callback == NULL) return (jlong) E_NOMEM;

    dnsbench_query_callback_t c_callback = (dnsbench_query_callback_t) malloc(sizeof(struct dnsbench_query_callback));
    if (c_callback == NULL) return (jlong) E_NOMEM;
    memcpy(c_callback, &QUERY_CALLBACK_TEMPLATE, sizeof(struct dnsbench_query_callback));
    c_callback->reserved1 = (void*) vm;
    c_callback->reserved2 = (void*) global_callback;

    const char *chars = (*env)->GetStringUTFChars(env, server, NULL);
    if (chars == NULL) return (jlong) E_NOMEM;

    const char *domain_chars = (*env)->GetStringUTFChars(env, domain, NULL);
    if (domain_chars == NULL) {
        (*env)->ReleaseStringUTFChars(env, server, chars);
        return (jlong) E_NOMEM;
    }

    uint64_t status = api_query(chars, domain_chars, c_callback);
    (*env)->ReleaseStringUTFChars(env, server, chars);
    (*env)->ReleaseStringUTFChars(env, domain, domain_chars);
    return (jlong) status;
}
