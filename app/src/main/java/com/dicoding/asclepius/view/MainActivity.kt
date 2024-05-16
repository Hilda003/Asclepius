package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import java.io.File



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permission request granted")
            } else {
                showToast("Permission request denied")
            }
        }

    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private fun allPermissionsGranted() =
        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        imageClassifierHelper = ImageClassifierHelper(context = this)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let { analyzeImage() } ?: showToast("No image selected")
            moveToResult()
        }
    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.

        launchGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    }

    private val launchGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
       if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            showToast("Image selection was cancelled")
        }
    }


    private fun showImage() {
        // TODO: Menampilkan gambar sesuai Gallery yang dipilih.
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage() {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.
        currentImageUri?.let {uri ->
            try {
                val classifications = imageClassifierHelper.classifyStaticImage(contentResolver, uri)
                val result = classifications.firstOrNull()?.categories?.firstOrNull()?.let {
                    val confidenceScore = (it.score * 100).toInt()
                    "${it.label} (${confidenceScore}%)"
                } ?: "No result"
                showToast(result)
            } catch (e: Exception) {
                showToast("Error Analyze Image: ${e.message}")
                e.printStackTrace()
            }

        } ?: showToast("No image selected")

    }

    private fun moveToResult() {
        currentImageUri?.let {
            try {
                val classifications = imageClassifierHelper.classifyStaticImage(contentResolver, it)
                val result = classifications.firstOrNull()?.categories?.firstOrNull()?.let {
                    val confidenceScore = (it.score * 100).toInt()
                    "${it.label} (${confidenceScore}%)"
                } ?: "No result"
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
                    putExtra(ResultActivity.EXTRA_RESULT, result)
                }
                startActivity(intent)
            } catch (e: Exception) {
                showToast("Error Analyze Image: ${e.message}")
                e.printStackTrace()
            }

        }?: showToast("No image selected")


    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}