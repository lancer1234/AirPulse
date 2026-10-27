// Copyright (c) 2026 Chunguang Wei (https://github.com/chunguangwei)
// Licensed under the PolyForm Noncommercial License 1.0.0 — noncommercial use only.
// Commercial use requires prior written consent: chunguangwee@gmail.com. See LICENSE.

package com.airsonic.demo.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/** 语言。 */
enum class Lang { ZH, EN }

/** 全部界面文案（中/英）。 */
data class Strings(
    val tagline: String,
    val skip: String,              // 跳过 / Skip  (后接 "5s")
    val selectDevice: String,
    val settings: String,
    val localMedia: String,
    val video: String,
    val audio: String,
    val mirrorTitle: String,       // 手机镜像 / Phone Mirror
    val mirrorSub: String,
    val mirrorPicture: String,     // 投画面（整屏，应用内投屏 DLNA）/ Mirror screen
    val resolution: String,
    val frameRate: String,
    val comingSoon: String,
    val sound: String,
    val soundOnly: String,
    val soundOnlyDesc: String,
    val soundInfo: String,           // 仅投声音 ⓘ 弹窗说明
    val castVideo: String,
    val castAudio: String,
    val needMedia: String,
    val grantDesc: String,
    val grant: String,
    val noVideo: String,
    val noAudio: String,
    val devicesOnWifi: String,
    val refresh: String,
    val noDevices: String,
    val notSupported: String,
    val supportTitle: String,
    val supportSub: String,
    val howTitle: String,
    val howSub: String,
    val debugTitle: String,
    val debugSub: String,
    val version: String,
    val licenseLine: String,       // 设置页底部版权/许可声明
    val language: String,
    val back: String,
    val device: String,
    val castingTo: String,         // 正在投送 · / Casting ·
    val stop: String,
    val connecting: String,        // 正在连接 / Connecting to
    val stopped: String,
    val playFinished: String,
    val castError: String,         // 投送异常： / Cast error:
    val pairFail: String,
    val setupFail: String,
    val captureFail: String,
    val openFail: String,
    val noDevice: String,
    val discoverFail: String,      // 发现失败： / Discovery failed:
    val notSupportedSuffix: String, // 暂不支持 / not supported（用于 "X 暂不支持"）
    val videoOnTv: String,
    val pause: String,
    val resume: String,
    val pinTitle: String,
    val pinHint: String,
    val confirm: String,
    val cancel: String,
    val rename: String,
    val hide: String,
    val unhide: String,
    val hiddenDevices: String,
    val renameHint: String,
    val checkUpdate: String,       // 检查更新 / Check for updates
    val checkUpdateSub: String,    // 当前版本 vX / Current vX
    val checking: String,          // 正在检查… / Checking…
    val upToDate: String,          // 已是最新版本 / Up to date
    val newVersion: String,        // 发现新版本 / New version
    val download: String,          // 下载并安装 / Download & install
    val downloading: String,       // 下载中 / Downloading
    val updateFailed: String,      // 检查失败 / Check failed
    val installNow: String,        // 已下载，点击安装 / Downloaded, tap to install
    val openInBrowser: String,     // 在浏览器打开 / Open in browser
    val dlnaDevice: String,         // DLNA 设备 / DLNA device
    val castDevice: String,         // 投屏设备（未识别时的通用名）/ Cast device
    val protocolsTitle: String,     // 支持的协议 / Supported protocols
    val protocolsSub: String,       // AirPlay · DLNA · 屏幕镜像
    val tagSystem: String,          // 系统 / System
    val protoAirplay: String,       // AirPlay / AirPlay 2
    val protoAirplaySub: String,    // HomePod · 音箱 · Apple TV · Mac
    val protoDlna: String,          // DLNA / UPnP
    val protoDlnaSub: String,       // 智能电视 · 盒子 · Kodi
    val tagAudio: String,           // 音频 / Audio
    val tagVideoAudio: String,      // 视频+音频 / Video+Audio
    val forceAlacTitle: String,     // 强制 ALAC 编码
    val forceAlacSub: String,       // Sonos 等只收 ALAC 的设备…
    val codecLabel: String,         // 编码 / Codec（投送时显示当前编码）
    val sonosWavTitle: String,      // Sonos 改投 WAV
    val sonosWavSub: String,        // AAC 不出声时打开…
    val legalTitle: String,         // 用户协议与隐私
    val legalSub: String,           // 用户协议 · 隐私政策
    val contactHeading: String,     // 联系与反馈
    val contactBody: String,        // 反馈/Bug/商用 邮件入口说明
    val termsHeading: String,       // 用户协议
    val termsBody: String,
    val privacyHeading: String,     // 隐私政策
    val privacyBody: String,
    val viewFullOnGitHub: String,   // 在 GitHub 查看完整条款
    val volumeLabel: String,        // 投送中音量标签
    val mirrorInAppTitle: String,   // 应用内投屏（DLNA 实时屏幕流）
    val mirrorInAppSub: String,     // 录屏实时投到选中设备…延迟说明
    val mirrorNeedsDlna: String,    // 应用内投屏需选中 DLNA 设备
    val tvDisconnected: String,     // 接收端已断开/停止，投送结束
    val volumeUnsupported: String,  // 音量下发失败提示（设备不支持）
    val bgKeepHint: String,         // 保活说明文案（镜像页感叹号弹窗；跳转动作在 bgKeepAction 按钮）
    val bgKeepAction: String,       // 「设置保活」按钮（跳系统电池/自启动设置）
    val cleanedLastSession: String, // 冷启动清理上次投送残留
    val httpStreamTitle: String,    // HTTP 流输出（AirMusic 式直播地址）
    val httpStreamSub: String,      // 复制地址，任意播放器可收听
    val httpStreamName: String,     // 投送中的设备名占位（无目标设备）
    val copyUrl: String,            // 复制地址
    val copied: String,             // 已复制
)

