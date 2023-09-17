package com.example.post28

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.post28.databinding.ActivityNewFeaturesBinding

class NewFeaturesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewFeaturesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewFeaturesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        with (binding) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                cutoutTypeTextView.text = getString(R.string.display_cutout_not_supported)
            } else {
                val cutout = window.decorView.rootWindowInsets.displayCutout
                if (cutout?.boundingRects.isNullOrEmpty()) {
                    cutoutTypeTextView.text = getString(R.string.display_cutout_none)
                } else {
                    cutoutTypeTextView.text = getString(R.string.display_cutout_top)
                }
            }

            sendChatNotificationButton.setOnClickListener {
                NotificationHelper(this@NewFeaturesActivity)
                    .showChatNotification()
            }

            sendSystemNotificationButton.setOnClickListener {
                NotificationHelper(this@NewFeaturesActivity)
                    .showSystemNotification()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.replyTextView.text = DIUtils.replyRepository.replyText
    }
}