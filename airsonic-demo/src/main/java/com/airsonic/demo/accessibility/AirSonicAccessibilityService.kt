package com.airsonic.demo.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.airsonic.demo.ui.CastEngine
import com.airsonic.demo.ui.StudioActivity

/**
 * AirSonic TV 遙控器硬體音量鍵全域劫持服務。
 *
 * 核心原理：
 * Android TV / Google TV 底層 PhoneWindowManager 會在 system_server 層攔截 KEYCODE_VOLUME_*，
 * 直接調用 AudioService 變更本機/投影機喇叭音量，普通 Activity 無法全域攔截。
 *
 * 本 AccessibilityService 透過 FLAG_REQUEST_FILTER_KEY_EVENTS，在 InputReader / InputDispatcher
 * 階段優先獲取所有硬體按鍵事件。當偵測到獨立音量鍵時：
 * 1. 直接驅動 CastEngine 調整 HomePod (AirPlay 2) 音量；
 * 2. 回傳 true 消耗事件，確保投影機本機喇叭音量絕對不會被變動。
 */
class AirSonicAccessibilityService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            val info = serviceInfo ?: AccessibilityServiceInfo()
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.notificationTimeout = 100
            serviceInfo = info
            Log.i(TAG, "AirSonicAccessibilityService connected and key filter enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring AccessibilityServiceInfo", e)
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        Log.i(TAG, "onKeyEvent: keyCode=$keyCode action=${event.action}")
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
        ) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                mainHandler.post {
                    when (keyCode) {
                        KeyEvent.KEYCODE_VOLUME_UP -> CastEngine.adjustVolume(5)
                        KeyEvent.KEYCODE_VOLUME_DOWN -> CastEngine.adjustVolume(-5)
                        KeyEvent.KEYCODE_VOLUME_MUTE -> CastEngine.toggleMute()
                    }
                }
            }
            // 回傳 true 徹底攔截：系統音訊服務無法接收，投影機喇叭音量保持不變
            return true
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 僅專注於按鍵過濾，不處理 UI 事件
    }

    override fun onInterrupt() {
        Log.i(TAG, "AirSonicAccessibilityService interrupted")
    }

    companion object {
        private const val TAG = "AirSonicAccess"

        fun isEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${AirSonicAccessibilityService::class.java.name}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.split(':').any { it.equals(expectedServiceName, ignoreCase = true) }
        }
    }
}
