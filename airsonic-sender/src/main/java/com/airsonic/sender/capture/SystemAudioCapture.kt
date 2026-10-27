// Copyright (c) 2026 Chunguang Wei (https://github.com/chunguangwei)
// Licensed under the PolyForm Noncommercial License 1.0.0 — noncommercial use only.
// Commercial use requires prior written consent: chunguangwee@gmail.com. See LICENSE.

package com.airsonic.sender.capture

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.abs

/**
 * Phase 0 验证点 2：[AudioPlaybackCapture] 系统音频捕获。
 *
 * 通过 [MediaProjection] 授权后，捕获 USAGE_MEDIA / USAGE_GAME / USAGE_UNKNOWN
 * 的播放音频为 PCM。配套 [SilenceDetector] 用于判断捕获是否被目标 App 拒绝
 * （表现为长时间静音），从而驱动「降级到本地文件直投」。
 *
 * 用途：在真机/华为机型上对 QQ音乐/网易云/汽水等逐个实测，建立支持兼容性表。
 */
class SystemAudioCapture(
    val sampleRate: Int = 44100,
    val captureSampleRate: Int = 48000,
    private val channelMask: Int = AudioFormat.CHANNEL_IN_STEREO,
    private val encoding: Int = AudioFormat.ENCODING_PCM_16BIT
) {

    data class CaptureStats(
        val totalFrames: Long,
        val totalBytes: Long,
        val nonSilentFrames: Long,
        val peakAmplitude: Int
    ) {
        val isLikelyCapturing: Boolean
            get() = nonSilentFrames > 0 && peakAmplitude > SILENCE_THRESHOLD
    }

    private var audioRecord: AudioRecord? = null
    private val ioLock = Any()

    private var resamplePhase: Long = 0L
    private val inShorts = ShortArray(8192)
    private var inShortsAvail: Int = 0
    private val rawReadBuf = ByteArray(4096)

    @Volatile
    var running: Boolean = false
        private set

    enum class CaptureMode(val label: String, val detail: String) {
        SUBMIX("系統混音", "低延遲系統混音輸出"),
        PLAYBACK_CAPTURE("標準錄音", "系統音訊錄製"),
        MIC("麥克風", "麥克風即時收音")
    }

    var currentMode: CaptureMode? = null
        private set

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    fun startPlaybackCapture(projection: MediaProjection): Boolean {
        val config = AudioPlaybackCaptureConfiguration.Builder(projection)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(AudioAttributes.USAGE_GAME)
            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
            .build()

        val format = AudioFormat.Builder()
            .setEncoding(encoding)
            .setSampleRate(captureSampleRate)
            .setChannelMask(channelMask)
            .build()

        val minBuf = AudioRecord.getMinBufferSize(captureSampleRate, channelMask, encoding)
        val bufferSize = if (minBuf > 0) maxOf(minBuf * 8, 352 * 4 * 16) else captureSampleRate * 2

        return try {
            val record = AudioRecord.Builder()
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setAudioPlaybackCaptureConfig(config)
                .build()
            if (record.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord not initialized")
                record.release()
                return false
            }
            audioRecord = record
            synchronized(ioLock) {
                inShortsAvail = 0
                resamplePhase = 0L
            }
            record.startRecording()
            running = true
            currentMode = CaptureMode.PLAYBACK_CAPTURE
            Log.i(TAG, "PlaybackCapture started, bufferSize=$bufferSize, capture=$captureSampleRate, out=$sampleRate")
            true
        } catch (t: Throwable) {
            Log.e(TAG, "start PlaybackCapture failed", t)
            false
        }
    }

    fun start(projection: MediaProjection): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startPlaybackCapture(projection)
        } else {
            startSubmix()
        }
    }

    @SuppressLint("MissingPermission")
    fun startSubmix(): Boolean {
        val minBuf = AudioRecord.getMinBufferSize(captureSampleRate, channelMask, encoding)
        val bufferSize = if (minBuf > 0) maxOf(minBuf * 8, 352 * 4 * 16) else captureSampleRate * 2
        return try {
            val audioSource = android.media.MediaRecorder.AudioSource.REMOTE_SUBMIX
            val record = AudioRecord(
                audioSource,
                captureSampleRate,
                channelMask,
                encoding,
                bufferSize
            )
            if (record.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord for REMOTE_SUBMIX not initialized")
                record.release()
                return false
            }
            audioRecord = record
            synchronized(ioLock) {
                inShortsAvail = 0
                resamplePhase = 0L
            }
            record.startRecording()
            running = true
            currentMode = CaptureMode.SUBMIX
            Log.i(TAG, "REMOTE_SUBMIX capture started, bufferSize=$bufferSize, capture=$captureSampleRate, out=$sampleRate")
            true
        } catch (t: Throwable) {
            Log.e(TAG, "startSubmix failed", t)
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun startMic(): Boolean {
        val minBuf = AudioRecord.getMinBufferSize(captureSampleRate, channelMask, encoding)
        val bufferSize = if (minBuf > 0) maxOf(minBuf * 8, 352 * 4 * 16) else captureSampleRate * 2
        return try {
            val record = AudioRecord(
                android.media.MediaRecorder.AudioSource.MIC,
                captureSampleRate,
                channelMask,
                encoding,
                bufferSize
            )
            if (record.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord for MIC not initialized")
                record.release()
                return false
            }
            audioRecord = record
            synchronized(ioLock) {
                inShortsAvail = 0
                resamplePhase = 0L
            }
            record.startRecording()
            running = true
            currentMode = CaptureMode.MIC
            Log.i(TAG, "MIC capture started, bufferSize=$bufferSize, capture=$captureSampleRate, out=$sampleRate")
            true
        } catch (t: Throwable) {
            Log.e(TAG, "startMic failed", t)
            false
        }
    }

    fun pcmFlow(chunkBytes: Int = 4096): Flow<ByteArray> = flow {
        while (running) {
            val chunk = readChunk(chunkBytes)
            if (chunk != null) {
                emit(chunk)
            } else {
                break
            }
        }
    }.flowOn(Dispatchers.IO)

    fun flushStaleBuffer() {
        val tmp = ByteArray(4096)
        var drainedBytes = 0
        synchronized(ioLock) {
            val record = audioRecord ?: return
            if (!running) return
            while (running) {
                val n = record.read(tmp, 0, tmp.size, AudioRecord.READ_NON_BLOCKING)
                if (n > 0) drainedBytes += n else break
            }
            inShortsAvail = 0
            resamplePhase = 0L
        }
        Log.i(TAG, "Flushed stale audio buffer: ${drainedBytes}B (${drainedBytes / 4} frames)")
    }

    fun readChunk(chunkBytes: Int = 1408): ByteArray? {
        if (chunkBytes <= 0) return ByteArray(0)

        if (captureSampleRate == sampleRate) {
            val buffer = ByteArray(chunkBytes)
            var totalRead = 0
            synchronized(ioLock) {
                val record = audioRecord ?: return null
                if (!running) return null
                while (running && totalRead < chunkBytes) {
                    val n = record.read(buffer, totalRead, chunkBytes - totalRead)
                    if (n > 0) {
                        totalRead += n
                    } else if (n == 0) {
                        try { Thread.sleep(2) } catch (_: InterruptedException) { return null }
                    } else {
                        if (!running) return null
                        Log.w(TAG, "AudioRecord.read error=$n")
                        try { Thread.sleep(4) } catch (_: InterruptedException) { return null }
                        break
                    }
                }
                if (!running && totalRead == 0) return null
                return if (totalRead == chunkBytes) {
                    buffer
                } else if (totalRead > 0) {
                    val aligned = totalRead - (totalRead % 4)
                    if (aligned > 0) buffer.copyOf(aligned) else null
                } else null
            }
        }

        val outFrames = chunkBytes / 4
        val outBuffer = ByteArray(chunkBytes)
        val step = ((captureSampleRate.toLong() shl 32) + (sampleRate / 2)) / sampleRate.toLong()

        synchronized(ioLock) {
            val record = audioRecord ?: return null
            if (!running) return null

            for (f in 0 until outFrames) {
                var curIdx = (resamplePhase ushr 32).toInt()
                var neededShorts = (curIdx + 2) * 2

                while (running && inShortsAvail < neededShorts) {
                    if (curIdx > 0) {
                        val consumedShorts = curIdx * 2
                        val remainingShorts = inShortsAvail - consumedShorts
                        if (remainingShorts > 0) {
                            System.arraycopy(inShorts, consumedShorts, inShorts, 0, remainingShorts)
                            inShortsAvail = remainingShorts
                        } else {
                            inShortsAvail = 0
                        }
                        resamplePhase = resamplePhase and 0xFFFFFFFFL
                        curIdx = 0
                        neededShorts = 4
                    }

                    val maxShortsToRead = inShorts.size - inShortsAvail
                    val maxBytesToRead = minOf(rawReadBuf.size, maxShortsToRead * 2)
                    if (maxBytesToRead < 4) break

                    val n = record.read(rawReadBuf, 0, maxBytesToRead)
                    if (n > 0) {
                        var b = 0
                        while (b + 1 < n && inShortsAvail < inShorts.size) {
                            val low = rawReadBuf[b].toInt() and 0xFF
                            val high = rawReadBuf[b + 1].toInt()
                            inShorts[inShortsAvail++] = ((high shl 8) or low).toShort()
                            b += 2
                        }
                    } else if (n == 0) {
                        try { Thread.sleep(2) } catch (_: InterruptedException) { return null }
                    } else {
                        if (!running) return null
                        Log.w(TAG, "AudioRecord.read error=$n")
                        try { Thread.sleep(4) } catch (_: InterruptedException) { return null }
                        break
                    }
                }

                if (!running) return null
                if (inShortsAvail < 4) return null

                curIdx = (resamplePhase ushr 32).toInt()
                val outByteIdx = f * 4
                if ((curIdx + 1) * 2 + 1 >= inShortsAvail) {
                    val lastFrameIdx = maxOf(0, (inShortsAvail / 2) - 1)
                    val l = inShorts[lastFrameIdx * 2].toInt()
                    val r = inShorts[lastFrameIdx * 2 + 1].toInt()
                    outBuffer[outByteIdx] = (l and 0xFF).toByte()
                    outBuffer[outByteIdx + 1] = ((l shr 8) and 0xFF).toByte()
                    outBuffer[outByteIdx + 2] = (r and 0xFF).toByte()
                    outBuffer[outByteIdx + 3] = ((r shr 8) and 0xFF).toByte()
                } else {
                    val frac = ((resamplePhase and 0xFFFFFFFFL) ushr 16).toInt()
                    val invFrac = 65536 - frac

                    val l0 = inShorts[curIdx * 2].toInt()
                    val r0 = inShorts[curIdx * 2 + 1].toInt()
                    val l1 = inShorts[(curIdx + 1) * 2].toInt()
                    val r1 = inShorts[(curIdx + 1) * 2 + 1].toInt()

                    val lOut = ((l0.toLong() * invFrac + l1.toLong() * frac) shr 16).toInt().coerceIn(-32768, 32767)
                    val rOut = ((r0.toLong() * invFrac + r1.toLong() * frac) shr 16).toInt().coerceIn(-32768, 32767)

                    outBuffer[outByteIdx] = (lOut and 0xFF).toByte()
                    outBuffer[outByteIdx + 1] = ((lOut shr 8) and 0xFF).toByte()
                    outBuffer[outByteIdx + 2] = (rOut and 0xFF).toByte()
                    outBuffer[outByteIdx + 3] = ((rOut shr 8) and 0xFF).toByte()
                }

                resamplePhase += step
            }

            val consumedFrames = (resamplePhase ushr 32).toInt()
            if (consumedFrames > 0) {
                val consumedShorts = consumedFrames * 2
                val remainingShorts = inShortsAvail - consumedShorts
                if (remainingShorts > 0) {
                    System.arraycopy(inShorts, consumedShorts, inShorts, 0, remainingShorts)
                    inShortsAvail = remainingShorts
                } else {
                    inShortsAvail = 0
                }
                resamplePhase = resamplePhase and 0xFFFFFFFFL
            }

            return outBuffer
        }
    }

    fun sampleForStats(durationMs: Long): CaptureStats {
        val record = audioRecord ?: error("call start() first")
        val buffer = ByteArray(4096)
        var totalFrames = 0L
        var totalBytes = 0L
        var nonSilent = 0L
        var peak = 0

        val deadline = System.currentTimeMillis() + durationMs
        while (System.currentTimeMillis() < deadline && running) {
            val read = record.read(buffer, 0, buffer.size)
            if (read <= 0) continue
            totalBytes += read
            totalFrames += read / 2
            var i = 0
            var localNonSilent = false
            while (i + 1 < read) {
                val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                val amp = abs(sample)
                if (amp > peak) peak = amp
                if (amp > SILENCE_THRESHOLD) localNonSilent = true
                i += 2
            }
            if (localNonSilent) nonSilent += read / 2
        }
        return CaptureStats(totalFrames, totalBytes, nonSilent, peak)
    }

    fun stop() {
        running = false
        runCatching { audioRecord?.stop() }
        synchronized(ioLock) {
            runCatching { audioRecord?.release() }
            audioRecord = null
            inShortsAvail = 0
            resamplePhase = 0L
        }
        Log.i(TAG, "Capture stopped")
    }

    companion object {
        private const val TAG = "SystemAudioCapture"
        /** 16-bit PCM 静音判定阈值，约 -54 dBFS 以下视为静音。 */
        const val SILENCE_THRESHOLD = 64
    }
}