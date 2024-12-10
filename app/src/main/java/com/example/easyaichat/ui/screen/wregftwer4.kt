/*
package com.example.easyaichat.ui.screen


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyaichat.databinding.FragmentChatBinding
import com.example.easyaichat.ui.adapter.ChatAdapter
import com.example.easyaichat.ui.viewmodel.ChatViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.ExperimentalUuidApi

class ChatFragment : Fragment() {

    private val providers = listOf("OpenAI")
    private val modelsByProvider = mapOf(
        "OpenAI" to listOf("gpt-3.5-turbo", "gpt-4", "gpt-4-turbo")
    )
    private val args: ChatFragmentArgs by navArgs()
    private val viewModel: ChatViewModel by viewModel() {
        parametersOf(args.chatId)
    }

    private var _binding: FragmentChatBinding? = null
    private val binding get() = checkNotNull(_binding)

    //intent
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.addImage(it.toString()) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*
        // First, observe the UI state to catch the initial load
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Immediately set dropdowns when we get the initial state
                state.selectedApiKeyEntity?.let { apiKey ->
                    binding.providerDropdown.setText(apiKey.provider)

                    // Get and set available models for this provider
                    val availableModels = viewModel.apiKeyEntities.value
                        .filter { it.provider == apiKey.provider }
                        .map { it.model }
                        .distinct()

                    val modelAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        availableModels
                    )
                    binding.modelDropdown.setAdapter(modelAdapter)
                    binding.modelDropdown.setText(apiKey.model)
                }
            }
        }
        */

        setupRecyclerView()
        setupDropdowns()
        setupClickListeners()
        setupMessageInputListener()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeViewModel()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMessageInputListener() {
        binding.messageInput.doOnTextChanged { text, _, _, _ ->
            updateSendButtonState(
                text = text?.toString() ?: "",
                uiState = viewModel.uiState.value
            )
        }
    }

    // Add this new function
    private fun updateSendButtonState(text: String, uiState: ChatViewModel.ChatUiState) {
        binding.sendButton.isEnabled = text.isNotBlank() &&
                uiState.isProviderSelected &&
                uiState.isModelSelected
    }
    private fun setupDropdowns() {
        val providerInput = binding.providerDropdown
        val modelInput = binding.modelDropdown

        viewLifecycleOwner.lifecycleScope.launch {
            // Setup initial providers from API keys
            viewModel.apiKeyEntities.collect { apiKeys ->

                val chatEntity = viewModel.uiState.value.chatEntity
                val apiKeyEntity = viewModel.uiState.value.selectedApiKeyEntity

                if (chatEntity != null && apiKeyEntity != null) {
                    // For existing chat, use the chat's stored provider and model
                    if (providerInput.text.toString() != apiKeyEntity.provider) {
                        providerInput.setText(apiKeyEntity.provider, false)

                        // Update model dropdown for this provider
                        val availableModels = apiKeys
                            .filter { it.provider == apiKeyEntity.provider }
                            .map { it.defaultModel }
                            .distinct()

                        val modelAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            availableModels
                        )
                        modelInput.setAdapter(modelAdapter)
                    }

                    // Important: Set the model from chatEntity, not apiKeyEntity
                    if (modelInput.text.toString() != chatEntity.model) {
                        modelInput.setText(chatEntity.model, false)
                    }
                } else{
                    val chatEntity = viewModel.uiState.value.chatEntity
                    val apiKeyEntity = viewModel.uiState.value.selectedApiKeyEntity

                    if (apiKeyEntity != null) {
                        if (providerInput.text.toString() != apiKeyEntity.provider) {
                            providerInput.setText(apiKeyEntity.provider, false)

                            // Set up initial models for this provider
                            val initialModels = apiKeys
                                .filter { it.provider == apiKeyEntity.provider }
                                .map { it.defaultModel }
                                .distinct()

                            val initialModelAdapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                initialModels
                            )
                            modelInput.setAdapter(initialModelAdapter)
                        }
                    }

                    // Set initial model if not already set
                    if (chatEntity != null) {
                        if (modelInput.text.toString() != chatEntity.model) {
                            modelInput.setText(chatEntity.model, false)
                        }
                    }
                }

            }
        }
    }
    /*
        private fun setupDropdowns() {
            val providerInput = binding.providerDropdown
            val modelInput = binding.modelDropdown

            viewLifecycleOwner.lifecycleScope.launch {
                // Observe apiKeyEntities to populate dropdowns
                viewModel.apiKeyEntities.collect { apiKeys ->
                    // Get unique providers from API keys
                    val availableProviders = apiKeys.map { it.provider }.distinct()

                    // Setup provider dropdown
                    val providerAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        availableProviders
                    )
                    providerInput.setAdapter(providerAdapter)

                    // Handle provider selection
                    providerInput.setOnItemClickListener { _, _, position, _ ->
                        val selectedProvider = availableProviders[position]

                        // Get models for selected provider from API keys
                        val availableModels = apiKeys
                            .filter { it.provider == selectedProvider }
                            .map { it.model }
                            .distinct()

                        // Update model dropdown
                        val modelAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            availableModels
                        )
                        modelInput.setAdapter(modelAdapter)
                        modelInput.text.clear()

                        // Update ViewModel
                        viewModel.updateProvider(selectedProvider)
                    }

                    // Handle model selection
                    modelInput.setOnItemClickListener { _, _, _, _ ->
                        val selectedModel = modelInput.text.toString()
                        viewModel.updateModel(selectedModel)
                    }

                    // Set initial values if available
                    viewModel.uiState.value.selectedApiKeyEntity?.let { selectedKey ->
                        if (providerInput.text.toString() != selectedKey.provider) {
                            providerInput.setText(selectedKey.provider, false)

                            // Update model dropdown
                            val availableModels = apiKeys
                                .filter { it.provider == selectedKey.provider }
                                .map { it.model }
                                .distinct()

                            val modelAdapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                availableModels
                            )
                            modelInput.setAdapter(modelAdapter)
                        }
                        if (modelInput.text.toString() != selectedKey.model) {
                            modelInput.setText(selectedKey.model, false)
                        }
                    }
                }
            }
        }

     */

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        binding.chatRecyclerView.layoutManager = layoutManager
        viewLifecycleOwner.lifecycleScope.launch {
            //Whenever the messages within viewModel is observed to be different,
            //The change causes the adapter to be reinstantiated... PERHAPS INEFFICEINT.
            viewModel.messages.collect { messages ->
                Log.d("ChatFragment", "Messages received SIZE: ${messages.size}")
                Log.d("ChatFragment", "Messages received: ${messages}")
                binding.chatRecyclerView.adapter = ChatAdapter(messages)
                // Scroll to the bottom when new messages are added
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
            }

        }
    }
    /*
binding.chatRecyclerView.apply {
    adapter = chatAdapter
    layoutManager = LinearLayoutManager(context).apply {
        stackFromEnd = true
        reverseLayout = false
    }
    adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            scrollToPosition(adapter?.itemCount?.minus(1) ?: 0)
        }
    })
}

 */


    private fun setupClickListeners() {
        binding.apply {
            sendButton.setOnClickListener {
                val message = messageInput.text.toString().trim()
                if (message.isNotEmpty()) {
                    viewModel.sendMessage(message)
                    messageInput.setText("")
                    Toast.makeText(requireContext(), "SEND BUTTON CLICKED", Toast.LENGTH_SHORT)
                        .show()
                }

            }

            photoButton.setOnClickListener {
                pickImage.launch("image/*")
            }

            chatTitle.doOnTextChanged { text, _, _, _ ->
                viewModel.updateChat { oldChat ->
                    oldChat.copy(title = text.toString())
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        updateUi(state)
                    }
                }

                launch {
                    viewModel.selectedImages.collect { images ->
                        // Update UI to show selected images
                        // You might want to add a horizontal scrollview to show selected images
                    }
                }
            }
        }
    }

    private fun updateUi(state: ChatViewModel.ChatUiState) {
        binding.apply {
            // If state.chatEntity is null, the let block inside the code snippet will not execute
            state.chatEntity?.let { chat ->
                if (chatTitle.text.toString() != chat.title) {
                    chatTitle.setText(chat.title)
                }
            }
            updateSendButtonState(
                text = messageInput.text?.toString() ?: "",
                uiState = state
            )

            // Show error if any
            state.error?.let { error ->
                Snackbar.make(root, error, Snackbar.LENGTH_LONG).show()
            }

            state.selectedApiKeyEntity?.provider.let { provider ->
                if (providerDropdown.text.toString() != provider) {
                    providerDropdown.setText(provider, false)
                }
            }

            state.selectedApiKeyEntity?.defaultModel.let { model ->
                if (modelDropdown.text.toString() != model) {
                    modelDropdown.setText(model, false)
                }
            }
        }
    }

}

 */