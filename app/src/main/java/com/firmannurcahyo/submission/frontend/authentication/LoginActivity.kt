package com.firmannurcahyo.submission.frontend.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.firmannurcahyo.submission.MainActivity
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.firmannurcahyo.submission.databinding.ActivityLoginBinding
import com.firmannurcahyo.submission.frontend.model.ViewModelFactory

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_key")
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginViewModel: LoginViewModel
    private var mShouldFinish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupViewModel()
        setupView()
    }

    override fun onStop() {
        super.onStop()
        if (mShouldFinish) finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSignUp -> startActivity(Intent(this, RegisterActivity::class.java))
            binding.btnLogin -> handleLogin()
        }
    }

    private fun handleLogin() {
        when {
            canLogin() -> {
                val email = binding.cvInputEmail.text.toString()
                val password = binding.cvInputPassword.text.toString()

                closeKeyboard(this)
                loginViewModel.login(email, password)
            }

            else -> {
                Toast.makeText(this, "Check Your Input", Toast.LENGTH_SHORT).show()
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

    private fun setupViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        loginViewModel = ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]

        loginViewModel.authInfo.observe(this) { result ->
            val isLoading = result is Resource.Loading
            showLoading(isLoading)

            val isSuccess = result is Resource.Success
            val isError = result is Resource.Error

            when {
                isSuccess -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    mShouldFinish = true
                }

                isError -> {
                    val errorMessage = (result as Resource.Error).message
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupView() {
        with(binding) {
            btnSignUp.setOnClickListener(this@LoginActivity)
            btnLogin.setOnClickListener(this@LoginActivity)
        }
    }

    private fun canLogin() =
        binding.cvInputEmail.error == null && binding.cvInputPassword.error == null && !binding.cvInputEmail.text.isNullOrEmpty() && !binding.cvInputPassword.text.isNullOrEmpty()

    private fun showLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}