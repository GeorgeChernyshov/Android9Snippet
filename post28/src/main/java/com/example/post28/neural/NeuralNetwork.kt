package com.example.post28.neural

class NeuralNetwork {
    private external fun initModel(): Long
    private external fun compute(input: Array<Float>, nnModel: Long): Array<Float>
    private external fun destroyModel(nnModel: Long)

    private var handle: Long = 0

    val modelCreated
        get() = (handle != 0L)

    fun init() {
        if (handle == 0L)
            handle = initModel()
    }

    fun compute(input: Array<Float>): Array<Float>? {
        return if (handle != 0L)
            compute(input, handle)
        else null
    }

    fun destroy() {
        if (handle != 0L) {
            destroyModel(handle)
            handle = 0L
        }
    }

    enum class NetworkStatus {
        UNKNOWN, READY, DESTROYED;
    }
}