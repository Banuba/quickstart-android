package com.banuba.sdk.example.effect_player_realtime_preview.arcloud

import androidx.lifecycle.*
import com.banuba.sdk.arcloud.data.source.ArEffectsRepository
import com.banuba.sdk.arcloud.data.source.model.ArEffect
import com.banuba.sdk.arcloud.data.source.model.EffectsLoadingResult
import kotlinx.coroutines.launch

class EffectsViewModel(
    private val arEffectsRepository: ArEffectsRepository
) : ViewModel() {

    private val _effectDownloadingSuccessData = MutableLiveData<EffectWrapper>()
    val effectDownloadingSuccessData: LiveData<EffectWrapper>
        get() = _effectDownloadingSuccessData

    val effectsWrapperData = MediatorLiveData<List<EffectWrapper>>().apply {
        addSource(arEffectsRepository.observeEffects()) { result ->
            if (result is EffectsLoadingResult.Success) {
                arEffects.clear()
                arEffects.add(ArEffect())
                arEffects.addAll(result.data.sortedBy { it.name })
                value = arEffects.map { EffectWrapper(it, downloadingData.contains(it.name)) }
            }
        }
    }

    private val downloadingData = mutableListOf<String>()

    private val arEffects = mutableListOf<ArEffect>()

    private var lastMaskEffect: ArEffect? = null

    fun load() {
        viewModelScope.launch {
            arEffectsRepository.getEffects()
        }
    }

    fun setLastEffect(effectWrapper: EffectWrapper) {
        lastMaskEffect = effectWrapper.effect
    }

    fun downloadEffect(effectWrapper: EffectWrapper) {
        lastMaskEffect = effectWrapper.effect
        if (!downloadingData.contains(effectWrapper.effect.name)) {
            downloadingData.add(effectWrapper.effect.name)

            postCheckableMasks()
            viewModelScope.launch {
                val resEffect = arEffectsRepository.getEffectData(effectWrapper.effect)
                downloadingData.remove(effectWrapper.effect.name)
                if (resEffect is EffectsLoadingResult.Success) {
                    if (lastMaskEffect?.name == effectWrapper.effect.name) {
                        _effectDownloadingSuccessData.value = EffectWrapper(resEffect.data, false)
                    }
                } else {
                    postCheckableMasks()
                }
            }
        }
    }

    private fun postCheckableMasks() {
        effectsWrapperData.postValue(arEffects.map {
            EffectWrapper(
                it, downloadingData.contains(it.name)
            )
        })
    }

}
