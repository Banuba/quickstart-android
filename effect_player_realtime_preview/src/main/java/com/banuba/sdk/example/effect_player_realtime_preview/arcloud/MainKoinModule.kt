package com.banuba.sdk.example.effect_player_realtime_preview.arcloud

import com.banuba.sdk.arcloud.data.source.ArEffectsRepositoryProvider
import com.banuba.sdk.example.effect_player_realtime_preview.R
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

class MainKoinModule {

    val module = module {

        single(createdAtStart = true, override = true) {
            ArEffectsRepositoryProvider(
                arEffectsRepository = get(named("backendArEffectsRepository")),
                ioDispatcher = get(named("ioDispatcher"))
            )
        }

        single(named("arEffectsCloudUuid"), override = true) {
            androidContext().getString(R.string.ar_cloud_client_id)
        }


        viewModel {
            EffectsViewModel(
                arEffectsRepository = get<ArEffectsRepositoryProvider>().provide()
            )
        }

    }


}