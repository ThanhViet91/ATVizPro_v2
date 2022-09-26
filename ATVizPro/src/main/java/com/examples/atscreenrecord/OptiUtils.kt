/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.examples.atscreenrecord

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*

class OptiUtils {

    val outputPath: String
        get() {
            val path = Environment.getExternalStorageDirectory().toString() + File.separator + "thanhlv/"

            val folder = File(path)
            if (!folder.exists())
                folder.mkdirs()

            return path
        }

    fun copyFileToInternalStorage(resourceId: Int, resourceName: String, context: Context): File {
        val cw = ContextWrapper(context)
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString()+"/thanhlv/"
        val folder = File(path)
            if (!folder.exists())
                folder.mkdirs()

        println("thanhlv copyFileToInternalStorage $path")

        val dataPath = "$path$resourceName.png"
        Log.v("OptiUtils", "path: $dataPath")
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            inputStream.toFile(dataPath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return File(dataPath)
    }

    fun copyFontToInternalStorage(resourceId: Int, resourceName: String, context: Context): File {
        val path = Environment.getExternalStorageDirectory().toString() + File.separator + "thanhlv/"
        val folder = File(path)
        if (!folder.exists())
            folder.mkdirs()

        val dataPath = "$path$resourceName.ttf"
        Log.v("OptiUtils", "path: $dataPath")
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            inputStream.toFile(dataPath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return File(dataPath)
    }

    private fun InputStream.toFile(path: String) {
        File(path).outputStream().use { this.copyTo(it) }
    }

    fun getConvertedFile(folder: String, fileName: String): File {
        val f = File(folder)

        if (!f.exists())
            f.mkdirs()

        return File(f.path + File.separator + fileName)
    }

    fun refreshGallery(path: String, context: Context) {

        val file = File(path)
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshGalleryAlone(context: Context) {
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isVideoHaveAudioTrack(path: String): Boolean {
        var audioTrack = false

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
        audioTrack = hasAudioStr == "yes"

        return audioTrack
    }

}


