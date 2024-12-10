package com.example.easyaichat.ui.screen

import android.R
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easyaichat.databinding.FragmentChatBinding
import com.example.easyaichat.ui.adapter.ChatAdapter
import com.example.easyaichat.ui.adapter.ImageClickListener
import com.example.easyaichat.ui.viewmodel.ChatViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.fragment.findNavController
import java.util.UUID

/**
 * Fragment that represents the chat screen where users can send and receive messages.
 */
class ChatFragment : Fragment(), ImageClickListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val args: ChatFragmentArgs by navArgs()
    private val viewModel: ChatViewModel by viewModel {
        parametersOf(args.chatId)
    }

    // RecyclerView Adapter
    private lateinit var chatAdapter: ChatAdapter

    // Image Picker
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                requireContext().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.addImage(it.toString())
            }
        }
    private fun handleLoadingState(isLoading: Boolean) {
        val loadingImage: ImageView = binding.loadingImage
        if (isLoading) {
            // Show loading image and start animation
            loadingImage.visibility = View.VISIBLE
            if (loadingImage.animation == null) {
                val rotateAnimation = AnimationUtils.loadAnimation(context, com.example.easyaichat.R.anim.rotate)
                loadingImage.startAnimation(rotateAnimation)
            }
        } else {
            // Hide loading image and clear animation
            loadingImage.clearAnimation()
            loadingImage.visibility = View.GONE
        }
    }
    private fun requestIsLoading() {
        val loadingImage: ImageView = binding.loadingImage
        loadingImage.visibility = View.VISIBLE
        val rotateAnimation =
            AnimationUtils.loadAnimation(context, com.example.easyaichat.R.anim.rotate)
        loadingImage.startAnimation(rotateAnimation)
        loadingImage.postDelayed({
            loadingImage.clearAnimation()
            loadingImage.visibility = android.view.View.GONE
        }, 3500)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun setupChatTitleListener() {
        binding.chatTitleEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.setEditableTitle(text.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadMessages()
        // Set up the chatTitleEditText listener
        setupChatTitleListener()

        // Handle navigation events to save the title
        handleNavigationEvents()


        // Observe new chat ID events
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.newChatId
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { newChatId ->
                    navigateToNewChat(newChatId)
                }
        }
        // Observe selected images to update UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedImages.collect { images ->
                updateSendButtonState(binding.messageInput.text.toString(), images.isNotEmpty())
            }
        }
    }
    private fun navigateToNewChat(newChatId: UUID) {
        val action = ChatFragmentDirections.actionNavigationToCreatedChat(newChatId)
        findNavController().navigate(action)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.messages.collect { messages ->
                    chatAdapter.updateMessages(messages)
                    binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                }
            }

            launch {
                viewModel.uiState.collect { state ->
                    state.error?.let { errorMsg ->
                        Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
                        // Optionally, clear the error after displaying
                        viewModel.clearError()
                    }
                    state.chatEntity?.let { chat ->
                        // Initialize the editableTitle if not set
                        if (viewModel.editableTitle.value.isEmpty()) {
                            viewModel.setEditableTitle(chat.title)
                        }
                    }
                    // Handle loading state
                    handleLoadingState(state.isLoading)
                }
            }
            launch {
                viewModel.editableTitle.collect { title ->
                    // Avoid updating the EditText if it's already displaying the correct title
                    if (binding.chatTitleEditText.text.toString() != title) {
                        binding.chatTitleEditText.setText(title)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Sets up the main chat messages RecyclerView using ChatAdapter.
     */
    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        binding.chatRecyclerView.layoutManager = layoutManager

        // Initialize the adapter with an empty list
        chatAdapter = ChatAdapter(emptyList(), this)
        binding.chatRecyclerView.adapter = chatAdapter
    }

    /**
     * Sets up the provider, model, and API key dropdowns based on available API keys.
     */


    private fun setupDropdowns() {
        val nicknameProviderInput = binding.nicknameProviderDropdown
        val modelInput = binding.modelDropdown

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    viewModel.apiKeyEntities,
                    viewModel.providerToModelMap,
                    viewModel.uiState
                ) { apiKeys, providerModels, uiState ->
                    Triple(apiKeys, providerModels, uiState)
                }.collect { (apiKeys, providerModels, uiState) ->
                    // Setup Provider Dropdown
                    val nicknameProviderList = apiKeys.map { "${it.nickname}_${it.provider}" }
                    nicknameProviderInput.setAdapter(
                        ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            nicknameProviderList
                        )
                    )

                    // Handle Provider Selection
                    uiState.selectedApiKeyEntity?.let { selectedKey ->
                        Log.d("ChatFragment", "Provider: ${selectedKey.provider}")
                        Log.d(
                            "ChatFragment",
                            "Available Models: ${providerModels[selectedKey.provider]}"
                        )

                        val selectedText = "${selectedKey.nickname}_${selectedKey.provider}"
                        if (nicknameProviderInput.text.toString() != selectedText) {
                            nicknameProviderInput.setText(selectedText, false)

                            // Get ALL models for the selected provider
                            val availableModels =
                                providerModels[selectedKey.provider] ?: emptyList()
                            val modelNames = availableModels.map { it.model }

                            Log.d("ChatFragment", "Model Names: $modelNames")
                            modelInput.setAdapter(
                                ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_dropdown_item_1line,
                                    modelNames
                                )
                            )

                            // Set Model Selection
                            val modelToSelect =
                                uiState.chatEntity?.model ?: selectedKey.defaultModel
                            if (modelInput.text.toString() != modelToSelect) {
                                modelInput.setText(modelToSelect, false)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle saving the title when navigating away.
     */
    private fun handleNavigationEvents() {
        // Example: Save title when the fragment is destroyed
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                viewModel.saveChatTitle()
            }
        })
    }
    /**
     * Sets up click listeners for send button, image button, and chat title.
     */
    private fun setupClickListeners() {
        binding.apply {
            sendButton.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val message = messageInput.text.toString().trim()
                    val hasImages = viewModel.selectedImages.value.isNotEmpty()
                    if (message.isNotEmpty() || hasImages) {
                        // Save the title before sending the message
                        viewModel.saveChatTitle()
                        viewModel.sendMessage(
                            content = message,
                            images = viewModel.selectedImages.value
                        )
                        messageInput.setText("")
                        viewModel.clearSelectedImages()
                        //requestIsLoading()
                        Toast.makeText(requireContext(), "Message Sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Cannot send empty message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            photoButton.setOnClickListener {
                pickImage.launch("image/*")
            }

            // Optional: Handle text input changes to update send button state
            messageInput.doOnTextChanged { text, _, _, _ ->
                updateSendButtonState(text.toString(), viewModel.selectedImages.value.isNotEmpty())
            }
        }
    }

    fun updateSendButtonState(text: String, hasImages: Boolean) {
        binding.sendButton.isEnabled = text.isNotBlank() || hasImages
    }

    private fun loadMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                chatAdapter.updateMessages(messages)
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    override fun onImageClick(imageUri: String) {
        val dialog = EnlargeImageDialogFragment.newInstance(imageUri)
        dialog.show(parentFragmentManager, "EnlargeImageDialog")
    }

}
