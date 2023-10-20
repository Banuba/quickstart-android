package com.banuba.sdk.example.effect_player_realtime_preview

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.banuba.sdk.effect_player.CameraOrientation
import com.banuba.sdk.entity.RecordedVideoInfo
import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.manager.IEventCallback
import com.banuba.sdk.types.Data
import com.banuba.sdk.types.FullImageData
import kotlinx.android.synthetic.main.activity_image.applyEffect
import kotlinx.android.synthetic.main.activity_image.imageSurface
import kotlinx.android.synthetic.main.activity_image.imageView


class ImageActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 100
        private const val TAG = "ImageActivity"
    }

    private val callback = object : IEventCallback {
        override fun onCameraOpenError(p0: Throwable) {
            Log.d(TAG, "onCameraOpenError")
        }

        override fun onCameraStatus(p0: Boolean) {
            Log.d(TAG, "onCameraStatus")
        }

        override fun onScreenshotReady(p0: Bitmap) {
            Log.d(TAG, "onScreenshotReady")
        }

        override fun onHQPhotoReady(p0: Bitmap) {
            Log.d(TAG, "onHQPhotoReady")
        }

        override fun onVideoRecordingFinished(p0: RecordedVideoInfo) {
            Log.d(TAG, "onVideoRecordingFinished")
        }

        override fun onVideoRecordingStatusChange(p0: Boolean) {
            Log.d(TAG, "onVideoRecordingStatusChange")
        }

        override fun onImageProcessed(p0: Bitmap) {
            Log.d(TAG, "onImageProcessed")
        }

        override fun onFrameRendered(p0: Data, p1: Int, p2: Int) {
            Log.d(TAG, "onFrameRendered")
        }
    }

    private val banubaSdkManager by lazy(LazyThreadSafetyMode.NONE) {
        BanubaSdkManager(applicationContext).apply {
            setCallback(callback)
        }
    }

    private var sourceBitmap: Bitmap? = null

    private val maskUri by lazy(LazyThreadSafetyMode.NONE) {
        Uri.parse(BanubaSdkManager.getResourcesBase())
            .buildUpon()
            .appendPath("effects")
            .appendPath("TrollGrandma")
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        pickImage()

        applyEffect.setOnClickListener {
            banubaSdkManager.effectPlayer.playbackPlay()

            banubaSdkManager.loadEffect(maskUri.toString(), true)

            Log.d(TAG, "Process image")
            banubaSdkManager.processImage(
                FullImageData(
                    sourceBitmap, FullImageData.Orientation(CameraOrientation.DEG_0)
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        banubaSdkManager.attachSurface(imageSurface)
    }

    private fun pickImage() {
        val i = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(i, "Select Picture"), REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            val uri = data!!.data
            sourceBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);

            imageView.setImageBitmap(sourceBitmap!!)
        }
    }
}