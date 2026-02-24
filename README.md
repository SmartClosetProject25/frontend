# SmartCloset Frontend (Android)

## 概要

SmartClosetは、AIを活用した服管理・コーディネート提案アプリです。  
本リポジトリは Androidアプリ（フロントエンド）です。

バックエンド（Flask + MySQL）と通信し、以下の機能を提供します。

- 衣類一覧表示
- お気に入り登録
- AIコーディネート提案
- 天気連携コーデ提案
- CameraXによる撮影
- ユーザー嗜好ベクトル反映

---

## 技術スタック

- Kotlin 2.x
- Jetpack Compose
- CameraX
- Retrofit
- Coroutines
- Material3
- OpenWeatherMap API（バックエンド経由）

---

## システム構成

Android App  
↓  
Flask API Server  
↓  
MySQL  
↓  
Gemini API / Vertex AI / Weather API  

---

## 開発環境

- Android Studio Narwhal 4 Feature Drop | 2025.1.4
- Gradle 8.6 以上
- Kotlin 1.9.22（Serialization Plugin使用）
- Jetpack Compose Compiler 1.5.4
- Java 17
- compileSdk: 36
- targetSdk: 36
- minSdk: 33

---

## 主な画面構成
- ログイン画面
- ホーム画面
- 衣類一覧画面
- 衣類詳細画面
- 登録（撮影）／編集画面
- AI提案画面
- 撮影（CameraX）画面
- 設定画面

---

## 実行方法
1. Android Studio Narwhal 4 Feature Drop | 2025.1.4以上のアンドロイドスタジオでプロジェクトを開く
2. Sync Projectする
3. アプリを実行
    実機（同一Wi-Fi環境）またはAndroid Emulatorを選択し、Runを押す。
---

## 注意事項

バックエンドが起動していないと正常に動作しません。
同一Wi-Fi環境内で接続してください。
実機テスト推奨（カメラ機能使用のため）
カメラ・位置情報の権限を許可してください。

---


