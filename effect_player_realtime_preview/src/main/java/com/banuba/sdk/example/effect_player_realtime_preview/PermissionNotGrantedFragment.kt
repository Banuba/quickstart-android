package com.banuba.sdk.example.effect_player_realtime_preview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banuba.sdk.example.effect_player_realtime_preview.databinding.FragmentPermissionNotGrantedBinding

class PermissionNotGrantedFragment : Fragment() {
    private var _binding: FragmentPermissionNotGrantedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissionNotGrantedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAllowCameraAccessBtn.setOnClickListener {
            (activity as MainActivity).askForCameraPermission()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}