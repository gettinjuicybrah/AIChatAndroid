package com.example.easyaichat.ui.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.easyaichat.R
import com.example.easyaichat.data.database.entities.ChatEntity
import com.example.easyaichat.data.model.ChatMessage
import com.example.easyaichat.databinding.ItemMessageAiBinding
import com.example.easyaichat.databinding.ItemMessageUserBinding
import com.google.android.flexbox.FlexboxLayout

/*
The chat adapter's purpose is to bind data to ViewHolders (items) within our RecyclerView.
The data is the List<ChatMessage>.

Currently, this is called from ChatFragment. Anytime List<ChatMessage> is observed to have changed from the ViewModel,
a new Adapter is instantiated to reflect the changes.

FROM ChatGPT:
How RecyclerView Works
getItemViewType() is Called First:

RecyclerView uses getItemViewType(position) to determine which type of ViewHolder to create for a given position.
This view type is then passed to onCreateViewHolder() so that the appropriate ViewHolder can be instantiated.
onCreateViewHolder() Creates the ViewHolder:

Once the view type is known, onCreateViewHolder() is called with the specific viewType to create the correct ViewHolder.
onBindViewHolder() Binds Data:

After the appropriate ViewHolder is created (or recycled), onBindViewHolder() is called to bind the data to the ViewHolder for the given position.
 */
class ChatAdapter(
    private var messages:List<ChatMessage>,
    private val imageClickListener: ImageClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    init{

        Log.d("ChatAdapter", "Adapter created with ${messages.size} messages")
    }
    fun updateMessages(newMessages: List<ChatMessage>) {
        val diffCallback = ChatMessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Update the list with the new messages
        messages = newMessages

        // Apply the diff result to the adapter
        diffResult.dispatchUpdatesTo(this)
    }
    //Used to determine whether or not the ChatMessage element should be bound to the UserMessage or AIMessage ViewHolder.
    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
    }
    /*
    Generic return type - Since either considered ViewHolder item extends RecyclerView.ViewHolder, this works.

    viewType will already be determined via 'getItemViewType()' before this function is called.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("ChatAdapter", "Creating ViewHolder for type: $viewType")
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemMessageUserBinding.inflate(inflater, parent, false)
                return UserMessageViewHolder(binding, imageClickListener)
            }
            VIEW_TYPE_AI -> {
                val binding = ItemMessageAiBinding.inflate(inflater, parent, false)
                return AIMessageViewHolder(binding, imageClickListener)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("ChatAdapter", "Binding position: $position")
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AIMessageViewHolder -> holder.bind(message)
        }
    }

    //Fortunately, getItemViewType is determined before OnCreateViewHolder is ran.
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun getItemCount() = messages.size

}

class UserMessageViewHolder(val binding: ItemMessageUserBinding, val imageClickListener: ImageClickListener) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: ChatMessage) {
        if (!message.content.isNullOrEmpty()) {
            binding.messageText.text = message.content
            binding.messageText.visibility = View.VISIBLE
        } else {
            binding.messageText.visibility = View.GONE
        }
        // Reuse existing ImageViews
        binding.imagesContainer.removeAllViews()
        message.images.forEach { uri ->
            val imageView = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_image, binding.imagesContainer, false) as ImageView
            imageView.load(Uri.parse(uri)) {
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.error_background)
            }
            imageView.setOnClickListener {
                imageClickListener.onImageClick(uri)
            }
            binding.imagesContainer.addView(imageView)
        }
    }
}

class AIMessageViewHolder(val binding: ItemMessageAiBinding, val imageClickListener: ImageClickListener) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: ChatMessage) {
        if (!message.content.isNullOrEmpty()) {
            binding.messageText.text = message.content
            binding.messageText.visibility = View.VISIBLE
        } else {
            binding.messageText.visibility = View.GONE
        }
        // Reuse existing ImageViews
        binding.imagesContainer.removeAllViews()
        message.images.forEach { uri ->
            val imageView = LayoutInflater.from(itemView.context)
                .inflate( R.layout.item_image, binding.imagesContainer, false) as ImageView
            imageView.load(Uri.parse(uri)) {
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.error_background)
            }
            imageView.setOnClickListener {
                imageClickListener.onImageClick(uri)
            }
            binding.imagesContainer.addView(imageView)
        }
    }
}
class ChatMessageDiffCallback(
    private val oldList: List<ChatMessage>,
    private val newList: List<ChatMessage>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    // Determine if two items represent the same message.
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].messageEntity.id == newList[newItemPosition].messageEntity.id
    }

    // Determine if the contents of two items are the same.
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldList[oldItemPosition]
        val newMessage = newList[newItemPosition]
        return oldMessage == newMessage
    }
}