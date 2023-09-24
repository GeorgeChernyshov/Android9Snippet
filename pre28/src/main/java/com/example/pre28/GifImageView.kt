package com.example.pre28

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View


class GifImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var movie: Movie? = null
        set(value) {
            val newWidth = value?.width() ?: 0
            val newHeight = value?.height() ?: 0

            this.layoutParams.width = newWidth
            this.layoutParams.height = newHeight

            rect = Rect(0, 0, newWidth, newHeight)
            rectF = RectF(rect)

            field = value
        }

    private var mMovieStart = 0
    private var rect = Rect(0, 0, width, height)
    private var rectF = RectF(rect)

    private val paint: Paint = Paint()

    init {
        paint.isAntiAlias = true
        paint.color = Color.RED
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val now = SystemClock.uptimeMillis()
        if (mMovieStart == 0) { // first time
            mMovieStart = now.toInt()
        }
        movie?.let {
            var dur = it.duration()
            if (dur == 0) {
                dur = 1000
            }
            it.setTime(((now - mMovieStart) % dur).toInt())

            // Draw Gif
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            it.draw(
                canvas,
                (width / 2 - it.width() / 2).toFloat(),
                (height / 2 - it.height() / 2).toFloat(),
                paint
            )

            // Clip corners
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            canvas?.drawBitmap(getBitmap(R.drawable.background_r20), rect, rect, paint)

            // Fill corners with white
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
            paint.color = Color.WHITE
            canvas?.drawRect(rectF, paint)

            invalidate()
        }
    }

    private fun getBitmap(drawableRes: Int): Bitmap? {
        val drawable = resources.getDrawable(drawableRes)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
}