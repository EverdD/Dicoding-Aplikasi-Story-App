package com.firmannurcahyo.submission.frontend

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.firmannurcahyo.submission.R
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.firmannurcahyo.submission.databinding.ActivityStoriesBinding
import com.firmannurcahyo.submission.frontend.authentication.Resource
import com.firmannurcahyo.submission.frontend.model.CameraActivity
import com.firmannurcahyo.submission.frontend.model.ViewModelFactory
import com.firmannurcahyo.submission.frontend.model.reduceFileImage
import com.firmannurcahyo.submission.frontend.model.rotateBitmap
import com.firmannurcahyo.submission.frontend.model.uriToFile
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoriesActivity : AppCompatActivity(), View.OnClickListener {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_key")
    private var _binding: ActivityStoriesBinding? = null
    private val binding get() = _binding!!
    private var imgScaleZoom = true
    private var getFile: File? = null
    private lateinit var storiesViewModel: StoriesViewModel

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSION = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.story_add)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION
            )
        }
        setupViewModel()
        setupView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnCamera -> startCameraX()
            binding.btnGallery -> startGallery()
            binding.btnUpload -> uploadImage(asGuest = false)
            binding.imgStoryAdd -> {
                imgScaleZoom = !imgScaleZoom
                binding.imgStoryAdd.scaleType = when (imgScaleZoom) {
                    true -> ImageView.ScaleType.CENTER_CROP
                    false -> ImageView.ScaleType.FIT_CENTER
                }
            }
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this, resources.getString(R.string.not_permission), Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun uploadImage(asGuest: Boolean) {
        when (val file = getFile) {
            null -> {
                Toast.makeText(
                    this, resources.getString(R.string.not_permission), Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val reducedFile = reduceFileImage(file)
                val description = binding.etDescriptionAdd.text.toString()
                    .toRequestBody(resources.getString(R.string.text_plain).toMediaType())
                val requestImageFile = reducedFile.asRequestBody(
                    resources.getString(R.string.image_jpeg).toMediaTypeOrNull()
                )
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    resources.getString(R.string.photo), reducedFile.name, requestImageFile
                )

                showLoading(true)
                CoroutineScope(Dispatchers.IO).launch {
                    storiesViewModel.upload(imageMultipart, description, asGuest)
                }
            }
        }
    }

    private fun setupView() {
        with(binding) {
            btnCamera.setOnClickListener(this@StoriesActivity)
            btnGallery.setOnClickListener(this@StoriesActivity)
            btnUpload.setOnClickListener(this@StoriesActivity)
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = resources.getString(R.string.image_all)
        val chooser = Intent.createChooser(intent, resources.getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private fun setupViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        storiesViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[StoriesViewModel::class.java]

        storiesViewModel.uploadInfo.observe(this) {
            when (it) {
                is Resource.Success -> {
                    val message = it.data
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    setResultAndFinish(true)
                }
                is Resource.Loading -> showLoading(true)
                is Resource.Error -> {
                    val message = it.message
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            }
        }
    }

    private fun setResultAndFinish(refresh: Boolean) {
        val resultIntent = Intent()
        resultIntent.putExtra("refresh", refresh)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when {
            result.resultCode == CAMERA_X_RESULT -> {
                val myFile =
                    result.data?.getSerializableExtra(resources.getString(R.string.gallery)) as File
                val isBackCamera = result.data?.getBooleanExtra(
                    resources.getString(R.string.back_camera), true
                ) as Boolean
                val resultBitmap = rotateBitmap(BitmapFactory.decodeFile(myFile.path), isBackCamera)
                binding.imgStoryAdd.setImageBitmap(resultBitmap)
                getFile = myFile
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when {
            result.resultCode == RESULT_OK -> {
                val selectedImg: Uri = result.data?.data as Uri
                val myFile = uriToFile(selectedImg, this@StoriesActivity)
                binding.imgStoryAdd.setImageURI(selectedImg)
                getFile = myFile
            }
        }
    }
}