package com.example.easyaichat.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easyaichat.databinding.FragmentChatListBinding
import com.example.easyaichat.ui.adapter.ChatListAdapter
import com.example.easyaichat.ui.viewmodel.ChatListViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.uuid.ExperimentalUuidApi
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ChatListFragment: Fragment() {
    private var _binding: FragmentChatListBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Binding is null."
        }
    private val chatListViewModel: ChatListViewModel by viewModel()

    private val navController by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)
        val chat = chatListViewModel.chat
        // Add the BorderItemDecoration
        val borderWidthInPx = resources.displayMetrics.density * 1 // 2dp to pixels
        binding.chatRecyclerView.addItemDecoration(
            BorderItemDecoration(
                borderColor = Color.GRAY, // Choose your desired color
                borderWidth = borderWidthInPx
            )
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            binding.newChatFab.setOnClickListener{
                findNavController().navigate(
                    ChatListFragmentDirections.actionNavigationChatListToNavigationHome()
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatListViewModel.chat.collect { chats ->
                    binding.chatRecyclerView.adapter = ChatListAdapter(
                        chats,
                        onChatSelected = { chat ->
                            findNavController().navigate(
                                ChatListFragmentDirections.actionNavigationChatListToNavigationHome(chat.id)
                            )
                        },
                        onChatDelete = { chat ->
                            chatListViewModel.deleteChat(chat)
                        }
                    )
                }

            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
class BorderItemDecoration(
    private val borderColor: Int,
    private val borderWidth: Float
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        color = borderColor
        strokeWidth = borderWidth
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount

        for (i in 0 until childCount) {
            val child: View = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val left = child.left - params.leftMargin
            val top = child.top - params.topMargin
            val right = child.right + params.rightMargin
            val bottom = child.bottom + params.bottomMargin

            // Draw rectangle around the child view
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }

    // Optionally, add padding if needed
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(borderWidth.toInt(), borderWidth.toInt(), borderWidth.toInt(), borderWidth.toInt())
    }
}