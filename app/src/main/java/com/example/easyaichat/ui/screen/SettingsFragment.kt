package com.example.easyaichat.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easyaichat.R
import com.example.easyaichat.data.database.entities.APIKeyEntity
import com.example.easyaichat.data.database.entities.ModelEntity
import com.example.easyaichat.data.database.repository.ChatRepository
import com.example.easyaichat.databinding.FragmentSettingsBinding
import com.example.easyaichat.ui.adapter.SettingsAdapter
import com.example.easyaichat.ui.viewmodel.SettingsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsFragment : Fragment(), KoinComponent {

    private val chatRepository: ChatRepository by inject()

    /*
    The hardcoding horseshit.
     */
    private val providers = listOf("OpenAI")
    private var modelsByProvider:List<String> = emptyList()

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModel()

    private val settingsAdapter = SettingsAdapter(
        onEditClick = { apiKey -> showEditDialog(apiKey) },
        onDeleteClick = { apiKey -> showDeleteConfirmation(apiKey) },
        onSetDefaultClick = { apiKey -> setDefaultKey(apiKey) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        viewLifecycleOwner.lifecycleScope.launch{
            chatRepository.getModelEntitiesByProvider("OpenAI").collect {
                modelList -> modelsByProvider = modelList.map{it.model}
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.apikeysRecyclerview.apply {
            adapter = settingsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners() {
        binding.addApiKeyFab.setOnClickListener {
            showAddDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.defaultKey.collect { defaultKey ->
                if (defaultKey != null) {
                    settingsAdapter.updateDefaultKey(defaultKey)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.apiKeys.collect { apiKeys ->
                settingsAdapter.updateApiKeys(apiKeys)
            }
        }
    }

    private fun showAddDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_api_key, null)

            // Setup provider dropdown
            val providerInput = dialogView.findViewById<AutoCompleteTextView>(R.id.provider_input)
            val providerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, providers
            )
            providerInput.setAdapter(providerAdapter)

            // Setup model dropdown (initially empty)
            val modelInput = dialogView.findViewById<AutoCompleteTextView>(R.id.model_input)
            var models: List<String> = emptyList()
            viewLifecycleOwner.lifecycleScope.launch{
            viewModel.models.collect{
                    list -> models = list
            }}
            var modelAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                models
            )
            modelInput.setAdapter(modelAdapter)

            // Update model options when provider changes
            providerInput.setOnItemClickListener { _, _, position, _ ->
                val selectedProvider = providers[position]
                val models = modelsByProvider
                modelAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    models
                )
                modelInput.setAdapter(modelAdapter)
                modelInput.text.clear()
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add API Key")
                .setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val apiKeyInput = dialogView.findViewById<TextInputEditText>(R.id.api_key_input)
                    val apiKeyNickname =
                        dialogView.findViewById<TextInputEditText>(R.id.api_key_nickname_input)
                    val apiKey = apiKeyInput.text.toString()
                    val nickname = apiKeyNickname.text.toString()
                    val provider = providerInput.text.toString()
                    val model = modelInput.text.toString()

                    viewLifecycleOwner.lifecycleScope.launch {
                        if (apiKey.isNotBlank() && provider.isNotBlank() && model.isNotBlank()) {
                            viewModel.addAPIKey(nickname, apiKey, provider, model)
                            //observeViewModel()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setDefaultKey(apiKey: APIKeyEntity) {
        Toast.makeText(context, "Default APIKey for new chats set!", Toast.LENGTH_SHORT).show()
        viewModel.setDefaultAPIKey(apiKey)
        //observeViewModel()
    }

    private fun showEditDialog(apiKey: APIKeyEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_api_key, null)

            // Setup existing values
            dialogView.findViewById<TextInputEditText>(R.id.api_key_input).setText(apiKey.apiKey)
            dialogView.findViewById<TextInputEditText>(R.id.api_key_nickname_input).setText(apiKey.nickname)
            val providerInput = dialogView.findViewById<AutoCompleteTextView>(R.id.provider_input)
            val providerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, providers
            )
            providerInput.setAdapter(providerAdapter)
            providerInput.setText(apiKey.provider, false)

            val modelInput = dialogView.findViewById<AutoCompleteTextView>(R.id.model_input)
            val models = modelsByProvider
            val modelAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                models
            )
            modelInput.setAdapter(modelAdapter)
            modelInput.setText(apiKey.defaultModel, false)

            // Update model options when provider changes
            providerInput.setOnItemClickListener { _, _, position, _ ->
                val selectedProvider = providers[position]
                val newModels = modelsByProvider
                val newModelAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line, newModels
                )
                modelInput.setAdapter(newModelAdapter)
                modelInput.text.clear()
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit API Key")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val newApiKey =
                        dialogView.findViewById<TextInputEditText>(R.id.api_key_input).text.toString()
                    val nickname =
                        dialogView.findViewById<TextInputEditText>(R.id.api_key_nickname_input).text.toString()
                    val newProvider = providerInput.text.toString()
                    val newModel = modelInput.text.toString()


                    if (newApiKey.isNotBlank() && newProvider.isNotBlank() && newModel.isNotBlank()) {
                        viewModel.updateAPIKey(
                            apiKey.copy(
                                apiKey = newApiKey,
                                nickname = nickname,
                                provider = newProvider,
                                defaultModel = newModel
                            )
                        )

                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showDeleteConfirmation(apiKey: APIKeyEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete API Key")
            .setMessage("Are you sure you want to delete this API key?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteDefaultAPIKey(apiKey)
                viewModel.deleteAPIKey(apiKey)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}