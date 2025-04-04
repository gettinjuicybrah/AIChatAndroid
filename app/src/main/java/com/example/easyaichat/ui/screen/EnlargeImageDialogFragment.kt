package com.example.easyaichat.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import coil.load
import com.example.easyaichat.databinding.DialogEnlargeImageBinding

class EnlargeImageDialogFragment : DialogFragment() {

    private var _binding: DialogEnlargeImageBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_IMAGE_URI = "image_uri"

        fun newInstance(imageUri: String): EnlargeImageDialogFragment {
            val fragment = EnlargeImageDialogFragment()
            val args = Bundle()
            args.putString(ARG_IMAGE_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = DialogEnlargeImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUri = arguments?.getString(ARG_IMAGE_URI)
        if (imageUri != null) {
            binding.enlargedImageView.load(imageUri) {
                placeholder(com.example.easyaichat.R.drawable.ic_launcher_foreground)
                error(com.example.easyaichat.R.drawable.error_background)
            }
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.root.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}