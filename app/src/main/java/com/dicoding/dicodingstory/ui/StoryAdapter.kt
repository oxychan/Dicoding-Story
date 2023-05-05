package com.dicoding.dicodingstory.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.dicodingstory.R
import com.dicoding.dicodingstory.data.models.StoryModel
import com.dicoding.dicodingstory.utils.Utils

class StoryAdapter(private val stories: MutableList<StoryModel>) :
    RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

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

    override fun getItemCount(): Int = stories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, photoUrl, _, createdAt) = stories[position]
        Glide.with(holder.itemView.context)
            .load(photoUrl)
            .into(holder.photo)
        holder.tvName.text = name
        holder.tvCreatedAt.text = Utils.dateTimeFormat(createdAt.toString())

        holder.itemView.setOnClickListener { onItemClickCallback.onItemClicked(stories[holder.adapterPosition]) }
    }
}