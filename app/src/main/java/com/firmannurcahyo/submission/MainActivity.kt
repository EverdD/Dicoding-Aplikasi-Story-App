package com.firmannurcahyo.submission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.firmannurcahyo.submission.database.datamodel.MapActivity
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.firmannurcahyo.submission.database.datapaging.StoryAdapter
import com.firmannurcahyo.submission.database.datapaging.StoryFactory
import com.firmannurcahyo.submission.databinding.ActivityMainBinding
import com.firmannurcahyo.submission.frontend.MainViewModel
import com.firmannurcahyo.submission.frontend.StoriesActivity
import com.firmannurcahyo.submission.frontend.authentication.LoginActivity
import com.firmannurcahyo.submission.frontend.authentication.LoginViewModel
import com.firmannurcahyo.submission.frontend.model.LoadingAdapter
import com.firmannurcahyo.submission.frontend.model.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_key")
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val storyAdapter = StoryAdapter()
    private lateinit var mainViewModel: MainViewModel
    private lateinit var loginViewModel: LoginViewModel

    companion object {
        const val RV_COLOMN_COUNT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = resources.getString(R.string.home)
        setupViewModel()
        setupView()
        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(500)
            refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                loginViewModel.logout()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finishAffinity()
                true
            }

            R.id.maps -> {
                startActivity(Intent(this, MapActivity::class.java))
                true
            }

            else -> false
        }
    }

    private fun refresh() {
        binding.swipeRefresh.isRefreshing = true
        storyAdapter.refresh()
        Timer().schedule(1000) {
            binding.swipeRefresh.isRefreshing = false
            binding.rvStory.smoothScrollToPosition(0)
        }
    }


    private fun setupViewModel() {
        val adapter = StoryAdapter()
        val pref = UserPreferences.getInstance(dataStore)
        val viewModelFactory = ViewModelFactory(pref)
        viewModelFactory.setApplication(application)

        loginViewModel = ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]
        mainViewModel = ViewModelProvider(this, StoryFactory(this))[MainViewModel::class.java]

        binding.rvStory.adapter = adapter.withLoadStateFooter(footer = LoadingAdapter {
            adapter.retry()
        })

        mainViewModel.getStories().observe(this) {
            storyAdapter.submitData(lifecycle, it)
        }

        fetchData()
    }

    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.getStories()
        }
    }

    private fun setupView() {
        setupRecylerView()
        fab()
    }

    private fun fab() {
        binding.fabUpload.setOnClickListener {
            val addIntent = Intent(this, StoriesActivity::class.java)
            startActivity(addIntent)
        }
    }

    private fun setupRecylerView() {
        with(binding.rvStory) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@MainActivity, RV_COLOMN_COUNT)
            adapter = storyAdapter
        }
    }
}