package com.example.post28.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.heifwriter.HeifWriter
import com.example.post28.databinding.ActivityHdrBinding
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HDRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHdrBinding

    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHdrBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

//        DownloadFile().execute(VIDEO_URL)
        with(binding) {

            nextButton.setOnClickListener {
                startActivity(Intent(this@HDRActivity, NeuralNetworkActivity::class.java))
            }

            with(videoView) {
                setVideoPath(VIDEO_URL)
                setOnErrorListener { p0, p1, p2 ->
                    visibility = View.GONE
                    hdrErrorTextView.visibility = View.VISIBLE

                    return@setOnErrorListener true
                }

                start()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                heifSaveButton.setOnClickListener {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI = FileProvider.getUriForFile(
                            this@HDRActivity,
                            "com.example.post28.fileprovider",
                            it
                        )

                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(intent, PHOTO_REQUEST_CODE)
                    }
                }

                heifLoadButton.setOnClickListener {
                    val savedImage = File("$cacheDir/$PHOTO_HEIF_NAME")
                    if (savedImage.exists()) {
                        heifImageView.setImageURI(
                            FileProvider.getUriForFile(
                                this@HDRActivity,
                                "com.example.post28.fileprovider",
                                savedImage
                            )
                        )
                    }
                }
            } else heifLayout.visibility = View.GONE
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            val photoURI = FileProvider.getUriForFile(
                this,
                "com.example.post28.fileprovider",
                File(currentPhotoPath)
            )
            val bitmap = MediaStore.Images.Media.getBitmap(
                contentResolver, photoURI
            )
            val heifWriter = HeifWriter.Builder(
                "$cacheDir/$PHOTO_HEIF_NAME",
                bitmap.width,
                bitmap.height,
                HeifWriter.INPUT_MODE_BITMAP
            ).build()

            heifWriter.start()
            heifWriter.addBitmap(bitmap)
            heifWriter.stop(1000)
            heifWriter.close()

        } else super.onActivityResult(requestCode, resultCode, data)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            cacheDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    inner class DownloadFile : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg p0: String?): String {
            if (externalCacheDir == null)
                return ""

            val fileName = "${externalCacheDir!!.path}/$VIDEO_NAME.$VIDEO_EXTENSION"
            if (File(fileName).exists())
                return ""

            val url = URL(p0[0])
            val connection = url.openConnection()
            connection.connect()

            val input = BufferedInputStream(url.openStream(), 8192)
            val output = FileOutputStream(fileName)

            val data = ByteArray(1024)
            var count = input.read(data)

            while (count != -1) {
                output.write(data, 0, count)
                count = input.read(data)
            }

            // flushing output
            output.flush()

            // closing streams
            output.close()
            input.close()

            return ""
        }

        override fun onPostExecute(result: String?) {
            val fileName = "${externalCacheDir!!.path}/$VIDEO_NAME.$VIDEO_EXTENSION"
            if (!File(fileName).exists())
                return

            binding.videoView.setVideoPath(fileName)
            binding.videoView.start()
        }
    }

    companion object {
        private const val PHOTO_HEIF_NAME = "photo.heif"
        private const val PHOTO_REQUEST_CODE = 61876
        private const val VIDEO_URL = "http://192.168.1.50:8000/vp9.mkv"
        //        private const val VIDEO_URL = "http://192.168.1.50:8000/vp9.mp4"
        private const val VIDEO_NAME = "video"
        private const val VIDEO_EXTENSION = "mkv"
    }
}