package com.banuba.sdk.example.effect_player_realtime_preview

import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.example.common.BANUBA_CLIENT_TOKEN

class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        // It crashes if token is empty string with
        //
        // RuntimeException:
        //  Unable to create application com.banuba.sdk.samples.SamplesApp:
        //  java.lang.RuntimeException: Can't parse client token.
        //
        //  Please, contact Banuba for obtain a correct client token.

        BanubaSdkManager.initialize(this, BANUBA_CLIENT_TOKEN)
    }
}