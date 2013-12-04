/*
    Changjie Chinese Input Method for Android
    Copyright (C) 2012 LinkOmnia Ltd.

    Author: Wan Leung Wong (wanleung@linkomnia.com)

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

package com.linkomnia.android.Changjie;

import com.linkomnia.android.Changjie.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.preference.PreferenceManager;
import android.util.Log;

public class IMESwitch {
    
    private Context ctx;
    
    private IMEKeyboard currentKeyboard;
    private IMEKeyboard englishKeyboard;
    private IMEKeyboard enNumberSymbolKeyboard;
    private IMEKeyboard enSymoblShiftKeyboard;
    private IMEKeyboard chSymoblKeyboard;
    private IMEKeyboard chineseKeyboard;
    private IMEKeyboard simleyKeyboard;
    
    private boolean isFromChinese;
    
    private SharedPreferences sharedPrefs;
    
    public IMESwitch(Context ctx) {
        this.ctx = ctx;
        this.englishKeyboard = new IMEKeyboard(this.ctx, R.xml.qwert);
        this.enNumberSymbolKeyboard = new IMEKeyboard(this.ctx, R.xml.symbols_en);
        this.enSymoblShiftKeyboard = new IMEKeyboard(this.ctx, R.xml.symbols_en_shift);
        this.chSymoblKeyboard = new IMEKeyboard(this.ctx, R.xml.symbols_ch);
        this.chineseKeyboard = new IMEKeyboard(this.ctx, R.xml.changjie);
        this.simleyKeyboard = new IMEKeyboard(this.ctx, R.xml.simley);
        this.sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        this.chineseKeyboard.setCapLock(this.sharedPrefs.getBoolean("setting_quick", false));
    }
    
    public void init() {
        if (this.currentKeyboard == null) {
            this.currentKeyboard = this.chineseKeyboard;
        } else {
            
        }
    }
    
    public IMEKeyboard getCurrentKeyboard() {
        return this.currentKeyboard;
    }
    
    public boolean isChinese() {
        return this.chineseKeyboard == this.currentKeyboard;
    }

    public boolean isEnglish() {
        return this.englishKeyboard == this.currentKeyboard;
    }

    public boolean isNumberSymbol() {
        return this.enNumberSymbolKeyboard == this.currentKeyboard;
    }

    public boolean isSymbol() {
        return this.enSymoblShiftKeyboard == this.currentKeyboard;
    }
    
    public boolean isChineseSymbol() {
        return this.chSymoblKeyboard == this.currentKeyboard;
    }
    
    public boolean isSimley() {
        return this.simleyKeyboard == this.currentKeyboard;
    }
    
    public boolean isNotCharKeyboard() {
        return (this.isNumberSymbol() || this.isSymbol() || this.isChineseSymbol() || this.isSimley());
    }
       
    public boolean handleKey(int keyCode) {
        boolean result = false;
        switch (keyCode) {
            case IMEKeyboard.KEYCODE_CAPLOCK: {
                this.currentKeyboard.setCapLock(! this.currentKeyboard.isCapLock());
                if (this.isChinese()) {
                	SharedPreferences.Editor edit = this.sharedPrefs.edit();
                	edit.putBoolean("setting_quick", this.currentKeyboard.isCapLock());
                	edit.commit();
                }
                result = true;
                break;
            }
            case IMEKeyboard.KEYCODE_SHIFT: {
                if (this.currentKeyboard.isCapLock()) {
                    this.currentKeyboard.setCapLock(false);
                    if (this.isChinese()) {
                    	SharedPreferences.Editor edit = this.sharedPrefs.edit();
                    	edit.putBoolean("setting_quick", false);
                    	edit.commit();
                    }
                } else {
                    this.currentKeyboard.setShifted(! this.currentKeyboard.isShifted());
                    if (this.isChinese()) {
                    	SharedPreferences.Editor edit = this.sharedPrefs.edit();
                    	edit.putBoolean("setting_quick", this.currentKeyboard.isShifted());
                    	edit.commit();
                    }
                }
                
                if (this.isNotCharKeyboard()) {
                    this.switchBetweenSymbolShift();
                }
                result = true; 
                break;
            }
            case IMEKeyboard.KEYCODE_MODE_CHANGE_CHAR: {
                this.switchToCharKeyboard();
                result = true;
                break;
            }
            case IMEKeyboard.KEYCODE_MODE_CHANGE: {
                this.switchToNumberSymbol();
                result = true;
                break;
            }
            case IMEKeyboard.KEYCODE_MODE_CHANGE_CHSYMBOL: {
                this.switchToChineseSymbol();
                result = true;
                break;
            }
            case IMEKeyboard.KEYCODE_MODE_CHANGE_LANG: {
                this.switchLanguage();
                result = true;
                break;
            }
            case IMEKeyboard.KEYCODE_MODE_CHANGE_SIMLEY: {
                this.switchToSimley();
                result = true;
                break;
            }
            default: {
                result = false;
            }
        }
        return result;
    }
    
    public void switchToCharKeyboard() {
        if (this.isNotCharKeyboard()) {
            if (this.isFromChinese) {
                this.currentKeyboard = this.chineseKeyboard;
            } else {
                this.currentKeyboard = this.englishKeyboard;
            }
        } else {
            if (this.isChinese()) {
                this.currentKeyboard = this.englishKeyboard;
            } else {
                this.currentKeyboard = this.chineseKeyboard;
            }
        }
        this.currentKeyboard.setCapLock(false);
    }
    
    public void switchToNumberSymbol() {
        if (this.isNotCharKeyboard()) {
            if (this.isNumberSymbol()) {
                this.currentKeyboard = this.enSymoblShiftKeyboard;
            } else {
                this.currentKeyboard = this.enNumberSymbolKeyboard;
            }
        } else {
            this.isFromChinese = this.isChinese();
            this.currentKeyboard = this.enNumberSymbolKeyboard;
        }
        this.currentKeyboard.setCapLock(false);
    }
    
    public void switchBetweenSymbolShift() {
        if (this.currentKeyboard.isShifted()) {
            this.currentKeyboard = this.enSymoblShiftKeyboard;
            this.currentKeyboard.setCapLock(true);
        } else {
            this.currentKeyboard = this.enNumberSymbolKeyboard;
            this.currentKeyboard.setCapLock(false);
        }
    }
    
    public void switchToChineseSymbol() {
        this.isFromChinese = this.isChinese();
        this.currentKeyboard = this.chSymoblKeyboard;
        this.currentKeyboard.setCapLock(false);
    }
    
    public void switchLanguage() {
        if (this.isNotCharKeyboard()) {
            if (this.isFromChinese) {
                this.currentKeyboard = this.englishKeyboard;
            } else {
                this.currentKeyboard = this.chineseKeyboard;
            }
        } else {
            if (this.isEnglish()) {
                this.currentKeyboard = this.chineseKeyboard;
            } else {
                this.currentKeyboard = this.englishKeyboard;
            }
        }
    }
    
    public void switchToSimley() {
        this.isFromChinese = this.isChinese();
        this.currentKeyboard = this.simleyKeyboard;
        this.currentKeyboard.setCapLock(false);
    }
}
