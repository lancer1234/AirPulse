// Copyright (c) 2026 Chunguang Wei / Enhanced with Apple Human Interface Guidelines
package com.airsonic.demo.ui.tv

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.airsonic.demo.ui.CastEngine
import com.airsonic.demo.ui.CastPhase
import com.airsonic.demo.ui.theme.AppleTvTheme
import com.airsonic.demo.ui.theme.TvFocusableCard
import com.airsonic.sender.api.AirDevice
import com.airsonic.sender.api.DeviceType
import com.airsonic.sender.capture.SystemAudioCapture

/**
 * Unified Apple HIG Adaptive Interface for Phone (Portrait) & Android TV / Tablet (Landscape).
 * - Strictly zero emojis across all labels.
 * - Authentic iOS 18 / tvOS 18 Dark Mode grouped surfaces (#000000 canvas, #1C1C1E cards, #2C2C2E controls).
 * - Seamless Android 8/9/10+ low-latency audio capture with automatic runtime permission handling.
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun TvMainScreen(
    onRequestSystemAudioCast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val devices = CastEngine.devices
    val selectedDevice by CastEngine.selected
    val phase by CastEngine.phase
    val statusLine by CastEngine.statusLine
    val activeMode by CastEngine.audioCaptureMode
    val latencyPreset by CastEngine.latencyPreset
    val volumePct by CastEngine.volumePct
    val isCasting = phase == CastPhase.CASTING || phase == CastPhase.CONNECTING
    val pinRequest by CastEngine.pinRequest

    var hasSubmixPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                "android.permission.CAPTURE_AUDIO_OUTPUT"
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var pendingDirectMode by remember {
        mutableStateOf(SystemAudioCapture.CaptureMode.SUBMIX)
    }

    // Both SUBMIX and MIC require runtime RECORD_AUDIO permission on Android 6.0+
    val recordPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            CastEngine.startDirectAudioCast(context, pendingDirectMode)
        } else {
            CastEngine.phase.value = CastPhase.ERROR
            CastEngine.statusLine.value = "未授予錄音權限，無法初始化音訊擷取"
        }
    }

    fun startDirectWithPermCheck(mode: SystemAudioCapture.CaptureMode) {
        hasSubmixPerm = ContextCompat.checkSelfPermission(
            context,
            "android.permission.CAPTURE_AUDIO_OUTPUT"
        ) == PackageManager.PERMISSION_GRANTED

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            CastEngine.startDirectAudioCast(context, mode)
        } else {
            pendingDirectMode = mode
            recordPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        val hasSubmix = ContextCompat.checkSelfPermission(
            context,
            "android.permission.CAPTURE_AUDIO_OUTPUT"
        ) == PackageManager.PERMISSION_GRANTED
        hasSubmixPerm = hasSubmix

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            CastEngine.audioCaptureMode.value == SystemAudioCapture.CaptureMode.PLAYBACK_CAPTURE
        ) {
            CastEngine.setAudioCaptureMode(context, SystemAudioCapture.CaptureMode.SUBMIX)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasSubmix &&
            CastEngine.audioCaptureMode.value == SystemAudioCapture.CaptureMode.SUBMIX
        ) {
            CastEngine.setAudioCaptureMode(context, SystemAudioCapture.CaptureMode.PLAYBACK_CAPTURE)
        }
        CastEngine.startDiscovery(context)
    }

    val handleToggleCast = {
        if (isCasting) {
            CastEngine.stop()
        } else {
            when (activeMode) {
                SystemAudioCapture.CaptureMode.SUBMIX -> {
                    startDirectWithPermCheck(SystemAudioCapture.CaptureMode.SUBMIX)
                }
                SystemAudioCapture.CaptureMode.MIC -> {
                    startDirectWithPermCheck(SystemAudioCapture.CaptureMode.MIC)
                }
                SystemAudioCapture.CaptureMode.PLAYBACK_CAPTURE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        onRequestSystemAudioCast()
                    } else {
                        CastEngine.setAudioCaptureMode(context, SystemAudioCapture.CaptureMode.SUBMIX)
                        startDirectWithPermCheck(SystemAudioCapture.CaptureMode.SUBMIX)
                    }
                }
            }
        }
    }

    val handleSelectMode: (SystemAudioCapture.CaptureMode) -> Unit = { mode ->
        if (mode == SystemAudioCapture.CaptureMode.PLAYBACK_CAPTURE &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        ) {
            CastEngine.statusLine.value = "Android 9 不支援標準系統錄音，已保持系統內錄低延遲模式"
            CastEngine.setAudioCaptureMode(context, SystemAudioCapture.CaptureMode.SUBMIX)
        } else {
            CastEngine.setAudioCaptureMode(context, mode)
        }
        hasSubmixPerm = ContextCompat.checkSelfPermission(
            context,
            "android.permission.CAPTURE_AUDIO_OUTPUT"
        ) == PackageManager.PERMISSION_GRANTED
    }

    val inputModeManager = LocalInputModeManager.current
    val heroButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        inputModeManager.requestInputMode(InputMode.Keyboard)
        for (delayMs in listOf(50L, 150L, 350L, 700L, 1200L)) {
            kotlinx.coroutines.delay(delayMs)
            val ok = runCatching {
                heroButtonFocusRequester.requestFocus()
                true
            }.getOrDefault(false)
            if (ok) break
        }
    }

    var showVolumeHud by remember { mutableStateOf(false) }
    var lastVolumePct by remember { mutableStateOf(volumePct) }
    LaunchedEffect(volumePct) {
        if (volumePct != lastVolumePct) {
            lastVolumePct = volumePct
            showVolumeHud = true
            kotlinx.coroutines.delay(2200)
            showVolumeHud = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(AppleTvTheme.BackgroundDeep)
    ) {
        val isWideScreen = maxWidth >= 680.dp

        if (isWideScreen) {
            // ---- Landscape / Android TV / Tablet Two-Column Layout ----
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 20.dp)
            ) {
                AppleNavigationHeader(
                    deviceCount = devices.size,
                    onRefresh = { CastEngine.startDiscovery(context) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .width(380.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AppleHeroControlCard(
                            isCasting = isCasting,
                            phase = phase,
                            statusLine = statusLine,
                            selectedDevice = selectedDevice,
                            onToggleCast = handleToggleCast,
                            focusRequester = heroButtonFocusRequester
                        )

                        AppleAudioModeSection(
                            currentMode = activeMode,
                            hasSubmixPerm = hasSubmixPerm,
                            onSelectMode = handleSelectMode
                        )

                        AppleLatencyAndVolumeSection(
                            currentLatency = latencyPreset,
                            volumePct = volumePct,
                            onSelectLatency = { CastEngine.setLatencyPreset(context, it) },
                            onVolumeChange = { delta ->
                                CastEngine.setVolume((volumePct + delta).coerceIn(0, 100))
                            }
                        )
                    }

                    AppleReceiversLandscapePanel(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        devices = devices,
                        selectedDevice = selectedDevice,
                        isCasting = isCasting,
                        onSelectDevice = { CastEngine.select(it) }
                    )
                }
            }
        } else {
            // ---- Portrait / Compact Phone Single-Column iOS Grouped Layout ----
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppleNavigationHeader(
                    deviceCount = devices.size,
                    onRefresh = { CastEngine.startDiscovery(context) }
                )

                AppleHeroControlCard(
                    isCasting = isCasting,
                    phase = phase,
                    statusLine = statusLine,
                    selectedDevice = selectedDevice,
                    onToggleCast = handleToggleCast
                )

                AppleReceiversPortraitSection(
                    devices = devices,
                    selectedDevice = selectedDevice,
                    isCasting = isCasting,
                    onSelectDevice = { CastEngine.select(it) }
                )

                AppleAudioModeSection(
                    currentMode = activeMode,
                    hasSubmixPerm = hasSubmixPerm,
                    onSelectMode = handleSelectMode
                )

                AppleLatencyAndVolumeSection(
                    currentLatency = latencyPreset,
                    volumePct = volumePct,
                    onSelectLatency = { CastEngine.setLatencyPreset(context, it) },
                    onVolumeChange = { delta ->
                        CastEngine.setVolume((volumePct + delta).coerceIn(0, 100))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (pinRequest != null) {
            TvPinDialog(
                deviceName = pinRequest ?: "AirPlay",
                onSubmit = { pin -> CastEngine.submitPin(pin) },
                onCancel = { CastEngine.cancelPin() }
            )
        }

        AppleVolumeHudOverlay(
            volumePct = volumePct,
            deviceName = selectedDevice?.name,
            visible = showVolumeHud,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * Minimalist Apple HIG Header Bar
 */
