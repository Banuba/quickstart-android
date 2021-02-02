package com.banuba.sdk.example.effect_player_realtime_preview.arcloud

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.banuba.sdk.example.effect_player_realtime_preview.R
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_effect.*
import java.io.File

class EffectsAdapter(
    private val glideRequestManager: RequestManager
) : ListAdapter<EffectWrapper, EffectsAdapter.EffectViewHolder>(DIFF_CALLBACK) {

    interface ActionCallback {
        fun onEffectSelected(checkableEffect: EffectWrapper, position: Int)
        fun onEffectStartDownloading(checkableEffect: EffectWrapper, position: Int)
    }

    var actionCallback: ActionCallback? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_effect, parent, false)
        return EffectViewHolder(view)
    }

    override fun onBindViewHolder(holder: EffectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EffectViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(effectWrapper: EffectWrapper) {

            containerView.setOnClickListener {
                if (URLUtil.isNetworkUrl(effectWrapper.effect.uri)) {
                    actionCallback?.onEffectStartDownloading(effectWrapper, adapterPosition)
                } else {
                    actionCallback?.onEffectSelected(effectWrapper, adapterPosition)
                }
            }

            handleEffectPreview(effectImageView, effectWrapper)

            handleDownloadingAnimation(downloadingAnimationView, effectDownloadBtn, effectWrapper)
        }

        private fun handleEffectPreview(imageView: ImageView, effectWrapper: EffectWrapper) {
            val previewUri = if (URLUtil.isNetworkUrl(effectWrapper.effect.preview)) {
                effectWrapper.effect.preview?.toUri()
            } else {
                effectWrapper.effect.preview?.let {
                    File(it).toUri()
                }
            } ?: Uri.EMPTY

            if (previewUri == Uri.EMPTY) {
                effectImageView.setImageResource(R.drawable.bg_effect_normal)
            } else {
                val requestOptions = RequestOptions().apply {
                    transform(CenterCrop(), CircleCrop())
                    error(R.drawable.bg_effect_error)
                }
                glideRequestManager.load(previewUri).apply(requestOptions).into(imageView)
            }
        }

        private fun handleDownloadingAnimation(downloadingView: LottieAnimationView, downloadBtn: View,  effectWrapper: EffectWrapper) {
            if (URLUtil.isNetworkUrl(effectWrapper.effect.uri)) {
                downloadingView.isVisible = effectWrapper.isDownloading
                if (effectWrapper.isDownloading) {
                    downloadingView.playAnimation()
                    downloadBtn.visibility = View.GONE
                } else {
                    downloadingView.cancelAnimation()
                    downloadBtn.visibility = View.VISIBLE
                }
            } else {
                downloadBtn.visibility = View.GONE
                downloadingView.visibility = View.GONE
                downloadingView.cancelAnimation()
            }
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EffectWrapper>() {

    override fun areItemsTheSame(oldItem: EffectWrapper, newItem: EffectWrapper): Boolean {
        return oldItem.effect.name == newItem.effect.name && oldItem.effect.id == newItem.effect.id
    }

    override fun areContentsTheSame(oldItem: EffectWrapper, newItem: EffectWrapper): Boolean {
        return oldItem == newItem
    }
}

