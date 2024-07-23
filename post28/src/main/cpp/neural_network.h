#include <android/NeuralNetworks.h>
#include <android/asset_manager_jni.h>
#include <memory>
#include <vector>

#define LOG_TAG "NNAPI_SEQUENCE"

class NeuralNetwork {
public:
    explicit NeuralNetwork();
    ~NeuralNetwork();

    static std::unique_ptr<NeuralNetwork> Create();
    bool Compute(std::vector<float> inputValue, std::vector<float> &result);
private:
    bool CreateSharedMemories();
    bool CreateModel();
    bool CreateCompilation();

    ANeuralNetworksModel* model_ = nullptr;
    ANeuralNetworksCompilation* compilation_ = nullptr;

    static constexpr uint32_t inputLength_ = 784;
    static constexpr uint32_t outputLength_ = 10;
    static constexpr uint32_t tensorSize_ = inputLength_ * outputLength_;

    int initialStateFd_ = -1;
    int denseLayerFd_ = -1;
    int outputLayerFd_ = -1;

    ANeuralNetworksMemory* memoryInitialState_ = nullptr;
    ANeuralNetworksMemory* memoryDenseLayer_ = nullptr;
    ANeuralNetworksMemory* memoryOutputLayer_ = nullptr;
};