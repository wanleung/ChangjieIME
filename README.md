# 倉頡鍵盤 Changjie Keyboard

> Cangjie Chinese Input Method for Android

[![Platform](https://img.shields.io/badge/platform-Android%205.0%2B-green)](https://play.google.com/store/apps/details?id=com.wanleung.android.Changjie)
[![Version](https://img.shields.io/badge/version-3.0-blue)](https://play.google.com/store/apps/details?id=com.wanleung.android.Changjie)
[![License](https://img.shields.io/badge/license-GPLv3-orange)](LICENSE)

---

## What's New in Version 3.0

**Version 3.0 — Complete rebuild for modern Android.**  
Fixed candidate bar display, updated keyboard engine for Android 14 compatibility. No data collection — all processing is local and on-device.

**版本 3.0 — 全面重建以支援現代 Android。**  
修復候選字欄顯示問題，更新鍵盤引擎以相容 Android 14。不收集任何資料，所有處理均在本機完成。

---

## Features

- 🀄 Cangjie Version 3 and Version 5 (倉頡第三代及第五代)
- ⚡ Quick Mode / 速成輸入
- 🈳 Simplified Chinese character filter (簡體中文字庫)
- 💬 Phrase suggestions after character selection (詞語聯想)
- 🔒 No internet permission — fully local, no data collection
- 📱 Compatible with Android 5.0 and above (API 21+)

---

## Google Play

[Download on Google Play](https://play.google.com/store/apps/details?id=com.wanleung.android.Changjie)

---

## Build

**Requirements:** Android SDK, JDK 8+

```bash
# Debug build
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Release AAB (for Google Play)
./gradlew bundleRelease
```

Output locations:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

> **Note:** A signed release build requires `keystore.properties` in the project root.
> See `keystore.properties.example` for the required format.

---

## Privacy Policy

This app does **not** collect, store, or transmit any personal data. All text processing happens entirely on your device. See the full [Privacy Policy](app/src/main/assets/privacy_policy.html).

---

## License

Copyright © 2012 Wanleung's Workshop  
Author: Wan Leung Wong — [wanleung.com](https://wanleung.com)  
Contact: [info@wanleung.com](mailto:info@wanleung.com)

This program is free software: you can redistribute it and/or modify it under the terms of the
[GNU General Public License v3](LICENSE) as published by the Free Software Foundation.

