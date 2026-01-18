// FILE: ui/audio/AudioRecorder.kt
package com.example.denikplus.ui.audio

import android.content.Context
import android.media.MediaRecorder
import androidx.core.content.FileProvider
import java.io.File

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outFile: File? = null

    fun start(): File {
        stopSafely()

        val file = File(context.cacheDir, "rec_${System.currentTimeMillis()}.m4a")
        outFile = file

        val r = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        recorder = r
        return file
    }

    fun stop(): File? {
        val f = outFile
        stopSafely()
        return f
    }

    private fun stopSafely() {
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
    }

    fun fileToUri(file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
