# SubscriptionReviewApp

画面仕様書 `sample/app-design.html` を元にした、サブスク管理 Android アプリの最小実装です。

## 実装画面

- S01 Home / Cost Review
- S02 サービス選択
- S03 プラン選択
- S05 サブスク詳細 / 金額編集
- S07 Review
- S08 Settings

## ビルド

このプロジェクトは Gradle なしで、Android SDK 付属ツールから debug APK を作成します。

```powershell
cd C:\LLM\codex\androidapp\SubscriptionReviewApp
.\build.ps1
```

生成物:

```text
dist\subscription-review-debug.apk
```

Android SDK は `%LOCALAPPDATA%\Android\Sdk` または `ANDROID_HOME` を参照します。
