package com.dicoding.dicodingstory.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.dicodingstory.databinding.ItemLoadingBinding

class LoadingStateAdapter() : LoadStateAdapter<LoadingStateAdapter.LoadingStateViewHolder>() {

    class LoadingStateViewHolder(private val binding: ItemLoadingBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(loadState: LoadState) {
            binding.pbLoadItem.isVisible = loadState is LoadState.Loading
        }
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingStateViewHolder {
        val binding = ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateViewHolder(binding)
    }
}