@Composable
private fun AppleNavigationHeader(
    deviceCount: Int,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1C1E26), Color(0xFF0E1015))
                        )
                    )
                    .border(0.5.dp, Color(0x1FFFFFFF), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.airsonic.demo.R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AirPulse",
                    color = AppleTvTheme.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1
                )
                Text(
                    text = if (deviceCount > 0) "AirPlay 2 無線音訊 · $deviceCount 台設備"
                    else "AirPlay 2 無線音訊 · 正在掃描",
                    color = AppleTvTheme.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        TvFocusableCard(
            onClick = onRefresh,
            shape = RoundedCornerShape(50),
            backgroundColor = AppleTvTheme.GroupedSurface,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) { isFocused ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = if (isFocused) AppleTvTheme.Blue else AppleTvTheme.TextSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "重新整理",
                    color = if (isFocused) AppleTvTheme.Blue else AppleTvTheme.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * Hero Control Center Now Playing & Primary Action Card
 */
@Composable
private fun AppleHeroControlCard(
    isCasting: Boolean,
    phase: CastPhase,
    statusLine: String,
    selectedDevice: AirDevice?,
    onToggleCast: () -> Unit,
    focusRequester: FocusRequester? = null
) {
    val statusDotColor by animateColorAsState(
        targetValue = when (phase) {
            CastPhase.CASTING -> AppleTvTheme.Green
            CastPhase.CONNECTING -> AppleTvTheme.Orange
            CastPhase.ERROR -> AppleTvTheme.Red
            CastPhase.IDLE -> if (selectedDevice != null) AppleTvTheme.Blue else AppleTvTheme.TextTertiary
        },
        label = "status_dot"
    )

    val headlineText = when {
        phase == CastPhase.CASTING -> selectedDevice?.name ?: "正在串流音訊"
        phase == CastPhase.CONNECTING -> "正在連線至 ${selectedDevice?.name ?: "接收端"}"
        selectedDevice != null -> selectedDevice.name
        else -> "尚未選擇輸出設備"
    }

    val subtitleText = when {
        statusLine.isNotEmpty() -> statusLine
        phase == CastPhase.CASTING -> "AirPlay 即時低延遲音訊串流中"
        selectedDevice != null -> "已準備就緒，點擊下方按鈕開始串流"
        else -> "請從下方列表選擇同一 Wi-Fi 下的 AirPlay 設備"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppleTvTheme.GroupedSurface)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (phase == CastPhase.CASTING) AppleTvTheme.Green.copy(alpha = 0.16f)
                            else AppleTvTheme.ElevatedSurface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (phase == CastPhase.CASTING) Icons.Rounded.GraphicEq else Icons.Rounded.Speaker,
                        contentDescription = null,
                        tint = if (phase == CastPhase.CASTING) AppleTvTheme.Green else AppleTvTheme.Blue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = headlineText,
                            color = AppleTvTheme.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitleText,
                        color = if (phase == CastPhase.ERROR) AppleTvTheme.Red else AppleTvTheme.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            TvAudioWaveformBars(
                isCasting = phase == CastPhase.CASTING,
                modifier = Modifier.width(76.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Pill Button (Apple System Blue / Red)
        TvFocusableCard(
            onClick = onToggleCast,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (focusRequester != null) {
                        Modifier.focusRequester(focusRequester)
                    } else Modifier
                ),
            shape = RoundedCornerShape(14.dp),
            backgroundColor = if (isCasting) AppleTvTheme.Red.copy(alpha = 0.18f) else AppleTvTheme.Blue,
            pressedBackgroundColor = if (isCasting) AppleTvTheme.Red.copy(alpha = 0.28f) else AppleTvTheme.Blue.copy(alpha = 0.85f),
            focusedBorderColor = if (isCasting) AppleTvTheme.Red else Color.White,
            contentPadding = PaddingValues(vertical = 13.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCasting) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = if (isCasting) AppleTvTheme.Red else Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCasting) "停止 AirPlay 投送" else "開始 AirPlay 投送",
                    color = if (isCasting) AppleTvTheme.Red else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Portrait AirPlay Receivers Grouped List (iOS AirPlay Picker Sheet style)
 */
@Composable
private fun AppleReceiversPortraitSection(
    devices: List<AirDevice>,
    selectedDevice: AirDevice?,
    isCasting: Boolean,
    onSelectDevice: (AirDevice) -> Unit
) {
    Column {
        SectionHeader(
            title = "AIRPLAY 接收設備",
            trailing = if (devices.isNotEmpty()) "${devices.size} 台可用" else null
        )
        Spacer(modifier = Modifier.height(7.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(AppleTvTheme.GroupedSurface)
        ) {
            if (devices.isEmpty()) {
                EmptyReceiversPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 20.dp)
                )
            } else {
                devices.forEachIndexed { index, device ->
                    val isSelected = selectedDevice?.id == device.id
                    val deviceCasting = isCasting && isSelected
                    AppleReceiverRow(
                        device = device,
                        isSelected = isSelected,
                        isCasting = deviceCasting,
                        onClick = { onSelectDevice(device) }
                    )
                    if (index < devices.lastIndex) {
                        Divider(
                            color = AppleTvTheme.HairlineDivider,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 64.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Landscape AirPlay Receivers Panel (Android TV & Wide Screen)
 */
@Composable
private fun AppleReceiversLandscapePanel(
    modifier: Modifier = Modifier,
    devices: List<AirDevice>,
    selectedDevice: AirDevice?,
    isCasting: Boolean,
    onSelectDevice: (AirDevice) -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AppleTvTheme.GroupedSurface)
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AirPlay 接收設備",
                color = AppleTvTheme.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "點選或使用方向鍵選擇目標音響",
                color = AppleTvTheme.TextTertiary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (devices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyReceiversPlaceholder()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                devices.chunked(2).forEachIndexed { _, rowDevices ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowDevices.forEachIndexed { _, device ->
                            Box(modifier = Modifier.weight(1f)) {
                                val isSelected = selectedDevice?.id == device.id
                                val deviceCasting = isCasting && isSelected
                                AppleReceiverGridCard(
                                    device = device,
                                    isSelected = isSelected,
                                    isCasting = deviceCasting,
                                    onClick = { onSelectDevice(device) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        if (rowDevices.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyReceiversPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = AppleTvTheme.Blue,
            modifier = Modifier.size(26.dp),
            strokeWidth = 2.5.dp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "正在搜尋區域網路內的 AirPlay 設備",
            color = AppleTvTheme.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "請確認此裝置與 HomePod 或 Apple TV 連線至相同 Wi-Fi",
            color = AppleTvTheme.TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AppleReceiverRow(
    device: AirDevice,
    isSelected: Boolean,
    isCasting: Boolean,
    onClick: () -> Unit
) {
    val icon = resolveDeviceIcon(device)
    val modelLabel = device.txtRecords["model"] ?: device.type.name

    TvFocusableCard(
        onClick = onClick,
        selected = false,
        shape = RoundedCornerShape(0.dp),
        backgroundColor = Color.Transparent,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isCasting -> AppleTvTheme.Green.copy(alpha = 0.18f)
                            isSelected || isFocused -> AppleTvTheme.Blue.copy(alpha = 0.18f)
                            else -> AppleTvTheme.ElevatedSurface
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        isCasting -> AppleTvTheme.Green
                        isSelected || isFocused -> AppleTvTheme.Blue
                        else -> AppleTvTheme.TextSecondary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    color = AppleTvTheme.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${device.host} · $modelLabel",
                    color = AppleTvTheme.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isCasting) {
                StatusPill(text = "串流中", color = AppleTvTheme.Green)
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = AppleTvTheme.Blue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AppleReceiverGridCard(
    device: AirDevice,
    isSelected: Boolean,
    isCasting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val icon = resolveDeviceIcon(device)
    val modelLabel = device.txtRecords["model"] ?: device.type.name

    TvFocusableCard(
        onClick = onClick,
        selected = isSelected,
        showRestingBorder = isSelected,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = if (isSelected) AppleTvTheme.ElevatedSurface else Color(0xFF242426),
        contentPadding = PaddingValues(16.dp),
        focusedBorderColor = Color.White,
        modifier = modifier
    ) { isFocused ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isCasting -> AppleTvTheme.Green.copy(alpha = 0.18f)
                            isSelected || isFocused -> AppleTvTheme.Blue.copy(alpha = 0.18f)
                            else -> AppleTvTheme.ElevatedSurfaceActive
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        isCasting -> AppleTvTheme.Green
                        isSelected || isFocused -> AppleTvTheme.Blue
                        else -> AppleTvTheme.TextSecondary
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    color = AppleTvTheme.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${device.host} · $modelLabel",
                    color = AppleTvTheme.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        isCasting -> "正在串流音訊"
                        isSelected -> "已選取"
                        else -> "AirPlay 2"
                    },
                    color = when {
                        isCasting -> AppleTvTheme.Green
                        isSelected -> AppleTvTheme.Blue
                        else -> AppleTvTheme.TextTertiary
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Audio Capture Engine Inset Grouped Card (3 modes, zero emojis)
 */
@Composable
private fun AppleAudioModeSection(
    currentMode: SystemAudioCapture.CaptureMode,
    hasSubmixPerm: Boolean,
    onSelectMode: (SystemAudioCapture.CaptureMode) -> Unit
) {
    val isAndroid10Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    Column {
        SectionHeader(title = "音訊擷取模式")
        Spacer(modifier = Modifier.height(7.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(AppleTvTheme.GroupedSurface)
        ) {
            // Mode 1: SUBMIX
            AppleModeRow(
                icon = Icons.Rounded.GraphicEq,
                title = "系統混音",
                subtitle = "低延遲系統混音輸出",
                badgeText = if (hasSubmixPerm) "已就緒" else "需權限",
                badgeColor = if (hasSubmixPerm) AppleTvTheme.Green else AppleTvTheme.Orange,
                isSelected = currentMode == SystemAudioCapture.CaptureMode.SUBMIX,
                onClick = { onSelectMode(SystemAudioCapture.CaptureMode.SUBMIX) }
            )

            Divider(
                color = AppleTvTheme.HairlineDivider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 58.dp)
            )

            // Mode 2: PLAYBACK_CAPTURE
            AppleModeRow(
                icon = Icons.Rounded.SpeakerGroup,
                title = "標準錄音",
                subtitle = "系統音訊錄製",
                badgeText = if (isAndroid10Plus) "標準" else "限 Android 10+",
                badgeColor = if (isAndroid10Plus) AppleTvTheme.Blue else AppleTvTheme.TextTertiary,
                isSelected = currentMode == SystemAudioCapture.CaptureMode.PLAYBACK_CAPTURE,
                onClick = { onSelectMode(SystemAudioCapture.CaptureMode.PLAYBACK_CAPTURE) }
            )

            Divider(
                color = AppleTvTheme.HairlineDivider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 58.dp)
            )

            // Mode 3: MIC
            AppleModeRow(
                icon = Icons.Rounded.Mic,
                title = "麥克風",
                subtitle = "麥克風即時收音",
                badgeText = "測試",
                badgeColor = AppleTvTheme.Indigo,
                isSelected = currentMode == SystemAudioCapture.CaptureMode.MIC,
                onClick = { onSelectMode(SystemAudioCapture.CaptureMode.MIC) }
            )
        }

        if (currentMode == SystemAudioCapture.CaptureMode.SUBMIX && !hasSubmixPerm) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppleTvTheme.Orange.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "需授予權限：adb shell pm grant com.airsonic.demo android.permission.CAPTURE_AUDIO_OUTPUT",
                    color = AppleTvTheme.Orange,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AppleModeRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        selected = isSelected,
        shape = RoundedCornerShape(12.dp),
        focusedBorderColor = AppleTvTheme.Blue,
        backgroundColor = if (isSelected) AppleTvTheme.ElevatedSurface.copy(alpha = 0.5f) else Color.Transparent,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        modifier = Modifier.fillMaxWidth()
    ) { isFocused ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected || isFocused) AppleTvTheme.Blue.copy(alpha = 0.18f)
                        else AppleTvTheme.ElevatedSurface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected || isFocused) AppleTvTheme.Blue else AppleTvTheme.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = AppleTvTheme.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = AppleTvTheme.TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            StatusPill(text = badgeText, color = badgeColor)

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.size(18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppleTvTheme.Blue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Apple Segmented Control for Buffer Latency & Interactive Volume Control Card
 */
@Composable
private fun AppleLatencyAndVolumeSection(
    currentLatency: CastEngine.AirplayLatencyPreset,
    volumePct: Int,
    onSelectLatency: (CastEngine.AirplayLatencyPreset) -> Unit,
    onVolumeChange: (Int) -> Unit
) {
    Column {
        SectionHeader(title = "接收端緩衝與音量")
        Spacer(modifier = Modifier.height(7.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(AppleTvTheme.GroupedSurface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AirPlay 緩衝延遲",
                    color = AppleTvTheme.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Authentic Apple UISegmentedControl Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppleTvTheme.SegmentedTrack)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                CastEngine.AirplayLatencyPreset.values().forEach { preset ->
                    val selected = currentLatency == preset
                    val label = "${preset.ms} ms"
                    TvFocusableCard(
                        onClick = { onSelectLatency(preset) },
                        selected = selected,
                         modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        backgroundColor = if (selected) AppleTvTheme.SegmentedThumb else Color.Transparent,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected || isFocused) AppleTvTheme.TextPrimary else AppleTvTheme.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = AppleTvTheme.HairlineDivider, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Volume Control Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "輸出音量",
                    color = AppleTvTheme.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$volumePct%",
                    color = AppleTvTheme.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TvFocusableCard(
                    onClick = { onVolumeChange(-5) },
                    shape = RoundedCornerShape(10.dp),
                    backgroundColor = AppleTvTheme.ElevatedSurface,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeDown,
                        contentDescription = "降低音量",
                        tint = AppleTvTheme.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Clean Apple Control Center Volume Progress Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(AppleTvTheme.SegmentedTrack)
                ) {
                    val fillFraction = (volumePct / 100f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fillFraction)
                            .clip(RoundedCornerShape(5.dp))
                            .background(AppleTvTheme.Blue)
                    )
                }

                TvFocusableCard(
                    onClick = { onVolumeChange(5) },
                    shape = RoundedCornerShape(10.dp),
                    backgroundColor = AppleTvTheme.ElevatedSurface,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeUp,
                        contentDescription = "提高音量",
                        tint = AppleTvTheme.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = AppleTvTheme.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )
        if (trailing != null) {
            Text(
                text = trailing,
                color = AppleTvTheme.TextTertiary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun resolveDeviceIcon(device: AirDevice): ImageVector {
    val model = device.txtRecords["model"] ?: ""
    return when (device.type) {
        DeviceType.HOMEPOD -> Icons.Rounded.Speaker
        DeviceType.APPLE_TV -> Icons.Rounded.Tv
        DeviceType.MAC -> Icons.Rounded.Laptop
        DeviceType.SONOS -> Icons.Rounded.Speaker
        DeviceType.DLNA -> Icons.Rounded.Tv
        DeviceType.XIAOMI -> Icons.Rounded.Speaker
        else -> {
            when {
                model.contains("HomePod", ignoreCase = true) -> Icons.Rounded.Speaker
                model.contains("AppleTV", ignoreCase = true) -> Icons.Rounded.Tv
                model.contains("Mac", ignoreCase = true) -> Icons.Rounded.Laptop
                else -> Icons.Rounded.Airplay
            }
        }
    }
}

/**
 * Compact Apple-style Real-time FFT Spectrum Bars
 */
@Composable
private fun TvAudioWaveformBars(
    isCasting: Boolean,
    modifier: Modifier = Modifier
) {
    val spectrum by CastEngine.spectrum
    val level by CastEngine.level

    Row(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppleTvTheme.ElevatedSurface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 10
        repeat(barCount) { index ->
            val bandEnergy = if (isCasting && spectrum.isNotEmpty()) {
                val mappedIdx = (index * spectrum.size / barCount).coerceIn(0, spectrum.lastIndex)
                spectrum[mappedIdx].coerceIn(0f, 1f)
            } else 0f

            val targetH = if (isCasting) {
                (3f + (bandEnergy * 11f) + (level * 3f)).coerceIn(3f, 16f)
            } else 3f

            val animatedHeight by animateFloatAsState(
                targetValue = targetH,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "fft_bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        if (isCasting) AppleTvTheme.Green else AppleTvTheme.TextTertiary
                    )
            )
        }
    }
}

/**
 * 4-Digit AirPlay PIN Dialog (Zero emojis, supports Touch & TV Remote D-Pad / Number keys)
 */
@Composable
private fun TvPinDialog(
    deviceName: String,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    var pinValue by remember(CastEngine.pinNonce.value) { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .onPreviewKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    val code = event.nativeKeyEvent.keyCode
                    when (code) {
                        in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                            val digit = (code - KeyEvent.KEYCODE_0).toString()
                            if (pinValue.length < 4) {
                                pinValue += digit
                                if (pinValue.length == 4) onSubmit(pinValue)
                            }
                            true
                        }
                        in KeyEvent.KEYCODE_NUMPAD_0..KeyEvent.KEYCODE_NUMPAD_9 -> {
                            val digit = (code - KeyEvent.KEYCODE_NUMPAD_0).toString()
                            if (pinValue.length < 4) {
                                pinValue += digit
                                if (pinValue.length == 4) onSubmit(pinValue)
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DEL -> {
                            if (pinValue.isNotEmpty()) pinValue = pinValue.dropLast(1)
                            true
                        }
                        KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                            onCancel()
                            true
                        }
                        else -> false
                    }
                } else false
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(22.dp))
                .background(AppleTvTheme.GroupedSurface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AirPlay 配對驗證",
                color = AppleTvTheme.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "請輸入顯示於「$deviceName」螢幕上的 4 位數配對碼",
                color = AppleTvTheme.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { idx ->
                    val char = pinValue.getOrNull(idx)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppleTvTheme.ElevatedSurface)
                            .border(
                                width = 1.dp,
                                color = if (char.isNotEmpty()) AppleTvTheme.Blue else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            color = AppleTvTheme.TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("退格", "0", "確定")
                ).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { keyLabel ->
                            TvFocusableCard(
                                onClick = {
                                    when (keyLabel) {
                                        "退格" -> if (pinValue.isNotEmpty()) pinValue = pinValue.dropLast(1)
                                        "確定" -> if (pinValue.length == 4) onSubmit(pinValue)
                                        else -> if (pinValue.length < 4) {
                                            pinValue += keyLabel
                                            if (pinValue.length == 4) onSubmit(pinValue)
                                        }
                                    }
                                },
                                modifier = Modifier.size(72.dp, 44.dp),
                                shape = RoundedCornerShape(10.dp),
                                backgroundColor = AppleTvTheme.ElevatedSurface,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = keyLabel,
                                        color = if (keyLabel == "確定") AppleTvTheme.Blue else AppleTvTheme.TextPrimary,
                                        fontSize = if (keyLabel.length > 1) 13.sp else 17.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TvFocusableCard(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                backgroundColor = Color.Transparent,
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "取消",
                        color = AppleTvTheme.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Apple TV Floating Volume HUD Toast
 * Displays when volume keys are pressed to adjust HomePod volume.
 */
@Composable
fun AppleVolumeHudOverlay(
    volumePct: Int,
    deviceName: String?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(top = 28.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xF01C1C1E))
                .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = if (volumePct == 0) Icons.Rounded.VolumeMute else Icons.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = AppleTvTheme.Blue,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(160.dp)
                    ) {
                        Text(
                            text = deviceName ?: "HomePod",
                            color = AppleTvTheme.TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$volumePct%",
                            color = AppleTvTheme.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF3A3A3C))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(volumePct / 100f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(AppleTvTheme.Blue)
                        )
                    }
                }
            }
        }
    }
}
