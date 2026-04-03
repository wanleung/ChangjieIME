/*
    Changjie Chinese Input Method for Android
    Copyright (C) 2012 Wanleung's Workshop

    Author: Wan Leung Wong (info@wanleung.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.wanleung.android.Changjie;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Manages switching between keyboard layout modes (Changjie, QWERTY, symbols, etc.).
 * Rewritten to use {@link KeyboardLayout} instead of the removed android.inputmethodservice.Keyboard.
 */
public class IMESwitch {

    private KeyboardLayout currentKeyboard;
    private final KeyboardLayout englishKeyboard;
    private final KeyboardLayout enNumberSymbolKeyboard;
    private final KeyboardLayout enSymbolShiftKeyboard;
    private final KeyboardLayout chSymbolKeyboard;
    private final KeyboardLayout chineseKeyboard;
    private final KeyboardLayout smileyKeyboard;

    /** Tracks whether we were in Chinese mode before switching to symbols. */
    private boolean isFromChinese;

    private final SharedPreferences sharedPrefs;

    /**
     * Creates a new IMESwitch and initialises all keyboard layouts.
     *
     * @param ctx application context
     */
    public IMESwitch(Context ctx) {
        chineseKeyboard = KeyboardLayout.createChangjie();
        englishKeyboard = KeyboardLayout.createQwerty();
        enNumberSymbolKeyboard = KeyboardLayout.createSymbolsEn();
        enSymbolShiftKeyboard = KeyboardLayout.createSymbolsEnShift();
        chSymbolKeyboard = KeyboardLayout.createSymbolsCh();
        smileyKeyboard = KeyboardLayout.createSmiley();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        chineseKeyboard.setCapLock(sharedPrefs.getBoolean("setting_quick", false));
    }

    /**
     * Initialises or resets to the default keyboard (Changjie Chinese).
     * Called at the start of each input session.
     */
    public void init() {
        if (currentKeyboard == null) {
            currentKeyboard = chineseKeyboard;
        }
    }

    /**
     * Returns the currently active keyboard layout.
     *
     * @return current keyboard layout
     */
    public KeyboardLayout getCurrentKeyboard() {
        return currentKeyboard;
    }

    /** Returns true if the Changjie Chinese keyboard is active. */
    public boolean isChinese() {
        return chineseKeyboard == currentKeyboard;
    }

    /** Returns true if the QWERTY English keyboard is active. */
    public boolean isEnglish() {
        return englishKeyboard == currentKeyboard;
    }

    /** Returns true if the English number/symbol keyboard is active. */
    public boolean isNumberSymbol() {
        return enNumberSymbolKeyboard == currentKeyboard;
    }

    /** Returns true if the English shifted symbol keyboard is active. */
    public boolean isSymbol() {
        return enSymbolShiftKeyboard == currentKeyboard;
    }

    /** Returns true if the Chinese symbol keyboard is active. */
    public boolean isChineseSymbol() {
        return chSymbolKeyboard == currentKeyboard;
    }

    /** Returns true if the smiley/emoji keyboard is active. */
    public boolean isSmiley() {
        return smileyKeyboard == currentKeyboard;
    }

    /**
     * Returns true if the current keyboard is a non-character keyboard
     * (symbols, Chinese symbols, or smiley).
     */
    public boolean isNotCharKeyboard() {
        return isNumberSymbol() || isSymbol() || isChineseSymbol() || isSmiley();
    }

