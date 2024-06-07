package com.firmannurcahyo.submission.frontend.authentication

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.firmannurcahyo.submission.R
import com.firmannurcahyo.submission.databinding.ActivityRegisterBinding
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.firmannurcahyo.submission.frontend.model.ViewModelFactory

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_key")
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupViewModel()
        setupView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        loginViewModel = ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]

        loginViewModel.authInfo.observe(this) {
            when (it) {
                is Resource.Success -> handleSuccess(it.data)
                is Resource.Loading -> showLoading(true)
                is Resource.Error -> handleError(it.message)
            }
        }
    }

    private fun handleSuccess(message: String?) {
        message?.let {
            showLoading(false)
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun handleError(errorMessage: String?) {
        Toast.makeText(this, errorMessage ?: "Unknown Error", Toast.LENGTH_SHORT).show()
        showLoading(false)
    }

    private fun setupView() {
        with(binding) {
            btnRegister.setOnClickListener(this@RegisterActivity)
            imgBack.setOnClickListener(this@RegisterActivity)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back -> finish()
            R.id.btn_register -> {
                val name = binding.cvUsername.text.toString()
                val email = binding.cvInputEmail.text.toString()
                val password = binding.cvInputPassword.text.toString()

                when {
                    binding.cvInputEmail.error == null && binding.cvInputPassword.error == null -> {
                        closeKeyboard(this)
                        loginViewModel.register(name, email, password)
                    }
                    else -> Toast.makeText(this, "Check Your Input", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun closeKeyboard(activity: AppCompatActivity) {
        val view: View? = activity.currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}