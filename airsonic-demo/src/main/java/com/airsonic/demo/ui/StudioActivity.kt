// Copyright (c) 2026 Chunguang Wei (https://github.com/chunguangwei)
// Licensed under the PolyForm Noncommercial License 1.0.0 — noncommercial use only.
// Commercial use requires prior written consent: chunguangwee@gmail.com. See LICENSE.

package com.airsonic.demo.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.remember
import com.airsonic.demo.MainActivity
import com.airsonic.demo.ui.theme.AirSonicTheme
import com.airsonic.demo.ui.theme.AuroraBackground

/** 产品 UI 入口（Compose / Aurora）：托管导航 + 桥接系统能力（投屏授权/文件选择）。 */
class StudioActivity : ComponentActivity() {

    private lateinit var projectionManager: MediaProjectionManager

    private val recordPerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchProjection()
            else {
                CastEngine.phase.value = CastPhase.ERROR
                CastEngine.statusLine.value = if (L10n.lang.value == Lang.EN) "Microphone permission denied" else "未授予錄音權限"
            }
        }

    private val projectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                CastEngine.startSystemAudioCast(this, result.resultCode, result.data!!)
            } else {
                CastEngine.statusLine.value = if (L10n.lang.value == Lang.EN) "Screen-capture canceled" else "已取消系統錄音授權"
            }
        }

    /** 应用内屏幕镜像（DLNA）：音轨需要 RECORD_AUDIO（拒绝则降级纯画面镜像）。 */
    private val mirrorRecordPerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // 不管是否授予都继续发起录屏授权；有无权限决定 TS 里带不带 AAC 音轨
            mirrorProjectionLauncher.launch(projectionManager.createScreenCaptureIntent())
        }

    /** 应用内屏幕镜像（DLNA）：录屏+系统声音（AudioPlaybackCapture→AAC 音轨，声画同投）。 */
    private val mirrorProjectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                CastEngine.startScreenMirrorCast(this, result.resultCode, result.data!!)
            } else {
                CastEngine.statusLine.value = if (L10n.lang.value == Lang.EN) "Screen-capture canceled" else "已取消投屏授权"
            }
        }

    /** HTTP 流输出：同一套 录音权限→录屏授权 链，只是不推设备、暴露直播地址。 */
    private val httpStreamRecordPerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) httpStreamProjectionLauncher.launch(projectionManager.createScreenCaptureIntent())
            else {
                CastEngine.phase.value = CastPhase.ERROR
                CastEngine.statusLine.value = if (L10n.lang.value == Lang.EN) "Microphone permission denied" else "未授予录音权限"
            }
        }

    private val httpStreamProjectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                CastEngine.startHttpStreamCast(this, result.resultCode, result.data!!)
            } else {
                CastEngine.statusLine.value = if (L10n.lang.value == Lang.EN) "Screen-capture canceled" else "已取消投屏授权"
            }
        }

    companion object {
        @Volatile
        var isForeground: Boolean = false
            private set
    }

    override fun onResume() {
        super.onResume()
        isForeground = true
    }

    override fun onPause() {
        super.onPause()
        isForeground = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        L10n.load(this)
        CastEngine.loadPrefs(this)
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val composeView = androidx.compose.ui.platform.ComposeView(this).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
            setContent {
                com.airsonic.demo.ui.tv.TvMainScreen(
                    onRequestSystemAudioCast = { ensurePermThenProjection() }
                )
            }
        }
        setContentView(composeView)
        composeView.requestFocus()
    }

    private fun ensurePermThenProjection() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) launchProjection()
        else recordPerm.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun launchProjection() {
        projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP ||
            event.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN ||
            event.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_MUTE
        ) {
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    android.view.KeyEvent.KEYCODE_VOLUME_UP -> CastEngine.adjustVolume(+5)
                    android.view.KeyEvent.KEYCODE_VOLUME_DOWN -> CastEngine.adjustVolume(-5)
                    android.view.KeyEvent.KEYCODE_VOLUME_MUTE -> CastEngine.toggleMute()
                }
            }
            return true // 劫持獨立音量鍵：直接調整 HomePod 音量，完全不觸動投影機音量
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == android.view.KeyEvent.KEYCODE_VOLUME_MUTE
        ) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == android.view.KeyEvent.KEYCODE_VOLUME_MUTE
        ) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { CastEngine.stopDiscovery() }
    }
}