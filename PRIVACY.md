# 隱私權政策 / Privacy Policy

**生效日期：2026-10-27 · 適用：AirPulse（Android / Google TV）**

> English summary follows the Traditional Chinese text below.

AirPulse 是一款在區域網路內將系統音訊串流至接收設備（Apple AirPlay / HomePod / DLNA 等）的開源工具應用程式。我們將隱私與安全性列為首要原則：**AirPulse 無任何遠端伺服器，不收集、不上傳、不出售使用者的任何個人資料。**

## 1. 我們不收集任何個人資料

* ❌ 不收集帳號、位置、通訊錄、設備識別碼用於追蹤。
* ❌ 無任何第三方廣告、分析或埋點 SDK。
* ❌ 音訊內容即時點對點傳輸，不寫入磁碟，不經過任何第三方雲端伺服器。

## 2. 系統權限與用途

| 權限 | 用途 | 資料去向 |
| :--- | :--- | :--- |
| **音訊擷取** (`RECORD_AUDIO` / `MEDIA_PROJECTION`) | 擷取系統音訊以即時傳輸至 HomePod / AirPlay 音響 | 僅經由區域網路發送至您選定的接收設備，不儲存、不上傳 |
| **輔助功能** (`AccessibilityService`) | 攔截實體遙控器獨立音量鍵，直接控制 HomePod 音量 | 僅於本機攔截按鍵事件，不讀取、不儲存任何螢幕內容 |
| **網路權限** (`INTERNET` / `ACCESS_WIFI_STATE`) | 透過 mDNS 在區域網路內搜尋 AirPlay / DLNA 設備 | 僅於區域網路內通訊 |
| **前台服務** (`FOREGROUND_SERVICE`) | 確保音訊串流在背景時不被系統休眠中斷 | 本機運作 |

## 3. 唯一的對外網路請求：檢查更新

設定頁面的「檢查更新」僅會向 GitHub Releases 公開 API 查詢最新版本號，不附帶任何設備或個人識別資訊。

## 4. 聯絡與支援

問題回報與建議，請前往 GitHub Issues 或聯繫：**danny950811@gmail.com**

---

## English Summary

AirPulse streams system audio to receivers (Apple HomePod / AirPlay / DLNA) **within your local area network only**. It has **no remote servers and collects, uploads, or sells zero personal data**. There are no analytics, ads, or tracking SDKs included.
