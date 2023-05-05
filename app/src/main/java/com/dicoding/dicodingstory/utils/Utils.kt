package com.dicoding.dicodingstory.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever.BitmapParams
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.dicoding.dicodingstory.R
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Utils {
    private const val FILENAME_FORMAT = "dd-MMM-yyyy"

    val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private const val MAXIMUM_SIZE = 1000000

    @SuppressLint("SimpleDateFormat")
    fun dateTimeFormat(createdAt: String): String {
        val inputFormatterStr = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val outputFormatterStr = "EEEE, dd-MM-yyyy, HH:mm"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val inputFormatter = DateTimeFormatter.ofPattern(inputFormatterStr)
            val outputFormatter =
                DateTimeFormatter.ofPattern(outputFormatterStr, Locale.getDefault())

            val localDateTime = LocalDateTime.parse(createdAt, inputFormatter)
            return localDateTime.format(outputFormatter)
        } else {
            val inputFormatter = SimpleDateFormat(inputFormatterStr)
            val outputFormatter = SimpleDateFormat(outputFormatterStr, Locale.getDefault())

            var date: Date? = null

            try {
                date = inputFormatter.parse(createdAt) as Date
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return outputFormatter.format(date!!)
        }
    }

    fun createTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    fun createFile(application: Application): File {
        val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
            File(it, application.resources.getString(R.string.app_name)).apply { mkdir() }
        }

        val outputDirectory = if (
            mediaDir != null && mediaDir.exists()
        ) mediaDir else application.filesDir

        return File(outputDirectory, "$timeStamp.jpg")
    }

    fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > MAXIMUM_SIZE)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))

        return file
    }

    fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = Utils.createTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    fun rotateFile(file: File, isBackCamera: Boolean = false) {
        val matrix = Matrix()
        val bitmap = BitmapFactory.decodeFile(file.path)
        val rotation = if (isBackCamera) 90f else -90f
        matrix.postRotate(rotation)
        if (!isBackCamera) {
            matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
    }
}





















