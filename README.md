# AirPulse — Android ➔ Apple HomePod & AirPlay 2 無線音訊橋樑

> **Android 手機、平板與 Android TV / Google TV 直連 Apple HomePod / AirPlay 2 的超低延遲無線音訊中繼器。**  
> 一鍵將 Android 設備上的音樂、影片與遊戲聲音，無損傳輸至客廳的 HomePod、HomePod mini 與 AirPlay 音響設備。

Developed independently by **[MAKOTO LAB](https://makotolab.tw/)**.

<p>
  <a href="https://github.com/lancer1234/AirPulse/releases/latest"><img alt="Download AirPulse APK" src="https://img.shields.io/badge/Download-Latest%20APK-2ea44f?style=for-the-badge&logo=android&logoColor=white"></a>
  <a href="https://buymeacoffee.com/MakotoLab"><img alt="Support MAKOTO LAB on Buy Me a Coffee" src="https://img.shields.io/badge/Support-MAKOTO%20LAB-FFDD00?style=for-the-badge&logo=buymeacoffee&logoColor=000000"></a>
  <a href="https://makotolab.tw/"><img alt="MAKOTO LAB" src="https://img.shields.io/badge/Studio-MAKOTO%20LAB-000000?style=for-the-badge&logoColor=white"></a>
  <a href="https://www.instagram.com/d.wang___/"><img alt="Instagram @d.wang___" src="https://img.shields.io/badge/Instagram-%40d.wang______-E4405F?style=for-the-badge&logo=instagram&logoColor=white"></a>
  <a href="LICENSE"><img alt="MIT License" src="https://img.shields.io/badge/License-MIT-555?style=for-the-badge"></a>
</p>

---

## 為什麼需要 AirPulse？

許多人擁有音質優異的 **Apple HomePod** 或 **AirPlay 頂級音響**，但手邊的手機是 **Android**，或是客廳的電視、投影機是 **Android TV / Google TV** 系統，兩者處於封閉的生態系而無法互相連線。

**AirPulse** 是專為打破生態藩籬所打造的跨平台開源音訊工具，全方位支援**手機**與**電視**雙端情境：

### 📱 Android 手機與平板（隨行聽音樂、打遊戲）
* **一鍵推流至 HomePod**：在手機上播放 Spotify、KKBOX、YouTube、Podcast 或玩手遊時，只要在 AirPulse 輕輕一點，音訊立刻無損直達 HomePod。
* **原生 iOS 風格卡片介面**：直向螢幕自動呈現清爽簡潔的卡片設計，直覺流暢。
* **背景持續穩定播放**：鎖定螢幕或切換其他 App 時，音訊持續無縫串流，不斷流、不卡頓。

### 📺 Android TV / Google TV / 投影機（客廳家庭劇院）
* **實體遙控器音量接管**：直接按下電視或投影機遙控器上的**獨立音量鍵**，即可直接調整 HomePod 音量，電視本機喇叭維持靜音。
* **串流時本機自動靜音**：串流開始時自動將電視內部喇叭靜音，徹底消除聲音疊加與雜音回音。
* **Apple tvOS 質感極簡介面**：橫向大螢幕自動切換為 Apple tvOS 雙欄「深空極光（Aurora）」流動玻璃質感，遙控器方向鍵（D-pad）焦點反應行雲流水。

---

## 支援設備與平台

### 1. 支援的接收端音響
* **Apple 系列**：HomePod (第 1、2 代)、HomePod mini、Apple TV 4K、Mac 電腦。
* **AirPlay 相容音響**：Sonos、Bose、Denon、Marantz、KEF、Devialet 等支援 AirPlay / AirPlay 2 之設備。
* **DLNA / UPnP 設備**：支援標準 DLNA 音訊渲染器的智慧音響。

### 2. 常用影音 App 支援情況
| 類別 | 代表應用程式 | 支援狀態 |
| :--- | :--- | :---: |
| **音樂串流** | Spotify、KKBOX、YouTube Music、Apple Music、Tidal | 完整支援 |
| **影音平台** | YouTube、Twitch、Bilibili、四季線上、LiTV | 完整支援 |
| **家庭劇院 / 本地影音** | Kodi、VLC、Plex、Nova Video Player、Emby | 完整支援 |
| **商業版權保護串流** | Netflix、Disney+、Apple TV+、Prime Video | 受 Android 硬體 DRM (Widevine L1) 保護限制，暫無法透過軟體截取 |

---

## 下載與安裝指南

前往 [Releases 頁面](https://github.com/lancer1234/AirPulse/releases/latest) 下載最新版 `AirPulse-vX.X.X.apk`：

* **Android 手機 / 平板**：直接在手機瀏覽器點擊下載並點選安裝即可。
* **Android TV / 投影機**：
  * **方法 A（隨身碟）**：將下載的 APK 放入隨身碟，插入電視或投影機後開啟檔案管理員安裝。
  * **方法 B（電視瀏覽器）**：在電視上下載 Downloader 或 TV Bro 瀏覽器直接輸入下載連結安裝。
  * **方法 C（ADB 指令）**：
    ```bash
    adb connect <電視IP>:5555
    adb install -r AirPulse-v1.0.0.apk
    ```

---

## 快速使用教學

### 第一步：連接至同一 Wi-Fi
請確認您的 Android 設備（手機或電視），與 Apple HomePod 連接在**同一個家用 Wi-Fi 網路**（建議使用 5GHz 頻段以獲得最佳傳輸品質）。

### 第二步：開啟 AirPulse 並選擇設備
1. 開啟 **AirPulse**。
2. 畫面會自動搜尋區域網路內的 AirPlay 與 HomePod 設備。
3. 點選您的 HomePod 開始連線，連線成功後即可享受家庭劇院級震撼音質。

### 第三步：電視用戶開啟遙控器音量接管（強烈推薦）
若在電視或投影機上使用實體遙控器音量鍵直接控制 HomePod：
1. 進入電視「設定」➔「系統」➔「協助工具（或無障礙設定）」。
2. 找到「**AirPulse TV**」並將其切換為**開啟**。
3. 即可直接使用實體遙控器音量鍵微調 HomePod 音量。

---

## 常見問題 (FAQ)

#### Q1：聲音會不會延遲？
AirPulse 採用本地 48kHz 原生採樣與動態連續相位重採樣演算法，針對區域網路封包進行極低延遲傳輸優化，無論手機看短影音或電視觀看影片對嘴均自然流暢。

#### Q2：這套軟體安全嗎？會不會洩漏隱私？
AirPulse 是純本地端工具，**沒有任何伺服器**，不收集、不上傳任何個人資料，也沒有置入任何廣告或統計追蹤 SDK。所有音訊資料均在您的私人區域網路內點對點傳輸。

#### Q3：為什麼需要「協助工具（無障礙）」權限？
Android 系統的實體遙控器音量鍵預設由電視硬體獨佔。開啟「AirPulse TV」輔助功能後，系統才能將遙控器按鍵事件轉給 AirPulse 即時發送給 HomePod。

#### Q4：為什麼 Netflix 沒有聲音或播放錯誤？
Netflix、Disney+ 等商業串流平台採用硬體級數位版權保護（Widevine L1 / TEE），Android 系統基於版權規範會全面封鎖第三方應用程式對受保護音訊進行錄製。觀看 YouTube、串流音樂及私人本地影片則不受影響。

---

## Support MAKOTO LAB

我在業餘時間獨立開發與維護 AirPulse。如果它解決了你在客廳聽音樂、看電視時的痛點，歡迎透過 Buy Me a Coffee 支持後續開發與測試設備採購：

<p>
  <a href="https://buymeacoffee.com/MakotoLab"><img alt="Support MAKOTO LAB on Buy Me a Coffee" src="https://img.shields.io/badge/Support-MAKOTO%20LAB-FFDD00?style=for-the-badge&logo=buymeacoffee&logoColor=000000"></a>
</p>

贊助純屬自願支持，不影響任何功能之免費開源使用。

---

## About MAKOTO LAB

**MAKOTO LAB** 是一間專注於探索硬體潛能、即時數據與非常規互動介面的獨立軟體與硬體工作室。

歡迎探索其他開源與獨立作品：
* **[Miles](https://apps.apple.com/tw/app/miles/id6760163400)**：iPhone 直線加速與駕駛性能紀錄器（已正式登陸 App Store）。
* **[MightyScroll](https://github.com/lancer1234/MightyScroll)**：為 Apple Mighty Mouse 打造的 macOS 選單列捲動加速與慣性微調工具。
* **[Makoto Glass](https://github.com/lancer1234/MakotoGlass-Beta)**：將 iPhone 通知與 Now Playing 媒體控制無縫帶入 Google Glass 的穿戴式介面。
* **[GlyphCheck](https://github.com/lancer1234/GlyphCheck)**：macOS 原生字形核對與老字檔修復工具。

<p>
  <a href="https://makotolab.tw/"><img alt="MAKOTO LAB Website" src="https://img.shields.io/badge/Website-makotolab.tw-000000?style=for-the-badge"></a>
  <a href="https://www.instagram.com/d.wang___/"><img alt="Instagram @d.wang___" src="https://img.shields.io/badge/Instagram-%40d.wang______-E4405F?style=for-the-badge&logo=instagram&logoColor=white"></a>
  <a href="https://buymeacoffee.com/MakotoLab"><img alt="Support MAKOTO LAB on Buy Me a Coffee" src="https://img.shields.io/badge/Support-MAKOTO%20LAB-FFDD00?style=for-the-badge&logo=buymeacoffee&logoColor=000000"></a>
</p>

---

## License

<p>
  <a href="LICENSE"><img alt="View MIT License" src="https://img.shields.io/badge/License-MIT-555?style=for-the-badge"></a>
</p>

[MIT](LICENSE). AirPulse is an independent open-source project by MAKOTO LAB and is not affiliated with Apple, Google, Sonos, or any other trademark holders.
