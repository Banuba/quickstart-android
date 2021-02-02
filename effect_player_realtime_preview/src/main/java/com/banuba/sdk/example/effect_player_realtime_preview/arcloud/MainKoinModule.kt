package com.banuba.sdk.example.effect_player_realtime_preview.arcloud

import com.banuba.sdk.arcloud.data.source.ArEffectsRepositoryProvider
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
            "44002e77-8a84-4583-bc26-d02a41f76c38"
        }


        viewModel {
            EffectsViewModel(
                arEffectsRepository = get<ArEffectsRepositoryProvider>().provide()
            )
        }

    }


}