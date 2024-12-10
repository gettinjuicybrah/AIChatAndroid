package com.example.easyaichat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Delete
import com.example.easyaichat.data.database.entities.ChatEntity
import com.example.easyaichat.databinding.ListItemChatBinding
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatHolder (
    val binding:ListItemChatBinding,
    private val onChatSelected: (ChatEntity) -> Unit,
    private val onChatDelete :(ChatEntity) -> Unit):
        RecyclerView.ViewHolder(binding.root){
            fun bind(chat: ChatEntity){
                    binding.chatTitle.text = chat.title
                    binding.root.setOnClickListener{
                        onChatSelected(chat)
                    }
                binding.deleteButton.setOnClickListener {
                    onChatDelete(chat)
                }
            }
        }

class ChatListAdapter (
        private var chat: List<ChatEntity>,
        private val onChatSelected: (ChatEntity) -> Unit,
        private val onChatDelete :(ChatEntity) -> Unit
) :RecyclerView.Adapter<ChatHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemChatBinding.inflate(inflater, parent, false)
        return ChatHolder(binding, onChatSelected, onChatDelete)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val chat = chat[position]
        holder.apply {
            bind(chat)
        }
    }

    override fun getItemCount(): Int {
        return chat.size
    }
}
