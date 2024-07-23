package com.example.post28.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.post28.R
import com.example.post28.databinding.ActivityNeuralNetworkBinding
import com.example.post28.neural.NeuralNetwork

class NeuralNetworkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNeuralNetworkBinding

    external fun testFunction(): Long

    val neuralNetwork = NeuralNetwork()

    init {
        System.loadLibrary("basic")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNeuralNetworkBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        displayStatus(NeuralNetwork.NetworkStatus.UNKNOWN)

        binding.trainNetworkButton.setOnClickListener {
            neuralNetwork.init()
        }

        binding.checkStatusButton.setOnClickListener {
            displayStatus(
                if (neuralNetwork.modelCreated)
                    NeuralNetwork.NetworkStatus.READY
                else
                    NeuralNetwork.NetworkStatus.DESTROYED
            )
        }
    }

    override fun onDestroy() {
        neuralNetwork.destroy()
        super.onDestroy()
    }

    private fun displayStatus(status: NeuralNetwork.NetworkStatus) {
        val statusString = getString(
            when(status) {
                NeuralNetwork.NetworkStatus.UNKNOWN -> R.string.neural_status_unknown
                NeuralNetwork.NetworkStatus.READY -> R.string.neural_status_ready
                NeuralNetwork.NetworkStatus.DESTROYED -> R.string.neural_status_destroyed
            }
        )

        binding.networkStatusTextView.text = getString(
            R.string.neural_train_status,
            statusString
        )
    }
}