package com.dicoding.dicodingstory.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.dicodingstory.R
import com.dicoding.dicodingstory.data.models.StoryModel
import com.dicoding.dicodingstory.data.response.Story
import com.dicoding.dicodingstory.utils.Utils

class StoryAdapterWithPaging() :
    PagingDataAdapter<Story, StoryAdapterWithPaging.ViewHolder>(DIFF_CALLBACK) {

    private lateinit var onItemClickCallback: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClicked(data: StoryModel)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.iv_user_story)
        val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.tv_user_posted_at)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        val storyModel = StoryModel(
            name = data?.name ?: "",
            photoUrl = data?.photoUrl ?: "",
            description = data?.description ?: "",
            createdAt = data?.createdAt
        )
        Glide.with(holder.itemView.context)
            .load(data?.photoUrl)
            .into(holder.photo)
        holder.tvName.text = data?.name
        holder.tvCreatedAt.text = Utils.dateTimeFormat(data?.createdAt.toString())

        holder.itemView.setOnClickListener { onItemClickCallback.onItemClicked(storyModel) }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }
}