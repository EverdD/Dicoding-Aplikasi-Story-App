package com.firmannurcahyo.submission.frontend.model

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.firmannurcahyo.submission.R
import com.firmannurcahyo.submission.databinding.ActivityCameraBinding
import com.firmannurcahyo.submission.frontend.model.createFile
import com.firmannurcahyo.submission.frontend.StoriesActivity

class CameraActivity : AppCompatActivity(), View.OnClickListener {

    private var _binding: ActivityCameraBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setupView()
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_capture_camera -> takePhoto()
            R.id.img_flip_camera -> flipCamera()
            R.id.img_back -> onBackPressed()
        }
    }

    private fun setupView() {
        with(binding) {
            imgCaptureCamera.setOnClickListener(this@CameraActivity)
            imgFlipCamera.setOnClickListener(this@CameraActivity)
            imgBack.setOnClickListener(this@CameraActivity)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.pvScreenAndroid.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    resources.getText(R.string.failed_show_camera),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        exc.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val intent = Intent().apply {
                        putExtra(resources.getString(R.string.gallery), photoFile)
                        putExtra(
                            resources.getString(R.string.back_camera),
                            cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
                        )
                    }

                    setResult(StoriesActivity.CAMERA_X_RESULT, intent)
                    finish()
                }
            }
        )
    }

    private fun flipCamera() {
        val newCameraSelector = when (cameraSelector) {
            CameraSelector.DEFAULT_BACK_CAMERA -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraSelector.DEFAULT_FRONT_CAMERA -> CameraSelector.DEFAULT_BACK_CAMERA
            else -> CameraSelector.DEFAULT_BACK_CAMERA // Fallback to back camera
        }

        if (newCameraSelector != cameraSelector) {
            cameraSelector = newCameraSelector
            startCamera()
        }
    }
}