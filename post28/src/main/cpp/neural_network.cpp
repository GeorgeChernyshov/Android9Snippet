#include <algorithm>
#include <utility>
#include <android/log.h>
#include <android/sharedmem.h>
#include <unistd.h>
#include <sys/mman.h>

#include "neural_network.h"

NeuralNetwork::NeuralNetwork() {}

/**
 * A helper method to allocate an ASharedMemory region and create an
 * ANeuralNetworksMemory object.
 */
static std::pair<int, ANeuralNetworksMemory*> CreateASharedMemory(
        const char* name,
        uint32_t size,
        int prot) {
    int fd = ASharedMemory_create(name, size * sizeof(float));

    // Create an ANeuralNetworksMemory object from the corresponding ASharedMemory objects.
    ANeuralNetworksMemory* memory = nullptr;
    int32_t status = ANeuralNetworksMemory_createFromFd(size * sizeof(float), prot, fd, 0, &memory);
    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "ANeuralNetworksMemory_createFromFd failed for %s", name);
        close(fd);
        return {-1, nullptr};
    }

    return {fd, memory};
}

static void fillMemory(int fd, uint32_t size, std::vector<float> value) {
    // Set the values of the memory.
    // In reality, the values in the shared memory region will be manipulated by
    // other modules or processes.
    float* data = reinterpret_cast<float*>(
            mmap(nullptr, size * sizeof(float), PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0));
    std::copy(std::begin(value), std::end(value), data);
    munmap(data, size * sizeof(float));
}

std::unique_ptr<NeuralNetwork> NeuralNetwork::Create() {
    auto model = std::make_unique<NeuralNetwork>();
    if (!model->CreateSharedMemories())
        return nullptr;
    if (!model->CreateModel())
        return nullptr;
    if (!model->CreateCompilation())
        return nullptr;

    return model;
}

bool NeuralNetwork::CreateSharedMemories() {
    std::tie(initialStateFd_, memoryInitialState_) =
            CreateASharedMemory("initialState", inputLength_, PROT_WRITE);
    std::tie(denseLayerFd_, memoryDenseLayer_) =
            CreateASharedMemory("denseLayer", tensorSize_, PROT_READ);
    std::tie(outputLayerFd_, memoryOutputLayer_) =
            CreateASharedMemory("outputLayer", inputLength_, PROT_READ | PROT_WRITE);

    return true;
}

bool NeuralNetwork::CreateModel() {
    int32_t status;

    // Create the ANeuralNetworksModel handle.
    status = ANeuralNetworksModel_create(&model_);
    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "ANeuralNetworksModel_create failed");
        return false;
    }

    uint32_t dimensions[] = {inputLength_};
    ANeuralNetworksOperandType float32TensorType{
            .type = ANEURALNETWORKS_TENSOR_FLOAT32,
            .dimensionCount = sizeof(dimensions) / sizeof(dimensions[0]),
            .dimensions = dimensions,
            .scale = 0.0f,
            .zeroPoint = 0,
    };

    uint32_t opIdx = 0;

    status = ANeuralNetworksModel_addOperand(model_, &float32TensorType);
    uint32_t inputLayerIdx = opIdx++;
    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "ANeuralNetworksModel_addOperand failed for operand (%d)",
                            inputLayerIdx);
        return false;
    }

    status = ANeuralNetworksModel_addOperand(model_, &float32TensorType);
    uint32_t outputLayerIdx = opIdx++;
    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "ANeuralNetworksModel_addOperand failed for operand (%d)",
                            outputLayerIdx);
        return false;
    }

    std::vector<uint32_t> tmpOperands = { inputLayerIdx };
    status = ANeuralNetworksModel_addOperation(
            model_,
            ANEURALNETWORKS_FLOOR,
            tmpOperands.size(),
            tmpOperands.data(),
            1,
            &outputLayerIdx);

    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "ANeuralNetworksModel_addOperation failed for ADD");
        return false;
    }

    std::vector<uint32_t> modelInputs = { inputLayerIdx };
    std::vector<uint32_t> modelOutputs = { outputLayerIdx };

    status = ANeuralNetworksModel_identifyInputsAndOutputs(
            model_,
            modelInputs.size(),
            modelInputs.data(),
            modelOutputs.size(),
            modelOutputs.data());

    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "ANeuralNetworksModel_identifyInputsAndOutputs failed");
        return false;
    }

    status = ANeuralNetworksModel_finish(model_);
    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "ANeuralNetworksModel_finish failed");
        return false;
    }

    return true;
}

bool NeuralNetwork::CreateCompilation() {
    int32_t status;

    // Create the ANeuralNetworksCompilation object for the constructed model.
    status = ANeuralNetworksCompilation_create(model_, &compilation_);
    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "ANeuralNetworksCompilation_create failed");
        return false;
    }

    status = ANeuralNetworksCompilation_setPreference(compilation_,
                                                      ANEURALNETWORKS_PREFER_FAST_SINGLE_ANSWER);
    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "ANeuralNetworksCompilation_setPreference failed");
        return false;
    }

    // Finish the compilation.
    status = ANeuralNetworksCompilation_finish(compilation_);
    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "ANeuralNetworksCompilation_finish failed");
        return false;
    }

    return true;
}

bool NeuralNetwork::Compute(std::vector<float> inputValue, std::vector<float> &result) {
    fillMemory(initialStateFd_, inputLength_, std::move(inputValue));

    ANeuralNetworksExecution* execution;
    int32_t status = ANeuralNetworksExecution_create(compilation_, &execution);
    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "ANeuralNetworksExecution_create failed");
        return false;
    }

    status = ANeuralNetworksExecution_setInputFromMemory(
            execution,
            0,
            nullptr,
            memoryInitialState_,
            0,
            inputLength_ * sizeof(float)
            );

    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "ANeuralNetworksExecution_setInputFromMemory failed for sumIn");
        return false;
    }

    status = ANeuralNetworksExecution_setOutputFromMemory(
            execution,
            0,
            nullptr,
            memoryOutputLayer_,
            0,
            inputLength_ * sizeof(float)
            );

    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "ANeuralNetworksExecution_setOutputFromMemory failed for sumOut");
        return false;
    }
    std::vector<ANeuralNetworksEvent*> events(1, nullptr);
    status = ANeuralNetworksExecution_startCompute(execution, &events[0]);

    if (status != ANEURALNETWORKS_NO_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "ANeuralNetworksExecution_compute failed");
        return false;
    }

    ANeuralNetworksExecution_free(execution);
    ANeuralNetworksEvent_wait(events.back());

    float* outputTensorPtr = reinterpret_cast<float*>(
            mmap(nullptr,
                 inputLength_ * sizeof(float),
                 PROT_READ,
                 MAP_SHARED,
                 outputLayerFd_,
                 0));

    for(int i = 0; i < inputLength_; i++) {
        auto f = *(outputTensorPtr + i);
        result.push_back(f);
    }

    munmap(outputTensorPtr, inputLength_ * sizeof(float));

    for (auto* event : events) {
        ANeuralNetworksEvent_free(event);
    }

    return true;
}

NeuralNetwork::~NeuralNetwork() {
    ANeuralNetworksCompilation_free(compilation_);
    ANeuralNetworksModel_free(model_);

    ANeuralNetworksMemory_free(memoryInitialState_);
    ANeuralNetworksMemory_free(memoryDenseLayer_);
    ANeuralNetworksMemory_free(memoryOutputLayer_);
    close(initialStateFd_);
    close(denseLayerFd_);
    close(outputLayerFd_);
}