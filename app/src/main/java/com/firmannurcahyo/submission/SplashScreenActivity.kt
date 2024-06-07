package com.firmannurcahyo.submission

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.firmannurcahyo.submission.MainActivity
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.firmannurcahyo.submission.databinding.ActivitySplashScreenBinding
import com.firmannurcahyo.submission.frontend.authentication.LoginActivity
import com.firmannurcahyo.submission.frontend.authentication.LoginViewModel
import com.firmannurcahyo.submission.frontend.model.ViewModelFactory

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_key")
    private var _binding: ActivitySplashScreenBinding? = null
    private val binding get() = _binding!!
    private var mShouldFinish = false
    private lateinit var loginViewModel: LoginViewModel

    companion object {
        const val DELAY = 3000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupViewModel()

        Handler(Looper.getMainLooper()).postDelayed({
            val optionsCompat: ActivityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this, binding.imgSplashScreen, "logoLogin"
                )

            loginViewModel.getUserKey().observe(this) {
                if (it.isNullOrEmpty()) {
                    startActivity(
                        Intent(this@SplashScreenActivity, LoginActivity::class.java),
                        optionsCompat.toBundle()
                    )
                } else {
                    startActivity(
                        Intent(this@SplashScreenActivity, MainActivity::class.java),
                        optionsCompat.toBundle()
                    )
                }
            }

            mShouldFinish = true
        }, DELAY)
    }

    override fun onStop() {
        super.onStop()
        if (mShouldFinish) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        loginViewModel = ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]
    }


}