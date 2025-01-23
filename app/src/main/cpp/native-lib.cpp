#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_pl_kamil_native_1lib_NativeLib_encryptWithXOR(JNIEnv *env, jobject thiz, jstring input,
                                                   jint key) {
    const char *inputChars = env->GetStringUTFChars(input, nullptr);
    std::string result;

    // Szyfrowanie XOR
    for (size_t i = 0; inputChars[i] != '\0'; i++) {
        result += inputChars[i] ^ key; // XOR z kluczem
    }

    // Zwalnianie pamięci
    env->ReleaseStringUTFChars(input, inputChars);

    // Zwracanie zaszyfrowanego ciągu jako jstring
    return env->NewStringUTF(result.c_str());
}