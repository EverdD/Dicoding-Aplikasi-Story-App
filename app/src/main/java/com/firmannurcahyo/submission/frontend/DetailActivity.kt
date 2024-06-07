package com.firmannurcahyo.submission.frontend

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firmannurcahyo.submission.R
import com.firmannurcahyo.submission.database.datamodel.StoriesDatabase
import com.firmannurcahyo.submission.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity(), View.OnClickListener {

    private var _binding: ActivityDetailBinding? = null
    private val binding get() = _binding!!
    private var imageScaleZoom = true

    companion object {
        const val EXTRA_DATA = "extra_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.well_story)

        val story = intent.getParcelableExtra<StoriesDatabase>(EXTRA_DATA)
        setupView(story)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_detail_story -> handleImageDetailClick()
        }
    }

    private fun handleImageDetailClick() {
        imageScaleZoom = !imageScaleZoom
        binding.imgDetailStory.scaleType =
            if (imageScaleZoom) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupView(story: StoriesDatabase?) {
        with(binding) {
            Glide.with(imgDetailStory.context).load(story?.photoUrl).centerCrop()
                .apply(RequestOptions.placeholderOf(R.drawable.img_add_picture))
                .into(imgDetailStory)
            tvDetailUsername.text = story?.name
            tvDetailDescription.text = story?.description
            imgDetailStory.setOnClickListener(this@DetailActivity)
        }
    }
}
