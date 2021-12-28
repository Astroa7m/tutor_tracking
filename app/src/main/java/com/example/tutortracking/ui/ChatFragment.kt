package com.example.tutortracking.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tutortracking.adapters.ChatAdapter
import com.example.tutortracking.databinding.FragmentChatBinding
import com.example.tutortracking.util.ChatBubble
import com.example.tutortracking.viewmodels.TutorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    val viewModel by viewModels<TutorViewModel>()

    override fun onStart() {
        super.onStart()
        viewModel.connectToChat()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root
        try{ (activity as MainActivity).hasSessionStarted = false }catch (e: Exception){ }
        initAdapter()
        getAllMessages()
        onSendClicked()
        chatAdapter.setOnNameClickListener { }

        return view
    }

    private fun onSendClicked() {
        binding.chatSendButton.setOnClickListener {
            viewModel.sendMessage(binding.chatMessageEt.text.toString())
            binding.chatMessageEt.setText("")
        }
    }

    private fun getAllMessages() = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
            viewModel.messagesState.collect { result ->
                if(result.contains(ChatBubble.Loading)){
                    showProgressbar()
                }else {
                    hideProgressbar()
                    chatAdapter.submitList(result.toMutableList()) {
                        if (result.isNotEmpty())
                            binding.chatRv.smoothScrollToPosition(viewModel.lastIndex)
                    }
                }
            }
        }
    }

    private fun hideProgressbar() {
        binding.chatProgressbar.isVisible = false
    }

    private fun showProgressbar() {
        binding.chatProgressbar.isVisible = true
    }

    private fun initAdapter() {
        chatAdapter = ChatAdapter()
        binding.chatRv.adapter = chatAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}