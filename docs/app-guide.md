# サブスク管理アプリ 解説書

## 概要

本アプリは、月額・年額などのサブスクリプション契約を一覧化し、月あたりの支出、見直し候補、登録済みサービスの詳細を確認するための Android アプリです。

画面仕様書 `app-design.html` を元に、Cost Editorial 風の落ち着いた UI トーンで実装しています。サービス追加、プラン選択、金額編集、レビュー、設定確認の主要導線を備えています。

## GitHub

https://github.com/thunderbird00809/subscription-review-app

## 主な機能

| 機能 | 内容 |
|---|---|
| ホーム | 今月のサブスク支出、見直し候補、登録済みサブスク一覧を表示 |
| サブスク追加 | Netflix、Spotify、YouTube、Adobe、iCloud、Canva などのサービスを選択 |
| プラン選択 | 月額・年額プランを選び、月あたりコストを確認 |
| 詳細表示 | 登録済みサブスクの価格、カテゴリ、請求情報を確認 |
| 金額編集 | 実際の請求額に合わせて登録価格を上書き |
| レビュー | 重複カテゴリや削減候補を確認 |
| 設定 | 通貨、通知、カタログ更新日、バックアップ項目を表示 |

## 画面構成

| 画面ID | 画面名 | 説明 |
|---|---|---|
| S01 | Home / Cost Review | 月次支出と主要サブスクを確認するトップ画面 |
| S02 | サブスク追加 | 登録するサービスを選択する画面 |
| S03 | プラン選択 | 選択サービスのプランと月あたりコストを確認する画面 |
| S05 | サブスク詳細 | 登録済みサービスの詳細と価格編集導線を表示 |
| S07 | Review | 見直し候補や削減見込みを表示 |
| S08 | Settings | アプリ設定項目を表示 |

## デザイン方針

| 項目 | 内容 |
|---|---|
| トーン | Cost Editorial |
| 背景色 | 紙面風の淡いベージュ |
| メインカラー | 黒に近いインクカラー |
| アクセント | ラスト、セージ、ブランドカラー |
| カード | 角丸 8dp 相当の控えめなカード |
| アイコン | サービスごとのブランドアイコン風タイル |
| ナビゲーション | Home / Spend / Review / Me の下部ナビ |

## サービスアイコン

アプリ内では、各サービスを角丸正方形のアイコンタイルで表現しています。

| サービス | 表現 |
|---|---|
| Netflix | 黒背景に赤い Netflix 風アイコン |
| Spotify | 黒背景に緑の Spotify 風アイコン |
| YouTube | 赤背景に白い再生アイコン |
| Adobe | 赤背景に白い Adobe 風アイコン |
| iCloud | 青背景に白いクラウドアイコン |
| Canva | ターコイズ背景に Canva 風アイコン |
| Disney+ | 青背景に D+ 表記 |

注記: 公式アイコン素材がプロジェクト配下に提供されていなかったため、取得可能なものは SVG ベースの VectorDrawable として実装しています。完全一致が必要な場合は、公式 PNG/SVG 素材を追加して差し替えます。

## 技術構成

| 項目 | 内容 |
|---|---|
| 対象 | Android |
| 実装言語 | Java |
| UI | ネイティブ Android View |
| ビルド方式 | Gradle なし、Android SDK 付属ツールで APK 生成 |
| パッケージ名 | `com.example.subscriptionreview` |
| minSdk | 23 |
| targetSdk | 33 |

## 主要ファイル

| ファイル | 説明 |
|---|---|
| `AndroidManifest.xml` | アプリ定義、Activity、SDK バージョン |
| `src/com/example/subscriptionreview/MainActivity.java` | 画面描画、状態管理、サンプルデータ、画面遷移 |
| `res/drawable/*.xml` | サービスアイコン用 VectorDrawable |
| `res/values/strings.xml` | アプリ名 |
| `res/values/styles.xml` | アプリテーマ |
| `build.ps1` | APK 生成用 PowerShell スクリプト |
| `.gitignore` | build、dist、keystore、スクリーンショットなどを除外 |

## ビルド手順

PowerShell で以下を実行します。

```powershell
cd C:\LLM\codex\androidapp\SubscriptionReviewApp
.\build.ps1
```

生成される APK:

```text
C:\LLM\codex\androidapp\SubscriptionReviewApp\dist\subscription-review-debug.apk
```

## 起動手順

エミュレータを起動します。

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Pixel_3a_API_33_x86_64
```

別の PowerShell でアプリを起動します。

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n com.example.subscriptionreview/.MainActivity
```

APK を再インストールする場合:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r dist\subscription-review-debug.apk
```

## 実装済みの画面遷移

| 操作 | 遷移先 |
|---|---|
| Home の追加ボタン | サブスク追加画面 |
| サービス選択 | プラン選択画面 |
| プラン設定 | Home に戻り一覧へ追加 |
| サブスク一覧タップ | サブスク詳細画面 |
| 詳細の金額編集 | 金額入力ダイアログ |
| 下部ナビ Review | Review 画面 |
| 下部ナビ Me | Settings 画面 |

## 検証状況

| 項目 | 結果 |
|---|---|
| APK ビルド | 成功 |
| APK 署名検証 | 成功 |
| エミュレータ起動 | 成功 |
| アプリ起動 | 成功 |
| ホーム画面表示 | 確認済み |
| サブスク追加画面表示 | 確認済み |

## 今後の改善候補

| 項目 | 内容 |
|---|---|
| 公式アイコン差し替え | 各サービスの公式素材を配置し、完全一致にする |
| データ永続化 | SharedPreferences または SQLite / Room に保存 |
| サービス検索 | 入力キーワードでサービス一覧を絞り込み |
| 通知 | 請求日前通知を Android 通知として実装 |
| エクスポート | 登録サブスク一覧の CSV 出力 |
| 価格カタログ | サービス別の標準価格データ更新機能 |

## 補足

このアプリは、画面仕様書に含まれる主要モックを元にしたプロトタイプです。現時点ではローカルのサンプルデータで動作し、サーバー連携や外部 API 連携は含んでいません。
