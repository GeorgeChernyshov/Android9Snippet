package com.example.post28

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.rtt.WifiRttManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.post28.databinding.ActivityIndoorPositioningBinding

class IndoorPositioningActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndoorPositioningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIndoorPositioningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextButton.setOnClickListener {
            startActivity(Intent(this, NewFeaturesActivity::class.java))
        }

        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.P ||
            !packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)
        ) {
            binding.indoorPositioningTextView.text = getText(R.string.indoor_positioning_rtt_not_supported)
        } else {
            val wifiRttManager = getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager
            val filter = IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED)

            val myReceiver = object: BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    if (wifiRttManager.isAvailable) {
                        // it is not on any of my devices
                    } else {
                        binding.indoorPositioningTextView.text =
                            getText(R.string.indoor_positioning_rtt_disabled)
                    }
                }
            }

            registerReceiver(myReceiver, filter)
        }
    }
}