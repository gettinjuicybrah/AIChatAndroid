package com.example.easyaichat.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.easyaichat.R
import com.example.easyaichat.data.database.entities.APIKeyEntity
import com.example.easyaichat.data.database.entities.DefaultAPIKeyEntity
import com.example.easyaichat.data.database.repository.ChatRepository
import com.google.android.material.button.MaterialButton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsAdapter(
    private val onEditClick: (APIKeyEntity) -> Unit,
    private val onDeleteClick: (APIKeyEntity) -> Unit,
    private val onSetDefaultClick: (APIKeyEntity) -> Unit,
) : RecyclerView.Adapter<SettingsAdapter.APIKeyViewHolder>(), KoinComponent {

    var apiKeys: List<APIKeyEntity> = emptyList()
    var defaultKey: DefaultAPIKeyEntity? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun updateApiKeys(newList: List<APIKeyEntity>) {
        val diffCallback = SettingsKeyDiffCallback(apiKeys, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Update the list
        apiKeys = newList

        // Apply the diff result to update the RecyclerView efficiently
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateDefaultKey(newKey: DefaultAPIKeyEntity) {

        defaultKey = newKey

        // Force a full refresh when the default key changes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): APIKeyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_api_key, parent, false)
        return APIKeyViewHolder(view)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return apiKeys.size
    }

    override fun onBindViewHolder(holder: APIKeyViewHolder, position: Int) {
        val apiKey = apiKeys[position]
        holder.bind(apiKey, defaultKey)
    }



    inner class APIKeyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val apikeyIsDefault: TextView = itemView.findViewById(R.id.apikeyIsDefault_text)
        private val apiKeyNickname: TextView = itemView.findViewById(R.id.apikeyNickname_text)
        private val providerText: TextView = itemView.findViewById(R.id.provider_text)
        private val modelText: TextView = itemView.findViewById(R.id.model_text)
        private val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
        private val setDefaultButton: MaterialButton =
            itemView.findViewById(R.id.set_default_button)

        fun bind(apiKey: APIKeyEntity, defaultKey: DefaultAPIKeyEntity?) {
            if (apiKey.id == defaultKey?.keyId) {
                apikeyIsDefault.text = "IS DEFAULT API KEY"
                setDefaultButton.visibility = View.GONE  // Hide the button for current default
            } else {
                apikeyIsDefault.text = ""
                setDefaultButton.visibility = View.VISIBLE
            }

            apiKeyNickname.text = apiKey.nickname
            providerText.text = apiKey.provider
            modelText.text = apiKey.defaultModel

            editButton.setOnClickListener { onEditClick(apiKey) }
            deleteButton.setOnClickListener { onDeleteClick(apiKey) }
            setDefaultButton.setOnClickListener { onSetDefaultClick(apiKey) }

        }

    }
}

class SettingsKeyDiffCallback(
    private val oldList: List<APIKeyEntity>,
    private val newList: List<APIKeyEntity>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    /**
     * Called by the DiffUtil to decide whether two object represent the same Item.
     *
     *
     * For example, if your items have unique ids, this method should check their id equality.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     * @return True if the two items represent the same object or false if they are different.
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    /**
     * Called by the DiffUtil when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item has changed.
     *
     *
     * DiffUtil uses this method to check equality instead of [Object.equals]
     * so that you can change its behavior depending on your UI.
     * For example, if you are using DiffUtil with a
     * [RecyclerView.Adapter], you should
     * return whether the items' visual representations are the same.
     *
     *
     * This method is called only if [.areItemsTheSame] returns
     * `true` for these items.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list which replaces the
     * oldItem
     * @return True if the contents of the items are the same or false if they are different.
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }


}

class DefaultKeyDiffCallback(
    private val oldKey: DefaultAPIKeyEntity?,
    private val newKey: DefaultAPIKeyEntity?
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = if (oldKey == null) 0 else 1

    override fun getNewListSize(): Int = if (newKey == null) 0 else 1

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compare IDs or object references
        return oldKey?.keyId == newKey?.keyId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compare full content
        return oldKey == newKey
    }
}