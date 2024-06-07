package com.firmannurcahyo.submission.frontend.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.firmannurcahyo.submission.databinding.ItemLoadingBinding

class LoadingAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingAdapter.LoadingStateViewHolder>() {

    class LoadingStateViewHolder(
        private val binding: ItemLoadingBinding, private val retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnTryAgain.setOnClickListener {
                retry.invoke()
            }
        }

        fun bind(loadState: LoadState) {
            when (loadState) {
                is LoadState.Error -> {
                    binding.tvMessageError.text = loadState.error.localizedMessage
                    binding.pbLoading.isVisible = false
                    binding.btnTryAgain.isVisible = true
                    binding.tvMessageError.isVisible = true
                }

                is LoadState.Loading -> {
                    binding.pbLoading.isVisible = true
                    binding.btnTryAgain.isVisible = false
                    binding.tvMessageError.isVisible = false
                }

                else -> {
                    binding.pbLoading.isVisible = false
                    binding.btnTryAgain.isVisible = false
                    binding.tvMessageError.isVisible = false
                }
            }
        }
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, loadState: LoadState
    ): LoadingStateViewHolder {
        val binding = ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateViewHolder(binding, retry)
    }
}
