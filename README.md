# SpaceShipGame — README.md

## 專案名稱與簡介

* **專案名稱**：星際大戰--超級3D版
* **簡介**：
在第一人稱視角中駕駛飛船在3D宇宙中與敵人交戰，賺取分數、金錢並升級飛船來獲得更多分數。

---

## 組員資訊（學號與姓名）

| 學號      | 姓名   |
| ------- | ------- |
| B11207014 | 江元宏 |
| B11207029 | 闕辰峻 |

---

## 分工說明

| 分工項目    | 主要負責人   | 內容摘要 |
| ------- | ------- | --------------- |
| 遊戲核心程式 | 江元宏 | 飛船進退、碰撞偵測、wave系統、停滯後退模式|
| 遊戲核心程式 | 闕辰峻 | 子彈碰撞邏輯、子彈飛行軌跡繪製 |
| 美術與 UI  | 江元宏 | 背景星空、選單設定、血條雷達 |
| 美術與 UI  | 闕辰峻 | 駕駛艙圖片、血條雷達位置微調 |
| 音效與 BGM | 闕辰峻 | 星際氛圍音樂與音效 |
| 測試與整合 | 全體 | Bug 修復、效能優化 |

---

## 報告影片連結

* [點此觀看報告影片](https://youtu.be/0yFFdqXaYN0)

---

## 遊戲操作說明與特色介紹

### 操作鍵位

| 動作    | 鍵位 |
| ------- | ------- |
|  上       |  W   |
|  下       |  S   |
|  左       |  A   |
|  右       |  D   |
|  商店     |  B   |
|  暫停     |  ESC |
| 駕駛艙切換|  C   |
|  懸停     |  Q   |
|  後退     |  E   |


### 遊戲特色

1. **偽 3D 視差星空**：多層次背景滑動，營造深度感。
2. **多波次敵人挑戰自我**：敵人一波接著一波，讓你措手不及。
3. **警告、雷達系統**：真實警告、雷達讓你倘若真的在開戰艦。
4. **視窗模式切換**：支援全螢幕與無邊框視窗啟動參數。
5. **原創 BGM 與音效**：星際風格配樂，讓你深入其境。

---

## 執行方式

### 系統需求

* **作業系統**：Windows／macOS／Linux
* **Java**：JDK 24 以上

### 下載與運行

1. 下載 `SSG.jar`。
2. 開啟終端機執行：

   ```bash
   java -jar SSG.jar              # 預設全螢幕
   java -jar SSG.jar   --windowed   # 一般視窗
   java -jar SSG.jar   --borderless # 無邊框
   ```

---

## 與 ChatGPT 的協作紀錄摘要



2025/05/02

2D Java SpaceShipGame pseudo‑3D 升級

ParallaxLayer 多層視差演算法

2025/05/05

畫面加入駕駛艙圖片、子彈碰撞與敵彈座標

駕駛艙 PNG、Bullet 路徑計算修正

2025/05/08

星雲背景：銀河感 + 星體大小不一 + 漂移

Star 隨機大小與速度、背景漂移效果

2025/05/12

生成星際感 BGM（加入鼓點）

bgm.mp3 與音量控制程式碼

2025/05/12

Bullet 距離縮放、Q/E 懸停/倒退功能

Bullet.update() 線性縮放、Hover/Reverse 模式

2025/05/14

子彈方向修正

Bullet 初始化方向與 Player 面向判斷修正

2025/05/15

選單新增全螢幕 / 無邊框視窗模式

SettingsMenu 視窗模式切換


## UML 類別圖 (Class Diagram)

![spaceshipgame_uml_full](https://github.com/user-attachments/assets/d0915e38-7005-4aa5-909d-48291da06d54)


## 流程圖 (Flow Chart)

![SpaceShipGame_Flow_Detailed](https://github.com/user-attachments/assets/0cf5c8a5-6fb6-4cec-a953-c40f8ddeb17d)

## 序列圖 (Sequence Diagram)

![SpaceShipGame_AllFlows](https://github.com/user-attachments/assets/78416ea5-518e-46c5-b849-96c24ca14b05)

