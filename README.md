# AirPulse TV 脈衝音訊

> **Android TV & Google TV 直連 Apple HomePod / AirPlay 2 的超低延遲無線音訊橋樑。**  
> 一鍵將智慧電視、投影機與電視盒的系統聲音，無損傳輸至客廳的 HomePod 與 AirPlay 音響。

---

## 為什麼需要 AirPulse？

許多人客廳擁有音質優異的 **Apple HomePod** 或 **AirPlay 頂級音響**，但客廳的電視或投影機卻是 **Android TV / Google TV** 系統，兩者處於封閉的生態系而無法互相連線。

**AirPulse** 是專為解決客廳視聽痛點打造的獨立開源工具：

* **無損無線串流**：無需外接額外發射器，直接透過電視內建 Wi-Fi，將電視系統聲音以高傳真 PCM 數位音訊即時串流至 HomePod。
* **實體遙控器音量接管**：業界首創！直接按下電視或投影機遙控器上的**獨立音量鍵**，即可精準控制 HomePod 音量，電視本機喇叭完全不被吵醒。
* **投送時本機自動靜音**：串流開始時自動將電視/投影機內部喇叭靜音，徹底消除聲音疊加與雜音回音。
* **Apple TV 質感極簡介面**：採用「深空極光（Aurora）」流動玻璃質感設計，完美適配電視大螢幕與遙控器方向鍵（D-pad）操作，極致流暢。
* **極致隱私保護**：無任何外部伺服器、無廣告、無追蹤 SDK；所有音訊封包均在您的居家區域網路內點對點傳輸。

---

## 支援設備與平台

### 1. 支援的音響與接收端
* **Apple 系列**：HomePod (第 1、2 代)、HomePod mini、Apple TV 4K、Mac 電腦。
* **AirPlay 相容音響**：Sonos、Bose、Denon、Marantz、KEF、Devialet 等支援 AirPlay / AirPlay 2 之設備。
* **DLNA / UPnP 設備**：支援標準 DLNA 渲染器的智慧音響。

### 2. 常用影音 App 支援情況
| 類別 | 代表應用程式 | 支援狀態 |
| :--- | :--- | :---: |
| **音樂串流** | Spotify、KKBOX、YouTube Music、Apple Music、Tidal | 完整支援 |
| **影音平台** | YouTube、Twitch、Bilibili、四季線上、LiTV | 完整支援 |
| **家庭劇院 / 本地影音** | Kodi、VLC、Plex、Nova Video Player、Emby | 完整支援 |
| **商業版權保護串流** | Netflix、Disney+、Apple TV+、Prime Video | 受 Android 硬體 DRM (Widevine L1) 保護限制，暫無法透過軟體截取 |

---

## 下載與安裝指南

### 方法一：隨身碟安裝（最推薦）
1. 前往本專案的 [Releases 頁面](https://github.com/lancer1234/AirPulse/releases/latest) 下載最新版的 `AirPulse-vX.X.X.apk`。
2. 將 APK 檔案複製到 USB 隨身碟。
3. 將隨身碟插入電視或投影機的 USB 連接埠。
4. 在電視上開啟「檔案管理員」（若無可至 Google Play 商店下載任何免費檔案管理工具），點選該 APK 進行安裝。

### 方法二：透過電視瀏覽器直接下載
1. 在電視上開啟瀏覽器（如 Downloader 或 TV Bro）。
2. 輸入下載連結直接下載 APK 並依照螢幕指示完成安裝。

### 方法三：透過 ADB 命令列安裝（進階用戶）
```bash
adb connect <電視或投影機IP>:5555
adb install -r AirPulse-v1.0.0.apk
```

---

## 快速使用教學

### 第一步：連接至同一 Wi-Fi
請確認您的 Android TV / 投影機，與 Apple HomePod 連接在**同一個家用 Wi-Fi 網路**（建議使用 5GHz 頻段以獲得最佳穩定度）。

### 第二步：開啟 AirPulse 並選擇設備
1. 在電視主畫面開啟 **AirPulse TV**。
2. 畫面會自動搜尋區域網路內的 AirPlay 與 HomePod 設備。
3. 使用遙控器方向鍵選取您的 HomePod 並按下確認鍵，連線成功後即可開始享受家庭劇院級音質。

### 第三步：啟用遙控器音量接管（選用，強烈推薦）
若希望使用實體遙控器的音量鍵直接增減 HomePod 音量：
1. 進入電視「系統設定」➔「系統」➔「協助工具（或無障礙設定）」。
2. 找到「**AirPulse TV**」並將其開關切換為**開啟**。
3. 完成！現在只要按遙控器上的音量鍵，電視畫面上方將顯示 AirPulse 專屬音量面板，直接微調 HomePod 音量。

---

## 常見問題 (FAQ)

#### Q1：聲音會不會延遲？
AirPulse 採用本地 48kHz 原生採樣與動態連續相位重採樣演算法，針對區域網路封包進行極低延遲傳輸優化，觀看影片對嘴自然流暢。

#### Q2：為什麼需要「協助工具（無障礙）」權限？
Android 系統的實體遙控器音量鍵預設由電視系統獨佔。若未開啟輔助功能，按下音量鍵只會調整電視內部喇叭；開啟「AirPulse TV」輔助功能後，系統才能將按鍵事件轉給 AirPulse 即時傳送給 HomePod。

#### Q3：為什麼 Netflix 沒有聲音或播放錯誤？
Netflix、Disney+ 等商業串流平台採用了硬體級數位版權保護（Widevine L1 / TEE），Android 系統基於版權規範會全面封鎖第三方應用程式對受保護音訊進行錄製。觀看 YouTube、串流音樂及私人本地影片則不受影響。

---

## 開源許可證

本專案採用 **[MIT License](LICENSE)** 開源授權，由 [MAKOTO LAB](https://makotolab.tw) 維護與開發。
歡迎提交 Issue、建議或 Pull Request！
