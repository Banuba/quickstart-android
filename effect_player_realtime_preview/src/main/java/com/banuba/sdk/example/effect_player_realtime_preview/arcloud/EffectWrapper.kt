package com.banuba.sdk.example.effect_player_realtime_preview.arcloud

import com.banuba.sdk.arcloud.data.source.model.ArEffect

data class EffectWrapper(
    var effect: ArEffect,
    var isDownloading: Boolean = false
)