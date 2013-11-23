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

import android.content.Context;
import android.inputmethodservice.Keyboard;

public class IMEKeyboard extends Keyboard {
    
    static final int KEYCODE_ENTER = 10;
    static final int KEYCODE_CAPLOCK = -200;
    static final int KEYCODE_MODE_CHANGE_CHAR = -300;
    static final int KEYCODE_MODE_CHANGE_SIMLEY = -400;
    static final int KEYCODE_MODE_CHANGE_CHSYMBOL = -500;
    static final int KEYCODE_MODE_CHANGE_LANG = -600;
    
    private boolean isCapLock;
    
    public IMEKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
        this.isCapLock = false;
        this.setShifted(false);
    }

    public IMEKeyboard(Context context, int layoutTemplateResId,
            CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns,
                horizontalPadding);
        this.isCapLock = false;
        this.setShifted(false);
    }
    
    public boolean isCapLock() {
        return this.isCapLock;
    }
    
    public void setCapLock(boolean b) {
        this.isCapLock = b;
        this.setShifted(b);
    }
}
