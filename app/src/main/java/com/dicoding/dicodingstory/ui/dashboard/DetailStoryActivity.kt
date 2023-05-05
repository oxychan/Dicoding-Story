package com.dicoding.dicodingstory.ui.dashboard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.dicodingstory.data.models.StoryModel
import com.dicoding.dicodingstory.databinding.ActivityDetailStoryBinding
import com.dicoding.dicodingstory.utils.Utils

class DetailStoryActivity : AppCompatActivity() {

    private var _binding: ActivityDetailStoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val detailData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(MainActivity.EXTRA_STORY, StoryModel::class.java)
        } else {
            intent.getParcelableExtra(MainActivity.EXTRA_STORY)
        }

        if (detailData != null) {
            Glide.with(applicationContext)
                .load(detailData.photoUrl)
                .into(binding.ivUserStoryDetail)

            binding.apply {
                this.tvUserNameDetail.text = detailData.name
                this.tvUserPostedAtDetail.text =
                    Utils.dateTimeFormat(detailData.createdAt.toString())
                this.tvUserDescriptionDetail.text = detailData.description
            }

            playAnim()
        }
    }

    private fun playAnim() {
        val imgDetail =
            ObjectAnimator.ofFloat(binding.ivUserStoryDetail, View.ALPHA, 1f).setDuration(500)
        val postedByText =
            ObjectAnimator.ofFloat(binding.tvPostedBy, View.ALPHA, 1f).setDuration(500)
        val nameText =
            ObjectAnimator.ofFloat(binding.tvUserNameDetail, View.ALPHA, 1f).setDuration(500)
        val postedAtText =
            ObjectAnimator.ofFloat(binding.tvPostedAt, View.ALPHA, 1f).setDuration(500)
        val dateText =
            ObjectAnimator.ofFloat(binding.tvUserPostedAtDetail, View.ALPHA, 1f).setDuration(500)
        val descText =
            ObjectAnimator.ofFloat(binding.tvDescription, View.ALPHA, 1f).setDuration(500)
        val descDetailText =
            ObjectAnimator.ofFloat(binding.tvUserDescriptionDetail, View.ALPHA, 1f).setDuration(500)

        val togetherPosted = AnimatorSet().apply {
            playTogether(postedByText, nameText)
        }

        val togetherAt = AnimatorSet().apply {
            playTogether(postedAtText, dateText)
        }

        val togetherDesc = AnimatorSet().apply {
            playTogether(descText, descDetailText)
        }

        AnimatorSet().apply {
            playSequentially(imgDetail, togetherPosted, togetherAt, togetherDesc)
            start()
        }
    }
}