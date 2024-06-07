package com.firmannurcahyo.submission.database.datapaging

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firmannurcahyo.submission.R
import com.firmannurcahyo.submission.database.datamodel.StoriesDatabase
import com.firmannurcahyo.submission.databinding.ItemStoryBinding
import com.firmannurcahyo.submission.frontend.DetailActivity

class StoryAdapter :
    PagingDataAdapter<StoriesDatabase, StoryAdapter.MainViewHolder>(DIFF_CALLBACK) {

    inner class MainViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val tvDetailDescription = binding.tvDetailDescription  // Inisialisasi tvDetailDescription di sini

        fun bind(story: StoriesDatabase) = with(binding) {
            Glide.with(imgStory)
                .load(story.photoUrl)
                .centerCrop()
                .apply(RequestOptions.placeholderOf(R.drawable.img_add_picture))
                .into(imgStory)
            tvUsername.text = story.name
            tvDetailDescription.text = story.description  // Atur nilai tvDetailDescription di sini
            root.setOnClickListener {
                val context = root.context
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        context as Activity,
                        Pair(imgStory, "imageDetail"),
                        Pair(tvUsername, "nameDetail"),
                        Pair(tvDetailDescription, "descriptionDetail")
                    )
                val detailIntent = Intent(context, DetailActivity::class.java)
                detailIntent.putExtra(DetailActivity.EXTRA_DATA, story)
                context.startActivity(detailIntent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder =
        MainViewHolder(
            ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoriesDatabase>() {
            override fun areItemsTheSame(
                oldItem: StoriesDatabase,
                newItem: StoriesDatabase
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: StoriesDatabase,
                newItem: StoriesDatabase
            ): Boolean =
                oldItem.id == newItem.id
        }
    }
}