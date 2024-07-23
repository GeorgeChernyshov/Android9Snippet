package com.example.post28.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.post28.DIUtils
import com.example.post28.notifications.NotificationHelper
import com.example.post28.R
import com.example.post28.databinding.ActivityNewFeaturesBinding

class NewFeaturesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewFeaturesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewFeaturesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val imageSource = ImageDecoder.createSource(
                    contentResolver,
                    Uri.parse("android.resource://com.example.post28/" + R.drawable.big_floppa)
                )

                binding.imageView.setImageDrawable(getCroppedImage(imageSource))

                val gifSource = ImageDecoder.createSource(
                    contentResolver,
                    Uri.parse("android.resource://com.example.post28/" + R.drawable.big_floppa_gif)
                )

                val drawable = getCroppedImage(gifSource)
                binding.gifImageView.setImageDrawable(drawable)
                (drawable as AnimatedImageDrawable).start()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.replyTextView.text = DIUtils.replyRepository.replyText
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getCroppedImage(imageSource: ImageDecoder.Source): Drawable {
        return ImageDecoder.decodeDrawable(imageSource) { decoder, info, source ->
            decoder.setPostProcessor {
                val path = Path()
                path.fillType = Path.FillType.INVERSE_EVEN_ODD
                path.addRoundRect(
                    0f,
                    0f,
                    it.width.toFloat(),
                    it.height.toFloat(),
                    40f,
                    40f,
                    Path.Direction.CW
                )

                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = Color.TRANSPARENT
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                it.drawPath(path, paint)

                return@setPostProcessor PixelFormat.UNKNOWN
            }
        }
    }
}