    /**
     * Handles a special key code and switches keyboard mode if applicable.
     *
     * @param keyCode the key code to handle
     * @return true if the key code was handled (mode switch occurred), false otherwise
     */
    public boolean handleKey(int keyCode) {
        boolean result = false;
        switch (keyCode) {
            case KeyboardLayout.KEYCODE_CAPLOCK: {
                currentKeyboard.setCapLock(!currentKeyboard.isCapLock());
                if (isChinese()) {
                    SharedPreferences.Editor edit = sharedPrefs.edit();
                    edit.putBoolean("setting_quick", currentKeyboard.isCapLock());
                    edit.apply();
                }
                result = true;
                break;
            }
            case KeyboardLayout.KEYCODE_SHIFT: {
                if (currentKeyboard.isCapLock()) {
                    currentKeyboard.setCapLock(false);
                    if (isChinese()) {
                        SharedPreferences.Editor edit = sharedPrefs.edit();
                        edit.putBoolean("setting_quick", false);
                        edit.apply();
                    }
                } else {
                    currentKeyboard.setShifted(!currentKeyboard.isShifted());
                    if (isChinese()) {
                        SharedPreferences.Editor edit = sharedPrefs.edit();
                        edit.putBoolean("setting_quick", currentKeyboard.isShifted());
                        edit.apply();
                    }
                }
                if (isNotCharKeyboard()) {
                    switchBetweenSymbolShift();
                }
                result = true;
                break;
            }
            case KeyboardLayout.KEYCODE_MODE_CHANGE_CHAR: {
                switchToCharKeyboard();
                result = true;
                break;
            }
            case KeyboardLayout.KEYCODE_MODE_CHANGE: {
                switchToNumberSymbol();
                result = true;
                break;
            }
            case KeyboardLayout.KEYCODE_MODE_CHANGE_CHSYMBOL: {
                switchToChineseSymbol();
                result = true;
                break;
            }
            case KeyboardLayout.KEYCODE_MODE_CHANGE_LANG: {
                switchLanguage();
                result = true;
                break;
            }
            case KeyboardLayout.KEYCODE_MODE_CHANGE_SIMLEY: {
                switchToSmiley();
                result = true;
                break;
            }
            default: {
                result = false;
            }
        }
        return result;
    }

    /**
     * Switches to a character keyboard (Changjie or QWERTY).
     * If already on a character keyboard, toggles between Chinese and English.
     */
    public void switchToCharKeyboard() {
        if (isNotCharKeyboard()) {
            if (isFromChinese) {
                currentKeyboard = chineseKeyboard;
            } else {
                currentKeyboard = englishKeyboard;
            }
        } else {
            if (isChinese()) {
                currentKeyboard = englishKeyboard;
            } else {
                currentKeyboard = chineseKeyboard;
            }
        }
        currentKeyboard.setCapLock(false);
    }

    /**
     * Switches to or cycles through the symbol keyboards.
     */
    public void switchToNumberSymbol() {
        if (isNotCharKeyboard()) {
            if (isNumberSymbol()) {
                currentKeyboard = enSymbolShiftKeyboard;
            } else if (currentKeyboard == enSymbolShiftKeyboard) {
                currentKeyboard = chSymbolKeyboard;
            } else {
                currentKeyboard = enNumberSymbolKeyboard;
            }
        } else {
            isFromChinese = isChinese();
            currentKeyboard = enNumberSymbolKeyboard;
        }
        currentKeyboard.setCapLock(false);
    }

    /**
     * Switches between number-symbol and shifted-symbol keyboards based on shift state.
     */
    public void switchBetweenSymbolShift() {
        if (currentKeyboard.isShifted()) {
            currentKeyboard = enSymbolShiftKeyboard;
            currentKeyboard.setCapLock(true);
        } else {
            currentKeyboard = enNumberSymbolKeyboard;
            currentKeyboard.setCapLock(false);
        }
    }

    /**
     * Switches to the Chinese fullwidth symbol keyboard.
     */
    public void switchToChineseSymbol() {
        isFromChinese = isChinese();
        currentKeyboard = chSymbolKeyboard;
        currentKeyboard.setCapLock(false);
    }

    /**
     * Switches between Chinese and English character keyboards.
     */
    public void switchLanguage() {
        if (isNotCharKeyboard()) {
            if (isFromChinese) {
                currentKeyboard = englishKeyboard;
            } else {
                currentKeyboard = chineseKeyboard;
            }
        } else {
            if (isEnglish()) {
                currentKeyboard = chineseKeyboard;
            } else {
                currentKeyboard = englishKeyboard;
            }
        }
    }

    /**
     * Switches to the smiley/emoji keyboard.
     */
    public void switchToSmiley() {
        isFromChinese = isChinese();
        currentKeyboard = smileyKeyboard;
        currentKeyboard.setCapLock(false);
    }
}
