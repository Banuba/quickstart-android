package com.banuba.sdk.example.effect_player_realtime_preview.arcloud

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banuba.sdk.effect_player.Effect
import com.banuba.sdk.example.effect_player_realtime_preview.*
import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.manager.BanubaSdkTouchListener
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_ar_cloud_effects.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArCloudMasksActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_APPLY_MASK_PERMISSION = 1001

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val effectsViewModel by viewModel<EffectsViewModel>()

    private val banubaSdkManager by lazy(LazyThreadSafetyMode.NONE) {
        BanubaSdkManager(applicationContext)
    }

    var effectsAdapter: EffectsAdapter? = null

    private var effect: Effect? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_cloud_effects)

        // Set custom OnTouchListener to change mask style.
        surfaceView.setOnTouchListener(BanubaSdkTouchListener(this, banubaSdkManager.effectPlayer))

        effectsAdapter = EffectsAdapter(Glide.with(this))
        effectsAdapter?.actionCallback = object : EffectsAdapter.ActionCallback {
            override fun onEffectSelected(checkableEffect: EffectWrapper, position: Int) {
                effectsViewModel.setLastEffect(checkableEffect)
                effect = if (position == 0) {
                    banubaSdkManager.effectManager.unload(effect)
                    null
                } else {
                    effect?.let { banubaSdkManager.effectManager.unload(it) }
                    banubaSdkManager.effectManager.loadAsync(checkableEffect.effect.uri)
                }
            }

            override fun onEffectStartDownloading(checkableEffect: EffectWrapper, position: Int) {
                effectsViewModel.downloadEffect(checkableEffect)
            }
        }

        effectsRv.run {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = effectsAdapter
            itemAnimator = null
        }

        effectsViewModel.effectsWrapperData.observe(this, Observer { effectsList ->
            effectsAdapter?.submitList(effectsList)
        })
        effectsViewModel.effectDownloadingSuccessData.observe(this, Observer { downloadingEffectWrapper ->
            effect?.let { banubaSdkManager.effectManager.unload(it) }
            effect = banubaSdkManager.effectManager.loadAsync(downloadingEffectWrapper.effect.uri)
        })


        effectsViewModel.load()
    }

    override fun onStart() {
        super.onStart()
        banubaSdkManager.attachSurface(surfaceView)

        if (allPermissionsGranted()) {
            banubaSdkManager.openCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_APPLY_MASK_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, results: IntArray
    ) {
        if (requireAllPermissionsGranted(permissions, results)) {
            banubaSdkManager.openCamera()
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        banubaSdkManager.effectPlayer.playbackPlay()
    }

    override fun onPause() {
        super.onPause()
        banubaSdkManager.effectPlayer.playbackPause()
    }

    override fun onStop() {
        super.onStop()
        banubaSdkManager.releaseSurface()
        banubaSdkManager.closeCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

}