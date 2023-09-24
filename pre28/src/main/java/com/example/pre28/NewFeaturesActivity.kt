package com.example.pre28

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.example.pre28.databinding.ActivityNewFeaturesBinding


class NewFeaturesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewFeaturesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewFeaturesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        binding.imageView.setImageBitmap(
            getCroppedImage(
                Uri.parse("android.resource://com.example.pre28/" + R.drawable.big_floppa)
            )
        )

        val stream = resources.openRawResource(+ R.drawable.big_floppa_gif)
        binding.gifImageView.movie = Movie.decodeStream(stream)
    }

    private fun getCroppedImage(imageUri: Uri): Bitmap {
        val sourceBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val result = Bitmap.createBitmap(
            sourceBitmap.width,
            sourceBitmap.height,
            Bitmap.Config.ARGB_8888
        )

        Canvas(result).also {
            val rect = Rect(0, 0, it.width, it.height)

            val paint = Paint()
            paint.isAntiAlias = true
            paint.color = Color.BLACK
            it.drawRoundRect(RectF(rect), 40f, 40f, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

            it.drawBitmap(sourceBitmap, rect, rect, paint)
        }

        return result
    }
}