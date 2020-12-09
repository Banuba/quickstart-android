package com.banuba.sdk.example.effect_player_realtime_preview

import com.banuba.sdk.arcloud.di.ArCloudKoinModule
import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.example.common.BANUBA_CLIENT_TOKEN
import com.banuba.sdk.example.effect_player_realtime_preview.arcloud.MainKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()


        startKoin {
            androidContext(this@Application)
            modules(ArCloudKoinModule().module + MainKoinModule().module)
        }

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