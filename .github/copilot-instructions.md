# Copilot Instructions

## Project Overview

Changjie IME is a legacy Android Cangjie (倉頡) Chinese input method. It is built as an Android `InputMethodService` using the **Ant build system** (not Gradle). Package: `com.linkomnia.android.Changjie`. Target SDK: `android-10`.

## Build

Requires Android SDK with `local.properties` pointing to it. Generate with:
```
android update project -p .
```

Build commands:
```
ant debug      # debug APK
ant release    # release APK (requires signing config in ant.properties)
ant clean      # clean build output
```

There are no automated tests in this project.

## Architecture

Key event flow:
1. `ChangjieIME` (`InputMethodService`) receives key events via `onKey()`
2. For Chinese mode, alpha keys (a–z) are accumulated in `charbuffer[]` (max 5, or 2 in Quick mode) via `typingStroke()`
3. Each keystroke triggers `WordProcessor.getChineseWordDictArrayList(key)` — a SQLite `GLOB` prefix query on the bundled `changjie.db`
4. Results populate `CandidateView`; user selection calls `onChooseWord()`, which commits the character and looks up follow-on phrases in `chinesePhraseDict`

### Class responsibilities

| Class | Role |
|---|---|
| `ChangjieIME` | Main `InputMethodService`; owns stroke buffer, coordinates all components |
| `IMESwitch` | Manages keyboard mode transitions (Chinese ↔ English ↔ Symbol variants) |
| `WordProcessor` | Character/phrase lookup; queries SQLite DB and holds in-memory phrase dict |
| `ChangjieDatabaseHelper` | Copies `changjie.db` from `assets/` to the app's files dir on first run, opens read-only |
| `CandidateView` | Horizontal scrolling candidate bar; calls back to `ChangjieIME.onChooseWord()` |
| `IMEKeyboardView` / `IMEKeyboard` | Custom keyboard view and layout extending Android's built-in classes |

### Data sources

- **`assets/changjie.db`** — SQLite database with `chars` and `codes` tables. Copied to `getFilesDir()/changjie.db` on first launch. Queried with `GLOB` (e.g., `"ab*"`) for prefix matching. Join: `chars INNER JOIN codes ON chars._id = codes._id`.
- **`res/raw/tsin.ser`** — Serialized `ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>>` for the phrase/bigram dictionary. Loaded asynchronously via `AsyncTask` at startup.

### Keyboard layouts

Defined as XML in `res/xml/`: `changjie.xml`, `qwert.xml`, `symbols_en.xml`, `symbols_en_shift.xml`, `symbols_ch.xml`, `simley.xml`. Each layout is loaded as an `IMEKeyboard` instance in `IMESwitch`.

## Key Conventions

### Cangjie code mapping
Letters `a`–`z` map to Cangjie radicals via `WordProcessor.cangjie_radicals[]` (index = `char - 'a'`). `translateToChangjieCode(String)` converts a stroke string to display radicals shown in the composing text area.

### SharedPreferences keys
| Key | Effect |
|---|---|
| `setting_quick` | Quick Changjie mode (2-key max, frequency-sorted results); toggled by Shift/CapsLock on the Chinese keyboard |
| `setting_version_5` | Use Changjie version 5 codes instead of default version 3 |
| `setting_filter_simplify` | Include Simplified Chinese/Kanji characters in results |
| `setting_changjie_tradition` | Traditional exact-match mode (no prefix GLOB, frequency order only) |

### Stroke buffer
`charbuffer[5]` + `strokecount` in `ChangjieIME`. Max length is 5 normally, 2 in Quick mode. Always call `strokereset()` after committing a character or on input field focus change.

### Database query pattern
Always pass `version` and `GLOB key` as positional args; apply the filter string `defaultChangjieFilter` (big5/hkscs/punct/zhuyin/katakana/hiragana/symbol flags) to control which character sets appear. Result limit is hardcoded to `"100"`.
