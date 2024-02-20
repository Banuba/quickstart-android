package com.banuba.sdk.example.effect_player_realtime_preview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.banuba.sdk.input.CameraDevice
import com.banuba.sdk.input.CameraInput
import com.banuba.sdk.output.SurfaceOutput
import com.banuba.sdk.player.Player
import com.banuba.sdk.player.PlayerTouchListener
import kotlinx.android.synthetic.main.activity_apply_mask.*
import kotlinx.android.synthetic.main.activity_camera_preview.surfaceView


/**
 * Sample activity that shows how to apply masks with Banuba SDK.
 * Some Banuba masks can change their appearance if tapping on them.
 */
class MaskActivity : AppCompatActivity(R.layout.activity_apply_mask) {
    companion object {
        private const val MASK_NAME = "effects/TrollGrandma"
    }

    // The player executes the main pipeline
    private val player by lazy(LazyThreadSafetyMode.NONE) {
        Player()
    }

    // This camera device will pass frames to the CameraInput
    private val cameraDevice by lazy(LazyThreadSafetyMode.NONE) {
        CameraDevice(requireNotNull(this.applicationContext), this@MaskActivity)
    }

    // The result will be displayed on the surface
    private val surfaceOutput by lazy(LazyThreadSafetyMode.NONE) {
        SurfaceOutput(surfaceView.holder)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set layer will take input frames from and where the player will display the result
        player.use(CameraInput(cameraDevice), surfaceOutput)

        // Set custom OnTouchListener to change mask style.
        surfaceView.setOnTouchListener(PlayerTouchListener(this, player))

        var shouldApply = false
        showMaskButton.setOnClickListener {
            shouldApply = !shouldApply

            showMaskButton.text = getString(if (shouldApply) R.string.hide_mask else R.string.show_mask)

            // The mask is loaded asynchronously and applied
            player.loadAsync(if (shouldApply) MASK_NAME else "")
        }
    }

    override fun onStart() {
        super.onStart()
        // We start the camera and then player starts taking frames
        cameraDevice.start()
    }

    override fun onResume() {
        super.onResume()
        // Running the player
        player.play()
    }

    override fun onPause() {
        super.onPause()
        // Pause the player when activity is inactive
        player.pause()
    }

    override fun onStop() {
        // After this method, the camera will stop capturing frames and transmitting them to player
        cameraDevice.stop()
        super.onStop()
    }

    override fun onDestroy() {
        // After you are done using the player, you must free all resources by calling close() method
        cameraDevice.close()
        surfaceOutput.close()
        player.close()
        super.onDestroy()
    }
}
