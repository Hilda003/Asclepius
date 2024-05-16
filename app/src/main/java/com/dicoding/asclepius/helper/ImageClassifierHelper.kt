package com.dicoding.asclepius.helper

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier



class ImageClassifierHelper(
private val context: Context
) {
    private var imageClassifier: ImageClassifier? = null


    init {
        setupImageClassifier()
    }


    private fun setupImageClassifier() {
        // TODO: Menyiapkan Image Classifier untuk memproses gambar.

        imageClassifier = ImageClassifier.createFromFileAndOptions(
            context, "cancer_classification.tflite",
            ImageClassifier.ImageClassifierOptions.builder()
                .setMaxResults(3)
                .setScoreThreshold(0.5f)
                .build()
        )


    }

    fun classifyStaticImage(
        contentResolver: ContentResolver,
        imageUri: Uri
    ): List<Classifications> {
        // TODO: mengklasifikasikan imageUri dari gambar statis.

        try {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }
            val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val tensorImage = TensorImage.fromBitmap(argbBitmap)
            return imageClassifier?.classify(tensorImage) ?: emptyList()
        } catch (e: Exception) {
            Log.d("ImageClassifierHelper", "Error: ${e.message}")
            e.printStackTrace()
        }
        return emptyList()




    }

}