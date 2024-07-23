#include <jni.h>
#include <android/log.h>

#include "neural_network.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_post28_activity_NeuralNetworkActivity_testFunction(JNIEnv *env,jobject obj) {
    __android_log_print(ANDROID_LOG_INFO, "test", "Test function successfully called.");
    return 15;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_post28_neural_NeuralNetwork_initModel(JNIEnv *env, jobject obj) {
    auto model = NeuralNetwork::Create();

    if (model == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Failed to create the model.");
        return 0;
    }

    return (jlong)(uintptr_t)model.release();
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_example_post28_neural_NeuralNetwork_compute(
        JNIEnv *env,
        jobject obj,
        jobjectArray input,
        jlong nnModel
        ) {
    auto* nn_model = (NeuralNetwork*)nnModel;
    auto result = std::vector<float>(0);

    int inputLength = env->GetArrayLength(input);
    std::vector<float> inputVector(0);

    auto floatClass = env->GetObjectClass(env->GetObjectArrayElement(input, 0));
    auto methodId = env->GetMethodID(floatClass, "floatValue", "()F");

    for(int i = 0; i < inputLength; i++) {
        auto fObj = env->GetObjectArrayElement(input, i);
        float f = env->CallFloatMethod(fObj, methodId);
        inputVector.push_back(f);
    }

    nn_model->Compute(inputVector, result);

    auto array = env->NewObjectArray(inputLength, floatClass, nullptr);
    jmethodID floatConstructorID = env->GetMethodID(floatClass, "<init>", "(F)V");
    for(int i = 0; i < inputLength; i++) {
        float f = result[i];
        auto floatObj = env->NewObject(floatClass, floatConstructorID, f);
        env->SetObjectArrayElement(array, i, floatObj);
    }

    return array;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_post28_neural_NeuralNetwork_destroyModel(
        JNIEnv *env,
        jobject obj,
        jlong nnModel
        ) {
    NeuralNetwork* nn_model = (NeuralNetwork*)nnModel;
    delete (nn_model);
}