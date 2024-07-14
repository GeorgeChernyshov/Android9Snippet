package com.example.post28

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.post28.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        binding.nextButton.setOnClickListener {
            startActivity(Intent(this, HDRActivity::class.java))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val characteristics = cameraManager.cameraIdList.getOrNull(2)?.let {
                cameraManager.getCameraCharacteristics(it)
            }

            val cameraCapabilities = characteristics
                ?.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)

            binding.multipleCameraTextView.text = when (
                cameraCapabilities?.contains(
                    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA
                )
            ) {
                true -> getString(R.string.camera_multiple_supported)
                false -> getString(R.string.camera_multiple_not_supported)
                else -> null
            }
        }
    }
}