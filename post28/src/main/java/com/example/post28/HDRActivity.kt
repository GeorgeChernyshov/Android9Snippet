package com.example.post28

import android.media.MediaPlayer
import android.media.MediaPlayer.OnErrorListener
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.post28.databinding.ActivityHdrBinding
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.ProtocolException
import java.net.URL

class HDRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHdrBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHdrBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

//        DownloadFile().execute(VIDEO_URL)
        with (binding.videoView) {
            setVideoPath(VIDEO_URL)
            setOnErrorListener { p0, p1, p2 ->
                visibility =  View.GONE
                binding.hdrErrorTextView.visibility = View.VISIBLE

                return@setOnErrorListener true
            }

            start()
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
        private const val VIDEO_URL = "http://192.168.1.50:8000/vp9.mkv"
//        private const val VIDEO_URL = "http://192.168.1.50:8000/vp9.mp4"
        private const val VIDEO_NAME = "video"
        private const val VIDEO_EXTENSION = "mkv"
    }
}