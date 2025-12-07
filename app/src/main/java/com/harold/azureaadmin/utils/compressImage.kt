package com.harold.azureaadmin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import androidx.core.net.toUri

fun compressImage(context: Context, uri: Uri, quality: Int = 50): Uri {
    val sourceBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
    } else {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    val outputStream = ByteArrayOutputStream()
    sourceBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

    val bytes = outputStream.toByteArray()
    val compressedUri = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size),
        "compressed_image",
        null
    ).toUri()

    return compressedUri
}
