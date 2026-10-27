# AirPulse TV 操作與架構指南

本版本已依據 **Apple Human Interface Guidelines (tvOS / Apple Design System)** 與 **Android TV 原生規範**完成全面重構與升級。

---

## 核心升級亮點

### 1. 遙控器獨立音量鍵接管（無障礙服務架構）
* 透過自研之 `AirPulseAccessibilityService`，全域攔截實體遙控器獨立音量鍵 (`KEYCODE_VOLUME_UP`, `KEYCODE_VOLUME_DOWN`, `KEYCODE_VOLUME_MUTE`)。
* 攔截後直接透過 AirPlay RTSP 協定微調 HomePod 音量，電視內部喇叭維持完全靜音。
* 提供 Apple TV 質感的頂部膠囊音量面板（Volume HUD），操作反饋精確直覺。

### 2. Apple 設計規範 (tvOS) 原生 TV 介面
* **Materials & Depth（毛玻璃磨砂質感）**：
  * 深邃暗黑磨砂玻璃背景 (`#090A0E` ~ `#13151F`)。
  * 半透明卡片層級，頂部具備捕捉環境光的 1px 細微高光邊界。
* **物理彈簧焦點系統**：
  * 專為電視遙控器（D-pad）設計的 `TvFocusableCard`。
  * 焦點選中時：依據 Apple 規範採用 Critically Damped Spring (`dampingRatio = 1.0f`) 平滑放大 1.06x，邊框亮起蘋果藍 (`#0A84FF`) 或高光白，帶有柔和陰影。
* **雙欄式電視佈局 (16:9 Master-Detail)**：
  * **左側邊欄**：AirPulse TV 品牌標識、串流連線狀態卡片、投送主按鈕、音量調節、動態聲波頻譜柱狀圖。
  * **右側工作區**：可用的 AirPlay 設備網格矩陣（HomePod、Apple TV、Mac、Sonos），清晰顯示 IP、設備型號與狀態。
  * **遙控器專用 PIN 碼彈窗**：連接 Apple TV 時自動浮現 4 位數輸入框與 0-9 數字鍵盤，支援遙控器上下左右點選輸入。

### 3. 音訊無縫播放架構
* **本機自動靜音 (Mute Local on Cast)**：
  串流開始時自動將電視內部喇叭靜音，避免雙重聲音與雜音回音干擾；中斷串流時自動還原原始音量。
* **連續相位重採樣器 (Continuous Phase Resampler)**：
  自研 48kHz 至 44.1kHz 浮點多項式重採樣演算法，徹底消除採樣率轉換所產生的雜音與爆音。