private val ZH = Strings(
    tagline = "將聲音無線串流至空間",
    skip = "略過", selectDevice = "選擇設備", settings = "設定",
    localMedia = "投送本機媒體", video = "影片", audio = "音訊",
    mirrorTitle = "螢幕鏡像", mirrorSub = "畫面與系統聲音即時串流至 AirPlay / DLNA 設備",
    mirrorPicture = "投送畫面",
    resolution = "解析度", frameRate = "影格率", comingSoon = "即將推出",
    sound = "聲音", soundOnly = "僅投送聲音",
    soundOnlyDesc = "即時將系統音訊串流至所選設備",
    soundInfo = "擷取系統音訊並即時傳送至所選的 AirPlay 或 DLNA 接收端。",
    castVideo = "投送影片", castAudio = "投送音訊",
    needMedia = "需要存取本機媒體", grantDesc = "授權後即可瀏覽並投送檔案", grant = "授權存取",
    noVideo = "找不到影片檔案", noAudio = "找不到音訊檔案",
    devicesOnWifi = "區域網路內的設備", refresh = "重新整理", noDevices = "尚未發現設備", notSupported = "暫不支援",
    supportTitle = "支援設備", supportSub = "相容於 AirPlay 與 DLNA 協定的設備",
    howTitle = "投送說明", howSub = "投送期間將系統音訊即時傳送至外部音響設備",
    debugTitle = "進階調試", debugSub = "開發者選項",
    version = "AirPulse 版本",
    licenseLine = "Copyright 2026 Makoto (lancer1234) · MAKOTO LAB · MIT License",
    language = "語言",
    back = "返回", device = "設備",
    castingTo = "正在串流至", stop = "停止投送",
    connecting = "正在連線至", stopped = "已停止投送", playFinished = "播放完成",
    castError = "投送錯誤：", pairFail = "配對失敗", setupFail = "連線建立失敗",
    captureFail = "音訊擷取初始化失敗", openFail = "無法開啟檔案", noDevice = "請先選擇目標設備",
    discoverFail = "搜尋失敗：", notSupportedSuffix = "暫不支援",
    videoOnTv = "正在投放", pause = "暫停", resume = "繼續",
    pinTitle = "輸入配對碼", pinHint = "請查看螢幕上的 4 位數配對碼", confirm = "確認", cancel = "取消",
    rename = "重新命名", hide = "隱藏", unhide = "取消隱藏", hiddenDevices = "隱藏的設備", renameHint = "設備顯示名稱",
    checkUpdate = "檢查更新", checkUpdateSub = "目前版本", checking = "正在檢查...", upToDate = "已是最新版本",
    newVersion = "發現新版本", download = "下載並安裝", downloading = "下載中", updateFailed = "檢查失敗",
    installNow = "已下載完成，點擊安裝",
    openInBrowser = "在瀏覽器開啟",
    dlnaDevice = "DLNA 設備", castDevice = "投送設備",
    protocolsTitle = "支援的協定", protocolsSub = "AirPlay / DLNA / 螢幕鏡像", tagSystem = "系統",
    protoAirplay = "AirPlay / AirPlay 2", protoAirplaySub = "HomePod / Apple TV / Mac / 音響",
    protoDlna = "DLNA / UPnP", protoDlnaSub = "智慧電視 / 機上盒 / Kodi",
    tagAudio = "音訊", tagVideoAudio = "視訊与音訊",
    forceAlacTitle = "強制 ALAC 編碼",
    forceAlacSub = "僅針對只接收 ALAC 的設備開啟",
    codecLabel = "編碼",
    sonosWavTitle = "Sonos 使用 WAV 串流",
    sonosWavSub = "預設開啟無壓縮低延遲串流",
    legalTitle = "使用者協議與隱私",
    legalSub = "使用者協議 / 隱私權政策",
    contactHeading = "聯繫與回饋",
    contactBody = "問題回報、技術交流與建議，歡迎聯繫作者：",
    termsHeading = "使用者協議",
    termsBody = "AirPulse 是由 MAKOTO LAB 開發之開源音訊投送工具，採用 MIT 開源許可證授權。",
    privacyHeading = "隱私權政策",
    privacyBody = "AirPulse 不收集、不上傳任何個人資料，所有音訊僅透過區域網路即時傳送至您選取的接收設備。",
    viewFullOnGitHub = "在 GitHub 查看完整條款",
    volumeLabel = "音量",
    mirrorInAppTitle = "應用內鏡像",
    mirrorInAppSub = "錄製螢幕與系統聲音即時投送至選取的設備",
    mirrorNeedsDlna = "請先選取目標投送設備",
    tvDisconnected = "接收端已中斷連線",
    volumeUnsupported = "此設備不支援遠端音量控制，請使用實體遙控器",
    bgKeepHint = "若切換至背景時中斷，請於系統設定允許應用程式背景執行",
    bgKeepAction = "背景執行設定",
    cleanedLastSession = "已清理上一次未正常結束的投送工作階段",
    httpStreamTitle = "HTTP 音訊串流輸出",
    httpStreamSub = "產生區域網路串流網址供外部播放器收聽",
    httpStreamName = "HTTP 串流",
    copyUrl = "複製網址",
    copied = "已複製到剪貼簿",
)

