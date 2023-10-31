package com.banuba.sdk.example.effect_player_realtime_preview.camera

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.banuba.sdk.example.effect_player_realtime_preview.adapters.SelectableItem
import com.banuba.sdk.example.effect_player_realtime_preview.data.DataRepository
import com.banuba.sdk.example.effect_player_realtime_preview.databinding.FragmentCameraBinding
import com.banuba.sdk.example.effect_player_realtime_preview.media.VideoInput
import com.banuba.sdk.input.CameraDevice
import com.banuba.sdk.input.CameraDeviceConfigurator.*
import com.banuba.sdk.input.CameraInput
import com.banuba.sdk.output.SurfaceOutput
import com.banuba.sdk.output.VideoOutput
import com.banuba.sdk.player.Player
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

class CameraViewModel(
        private val application: Application,
        private val binding: FragmentCameraBinding,
        private val lifecycleOwner: LifecycleOwner
): AndroidViewModel(application) {

    companion object {
        private const val MASK_NAME = "effects/TrollGrandma"
        private const val VIDEO_FILE = "face_video_1080p.mp4"
    }

    private val player by lazy(LazyThreadSafetyMode.NONE) {
        Player()
    }

    private val cameraDevice by lazy(LazyThreadSafetyMode.NONE) {
        CameraDevice(application, lifecycleOwner)
    }

    private val surfaceOutput by lazy(LazyThreadSafetyMode.NONE) {
        SurfaceOutput(binding.surfaceView.holder)
    }

    private val videoOutput by lazy(LazyThreadSafetyMode.NONE) {
        VideoOutput()
    }

    private val videoInput by lazy(LazyThreadSafetyMode.NONE) {
        VideoInput(application.applicationContext)
    }

    private val _listOfPreview = MutableLiveData<List<SelectableItem>>()
    val listOfPreview: LiveData<List<SelectableItem>>
        get() = _listOfPreview

    private val _actionString = MutableLiveData<String?>()
    val actionString: LiveData<String?>
        get() = _actionString

    private val _processedVideoFile = MutableLiveData<File>()
    val processedVideoFile: LiveData<File>
        get() = _processedVideoFile

    private val _waitForFinish = MutableLiveData<Boolean>()
    val waitForFinish: LiveData<Boolean>
        get() = _waitForFinish

    private var cameraFacing = LensSelector.FRONT

    private var nextActionIndex = 0

    private var _preview: DataRepository.Preview? = null
    private val preview: DataRepository.Preview
        get() = _preview!!

    private var lastVideoFile: File = File("")
    private var canSwitchPreview: Boolean = true

    init {
        selectPreview(SelectableItem(DataRepository.listOfPreview.get(0).name, false))
        player.use(CameraInput(cameraDevice), surfaceOutput)
        cameraDevice.start()
        player.play()

        lifecycleOwner.lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                player.play()
            }

            override fun onPause(owner: LifecycleOwner) {
                player.pause()
            }
        })
    }

    fun switchCamera() {
        if (!canSwitchPreview) {
            return
        }
        cameraFacing = if (cameraFacing == LensSelector.FRONT) LensSelector.BACK else LensSelector.FRONT
        cameraDevice.configurator.setLens(cameraFacing).commit()
    }

    private fun resetOldState() {
        videoOutput.stopRecording()
        videoInput.stopProcessing()
        player.loadAsync("")
    }

    fun doAction() {
        val option = preview.options.get(nextActionIndex)
        when (option.action) {
            DataRepository.Action.NOTHING -> {
                // DO NOTHING
            }

            DataRepository.Action.APPLY_MASK -> {
                player.setRenderMode(Player.RenderMode.LOOP)
                player.use(CameraInput(cameraDevice), surfaceOutput)
                player.loadAsync(MASK_NAME)
            }
            DataRepository.Action.HIDE_MASK -> {
                player.loadAsync("")
            }

            DataRepository.Action.START_RECORDING -> {
                player.setRenderMode(Player.RenderMode.LOOP)
                player.use(CameraInput(cameraDevice), surfaceOutput)
                player.addOutput(videoOutput)
                lastVideoFile = generateVideoFilePath()
                videoOutput.startRecording(lastVideoFile)
            }
            DataRepository.Action.STOP_RECORDING -> {
                videoOutput.stopRecordingAndWaitForFinish()
            }

            DataRepository.Action.START_PROCESSING -> {
                player.setRenderMode(Player.RenderMode.MANUAL)
                player.use(videoInput, surfaceOutput)
                player.addOutput(videoOutput)
                player.loadAsync(MASK_NAME)

                thread(start = true) {
                    _waitForFinish.postValue(true)
                    val inputFile = FileUtils.copyFromAssetsToFile(application.applicationContext, VIDEO_FILE)
                    lastVideoFile = generateVideoFilePath()
                    videoOutput.startRecording(lastVideoFile)
                    videoInput.processVideoFile(inputFile, object : VideoInput.IVideoFrameStatus {
                        override fun onError(throwable: Throwable) {
                            throw throwable;
                        }

                        override fun onFrameDecoded(frameTimeNanos: Long) {
                            player.render()
                        }

                        override fun onFinished() {
                            videoOutput.stopRecordingAndWaitForFinish();
                            nextActionIndex = 2
                            canSwitchPreview = preview.options.get(nextActionIndex).canSwitchPreview
                            _actionString.postValue(preview.options.get(nextActionIndex).name)
                            _waitForFinish.postValue(false)
                        }
                    })

                    player.setRenderMode(Player.RenderMode.LOOP)
                    player.use(CameraInput(cameraDevice), surfaceOutput)
                    player.loadAsync("")
                }
            }

            DataRepository.Action.PLAY_LAST_VIDEO -> {
                _processedVideoFile.postValue(lastVideoFile)
            }
        }
        nextActionIndex = option.nextOptionIndex
        canSwitchPreview = preview.options.get(nextActionIndex).canSwitchPreview
        _actionString.postValue(preview.options.get(nextActionIndex).name)
    }

    fun selectPreview(item: SelectableItem) {
        if (!canSwitchPreview || _preview != null && item.name == preview.name) {
            return
        }
        resetOldState()
        _preview = DataRepository.listOfPreview.find { it.name == item.name }
        _listOfPreview.postValue(DataRepository.listOfPreview.map {
            SelectableItem(it.name, preview == it)
        })
        nextActionIndex = 0
        _actionString.postValue(preview.options.get(nextActionIndex).name)
    }

    private fun generateVideoFilePath(): File
            = File(application.filesDir, "banuba_video_${System.currentTimeMillis()}.mp4")
}

class CameraViewModelFactory(
        private val application: Application,
        private val binding: FragmentCameraBinding,
        private val lifecycleOwner: LifecycleOwner
): ViewModelProvider.NewInstanceFactory() {
    override fun <T: ViewModel> create(modelClass:Class<T>): T
            = CameraViewModel(application, binding, lifecycleOwner) as T
}

class FileUtils {

    companion object {

        @JvmStatic
        fun copyFromAssetsToFile(context: Context, filename: String): File {

            val file = File(context.getExternalFilesDir(null), filename)
            val dir = file.parentFile
            dir?.mkdirs()
            context.assets.open(filename).copyTo(FileOutputStream(file))
            return file
        }
    }
}
