package com.harold.azureaadmin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream

/**
 * Compresses an image and returns a Uri to the compressed file in cache
 * without saving it to the device's gallery
 */
fun compressImage(context: Context, uri: Uri, maxSizeKB: Int = 400): Uri {
    try {
        // Load the original bitmap with fallback strategy
        val sourceBitmap = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Try ImageDecoder first
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            android.util.Log.w("ImageCompression", "ImageDecoder failed, trying fallback method", e)
            // Fallback: Try opening as input stream
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: throw IllegalStateException("Cannot decode image from URI")
        }

        // Validate bitmap loaded successfully
        if (sourceBitmap.width == 0 || sourceBitmap.height == 0) {
            throw IllegalStateException("Invalid image dimensions: ${sourceBitmap.width}x${sourceBitmap.height}")
        }

        android.util.Log.d("ImageCompression", "Loaded bitmap: ${sourceBitmap.width}x${sourceBitmap.height}")

        // Calculate scaled dimensions if image is too large
        val maxDimension = 1600
        val scaledBitmap = if (sourceBitmap.width > maxDimension || sourceBitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(sourceBitmap.width, sourceBitmap.height)
            val newWidth = (sourceBitmap.width * scale).toInt()
            val newHeight = (sourceBitmap.height * scale).toInt()
            android.util.Log.d("ImageCompression", "Scaling to: ${newWidth}x${newHeight}")
            Bitmap.createScaledBitmap(sourceBitmap, newWidth, newHeight, true)
        } else {
            sourceBitmap
        }

        // Compress with adaptive quality
        var quality = 85
        var compressedBytes: ByteArray
        var attempts = 0
        val maxAttempts = 8

        do {
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
            attempts++
            if (compressedBytes.size > maxSizeKB * 1024 && quality > 15) {
                quality -= 10
                android.util.Log.d("ImageCompression", "Attempt $attempts: ${compressedBytes.size / 1024}KB at quality $quality%")
            }
        } while (compressedBytes.size > maxSizeKB * 1024 && quality > 15 && attempts < maxAttempts)

        android.util.Log.d("ImageCompression", "Final size: ${compressedBytes.size / 1024}KB at quality $quality%")

        // Ensure we got some data
        if (compressedBytes.isEmpty()) {
            throw IllegalStateException("Compression resulted in empty data")
        }

        // Save to cache directory (won't appear in gallery)
        val cacheDir = File(context.cacheDir, "compressed_images").apply {
            if (!exists()) {
                val created = mkdirs()
                if (!created) throw IllegalStateException("Failed to create cache directory")
            }
        }
        val compressedFile = File(cacheDir, "compressed_${System.currentTimeMillis()}_${(0..9999).random()}.jpg")

        FileOutputStream(compressedFile).use { fos ->
            fos.write(compressedBytes)
            fos.flush()
        }

        // Verify file was created
        if (!compressedFile.exists() || compressedFile.length() == 0L) {
            throw IllegalStateException("Failed to save compressed image")
        }

        // Clean up
        if (scaledBitmap != sourceBitmap) {
            scaledBitmap.recycle()
        }
        sourceBitmap.recycle()

        // Return Uri using FileProvider
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            compressedFile
        )
    } catch (e: Exception) {
        android.util.Log.e("ImageCompression", "Error compressing image from URI: $uri", e)
        throw Exception("Failed to compress image: ${e.message}", e)
    }
}

/**
 * Compresses multiple images and returns list of compressed URIs
 */
fun compressMultipleImages(context: Context, uris: List<Uri>, maxSizeKB: Int = 400): List<Uri> {
    return uris.map { compressImage(context, it, maxSizeKB) }
}

/**
 * Calculates total size of URIs in KB
 */
fun calculateTotalSizeKB(context: Context, uris: List<Uri>): Long {
    val totalSizeBytes = uris.sumOf { uri ->
        context.contentResolver.openInputStream(uri)?.use { it.available().toLong() } ?: 0L
    }
    return totalSizeBytes / 1024
}

/**
 * Clears old compressed images from cache
 */
fun clearCompressedImageCache(context: Context) {
    val cacheDir = File(context.cacheDir, "compressed_images")
    if (cacheDir.exists()) {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}

// Usage in your dialog:
// val compressedUris = compressMultipleImages(context, uris, maxSizePerImageKB = 500)
// val totalSizeKB = calculateTotalSizeKB(context, compressedUris)