private val EN = Strings(
    tagline = "Cast your sound into the room",
    skip = "Skip", selectDevice = "Select device", settings = "Settings",
    localMedia = "Local media", video = "Video", audio = "Audio",
    mirrorTitle = "Phone Mirror", mirrorSub = "In-app screen cast · Sound via AirPlay/DLNA",
    mirrorPicture = "Mirror screen",
    resolution = "Resolution", frameRate = "Frame rate", comingSoon = "Coming soon",
    sound = "Sound", soundOnly = "Sound only",
    soundOnlyDesc = "Live-cast system audio to the selected device (AirPlay/DLNA)",
    soundInfo = "Captures system audio and casts it live to the selected device (AirPlay/DLNA). Sound only, no screen — for screen use the in-app \"Mirror screen\" above.",
    castVideo = "Cast video", castAudio = "Cast audio",
    needMedia = "Media access needed", grantDesc = "Grant access to browse and cast files", grant = "Grant access",
    noVideo = "No video files", noAudio = "No audio files",
    devicesOnWifi = "Devices on this Wi-Fi", refresh = "Refresh", noDevices = "No devices found", notSupported = "Not supported",
    supportTitle = "Supported devices", supportSub = "Any AirPlay-compatible device",
    howTitle = "How it works", howSub = "Phone media volume is lowered while casting; sound plays on the AirPlay device only",
    debugTitle = "Debug · Test points", debugSub = "Developer entry",
    version = "AirPulse · Version",
    licenseLine = "© 2026 Makoto (lancer1234) · MAKOTO LAB · MIT License\nContact / bug reports: danny950811@gmail.com",
    language = "Language",
    back = "Back", device = "Device",
    castingTo = "Casting ·", stop = "Stop casting",
    connecting = "Connecting to", stopped = "Stopped", playFinished = "Playback finished",
    castError = "Cast error: ", pairFail = "Pairing failed", setupFail = "Setup failed",
    captureFail = "Audio capture failed to start", openFail = "Cannot open file", noDevice = "No device selected",
    discoverFail = "Discovery failed: ", notSupportedSuffix = "not supported",
    videoOnTv = "Casting", pause = "Pause", resume = "Resume",
    pinTitle = "Enter pairing code", pinHint = "See the 4-digit PIN on the TV screen", confirm = "OK", cancel = "Cancel",
    rename = "Rename", hide = "Hide", unhide = "Unhide", hiddenDevices = "Hidden", renameHint = "Display name",
    checkUpdate = "Check for updates", checkUpdateSub = "Current", checking = "Checking…", upToDate = "Up to date",
    newVersion = "New version", download = "Download & install", downloading = "Downloading", updateFailed = "Check failed",
    installNow = "Downloaded — tap to install",
    openInBrowser = "Open in browser",
    dlnaDevice = "DLNA device", castDevice = "Cast device",
    protocolsTitle = "Protocols", protocolsSub = "AirPlay · DLNA · Screen mirroring", tagSystem = "System",
    protoAirplay = "AirPlay / AirPlay 2", protoAirplaySub = "HomePod · Speakers · Apple TV · Mac",
    protoDlna = "DLNA / UPnP", protoDlnaSub = "Smart TV · Box · Kodi",
    tagAudio = "Audio", tagVideoAudio = "Video+Audio",
    forceAlacTitle = "Force ALAC",
    forceAlacSub = "Usually keep OFF! Forcing ALAC silences PCM-only speakers; ALAC-only devices are auto-detected",
    codecLabel = "Codec",
    sonosWavTitle = "Sonos: WAV stream",
    sonosWavSub = "On by default (uncompressed, lower latency); off = AAC radio stream (less bandwidth)",
    legalTitle = "Terms & Privacy",
    legalSub = "Terms of Use · Privacy Policy",
    contactHeading = "Contact & feedback",
    contactBody = "Bug reports and suggestions — email the author:",
    termsHeading = "Terms of Use",
    termsBody = "AirPulse is an open-source audio streaming tool developed by MAKOTO LAB and licensed under the MIT License. " +
        "Use it only with devices and media you own or are authorized to use. " +
        "The software is provided \"AS IS\" with no warranty of compatibility or results, and the author is not liable for any damages from its use. " +
        "AirPulse is not affiliated with Apple, Sonos, or any TV vendor; such names are used only to describe compatibility.",
    privacyHeading = "Privacy Policy",
    privacyBody = "AirPulse has no server and collects, uploads, or sells no personal data; it contains no analytics/ads SDK. " +
        "Captured system audio and your local media are streamed only to the receiver you pick, over your LAN — never written to disk or uploaded. " +
        "Network/Wi-Fi permissions are used solely to discover receivers on your LAN. The only outbound request is the optional \"Check for updates\" (reads public GitHub Release info). " +
        "Preferences and AirPlay pairing credentials are stored only in the app's private storage and removed on uninstall.",
    viewFullOnGitHub = "View full text on GitHub",
    volumeLabel = "Volume",
    mirrorInAppTitle = "In-app screen cast",
    mirrorInAppSub = "Mirror screen + system sound live to the selected device (TV/projector/box/Apple TV/Mac), ~1–3s delay",
    mirrorNeedsDlna = "In-app casting needs a target device selected on Home",
    tvDisconnected = "Receiver disconnected — cast ended",
    volumeUnsupported = "Volume command not accepted by this device — use the TV remote",
    bgKeepHint = "Drops when backgrounded? Usually the system's battery saver. Tap \"Keep-alive settings\" to allow auto-start & background run (some ROMs also need \"high background power\")",
    bgKeepAction = "Keep-alive settings",
    cleanedLastSession = "Previous cast did not end cleanly — stop sent to the TV",
    httpStreamTitle = "HTTP stream out",
    httpStreamSub = "Get a live URL — VLC, browsers or any player can tune in",
    httpStreamName = "HTTP stream",
    copyUrl = "Copy URL",
    copied = "Copied to clipboard",
)

object L10n {
    private const val PREF = "airsonic_prefs"
    private const val KEY = "lang"

    /** 当前语言（Compose 状态，切换即刷新 UI）。 */
    val lang = mutableStateOf(Lang.ZH)

    /** 当前字符串表。 */
    val s: Strings get() = if (lang.value == Lang.EN) EN else ZH

    fun load(context: Context) {
        val v = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, null)
        lang.value = if (v == "en") Lang.EN else Lang.ZH
    }

    fun set(context: Context, l: Lang) {
        lang.value = l
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString(KEY, if (l == Lang.EN) "en" else "zh").apply()
    }
}