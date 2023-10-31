package com.banuba.sdk.example.effect_player_realtime_preview.camera

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.content.FileProvider
import androidx.core.view.doOnAttach
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.banuba.sdk.example.effect_player_realtime_preview.BuildConfig
import com.banuba.sdk.example.effect_player_realtime_preview.adapters.CenterLayoutManager
import com.banuba.sdk.example.effect_player_realtime_preview.databinding.FragmentCameraBinding
import com.banuba.sdk.example.effect_player_realtime_preview.adapters.PreviewListAdapter
import java.io.File

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CameraViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
                requireActivity(),
                CameraViewModelFactory(requireActivity().application, binding, viewLifecycleOwner)
        )[CameraViewModel::class.java]
        return binding.root
    }

    private val previewListAdapter by lazy(LazyThreadSafetyMode.NONE) {
        PreviewListAdapter(getWidthOfScreen()) { item, position ->
            binding.previewList.smoothScrollToPosition(position)
            viewModel.selectPreview(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnAttach {
            binding.root.setPadding(0, 0, 0, getBottomInset(it))
        }

        initViews()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        binding.previewList.adapter = previewListAdapter
        binding.previewList.layoutManager = CenterLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.buttonSwitchCamera.setOnClickListener {
            viewModel.switchCamera()
        }

        binding.buttonAction.setOnClickListener {
            viewModel.doAction()
        }
    }

    private fun observeData() {
        viewModel.listOfPreview.observe(viewLifecycleOwner) { list ->
            previewListAdapter.submitList(list)
        }

        viewModel.actionString.observe(viewLifecycleOwner) { text ->
            binding.buttonAction.text = text
            binding.buttonAction.visibility = if (text == "") View.INVISIBLE else View.VISIBLE
        }

        viewModel.processedVideoFile.observe(viewLifecycleOwner) { file ->
            playVideo(file)
        }

        viewModel.waitForFinish.observe(viewLifecycleOwner) { show ->
            binding.progress.visibility = if (show) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun playVideo(videoFile: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider", videoFile
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setDataAndType(uri, "video/*")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }

    private fun getWidthOfScreen() =  Resources.getSystem().displayMetrics.widthPixels

    private fun getBottomInset(view: View): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                view.rootWindowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).bottom
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                view.rootWindowInsets?.stableInsetTop ?: 0
            }
            else -> {
                0
            }
        }
    }
}
