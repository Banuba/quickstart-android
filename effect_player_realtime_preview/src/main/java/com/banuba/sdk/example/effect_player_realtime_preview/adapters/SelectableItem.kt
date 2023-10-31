package com.banuba.sdk.example.effect_player_realtime_preview.adapters

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectableItem (val name: String, val isSelected: Boolean) : Parcelable
