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

import java.util.ArrayList;
import java.util.List;

import com.linkomnia.android.Changjie.R;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class ChangjieIME extends InputMethodService implements
        KeyboardView.OnKeyboardActionListener {
    /** Called when the activity is first created. */
    private IMEKeyboardView inputView;
    private CandidateView candidateView;
    
    //private ChangjieTable stroke5WordDictionary;
    private WordProcessor wordProcessor; 
    
    private IMESwitch imeSwitch;
    
    private char [] charbuffer = new char[5];
    private int strokecount = 0;
    
    private SharedPreferences sharedPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        //stroke5WordDictionary = new ChangjieTable(this, false);
        //stroke5WordDictionary.open();
        this.wordProcessor = new WordProcessor(this);
        this.wordProcessor.init();
        sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
    }

    public void onInitializeInterface() {
        imeSwitch = new IMESwitch(this);
    }

    public View onCreateInputView() {
        inputView = (IMEKeyboardView) getLayoutInflater().inflate(R.layout.main, null);
        inputView.setPreviewEnabled(false);
        inputView.setOnKeyboardActionListener(this);
        return inputView;
    }
    
    public View onCreateCandidatesView() {
        candidateView = (CandidateView) getLayoutInflater().inflate(R.layout.candidates, null);
        candidateView.setDelegate(this);
        return candidateView;
    }

    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        this.strokereset();
        //this.mInputView.closing();
    }
    
    public void onFinishInput() {
        this.strokereset();
        /// TODO: SAVE
        super.onFinishInput();

    }
    
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        this.imeSwitch.init();
        this.inputView.setKeyboard(this.imeSwitch.getCurrentKeyboard());
        this.setCandidatesViewShown(true);
    }
    
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
    }
    
    public void onDisplayCompletions(CompletionInfo[] completions) {
        super.onDisplayCompletions(completions);
    }
    
    public void onKey(int primaryCode, int[] keyCodes) {
        this.wordProcessor.getChinesePhraseDictLinkedHashMap("(");
        if (imeSwitch.handleKey(primaryCode)) {
            this.strokereset();
            this.inputView.setKeyboard(imeSwitch.getCurrentKeyboard());
            return;
        }
        if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            this.handleClose();
            return;
        }
        if (primaryCode == Keyboard.KEYCODE_DELETE) {
            this.handleBackspace();
            return;
        }
        if (primaryCode == IMEKeyboard.KEYCODE_ENTER) {
            this.handleEnter();
        }
        if (primaryCode == Keyboard.KEYCODE_DONE) {
            return;
        }
        this.handleKey(primaryCode, keyCodes);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
    
    public void onPress(int primaryCode) {
        // TODO Auto-generated method stub       
    }

    public void onRelease(int primaryCode) {
        // TODO Auto-generated method stub
    }

    public void onText(CharSequence text) {
        // TODO Auto-generated method stub
    }
    
    public void swipeDown() {
        // TODO Auto-generated method stub
    }

    public void swipeLeft() {
        // TODO Auto-generated method stub
    }

    public void swipeRight() {
        // TODO Auto-generated method stub
    }

    public void swipeUp() {
        // TODO Auto-generated method stub
    }
    
    public void onDestroy() {
        this.inputView.closing();
        //this.stroke5WordDictionary.close();
        super.onDestroy();
    }
    
    private void strokereset() {
        this.charbuffer = new char[5];
        this.strokecount = 0;
        if (this.candidateView != null) {
        	this.updateInputCode(new String(this.charbuffer,0,this.strokecount));
            this.candidateView.updateInputBox(new String(this.charbuffer,0,this.strokecount));
            this.updateCandidates(new ArrayList<String>());
        }
    }
    
    private void handleKey(int keyCode, int[] keyCodes) {
    	if (this.imeSwitch.isChinese() && keyCode == ' ') {
    		if (this.strokecount == 0) {
    			this.handleCharacter(keyCode, keyCodes);
    		} else if (this.strokecount == 1) {
    			this.onChooseWord(WordProcessor.translateToChangjieCode(new String(this.charbuffer,0,this.strokecount)));
    		} else {
    			this.onChooseWord(this.candidateView.getSuggestion().get(0));
    		}
    	} else if (this.imeSwitch.isChinese() && (keyCode >= 'a' && keyCode <= 'z' ) ) {
            this.typingStroke(keyCode);
        } else {
            this.handleCharacter(keyCode, keyCodes);
        }
    }
    
    private void typingStroke(int keycode) {
        char c = (char)keycode;
        int maxKeyNum = 5;
        if (this.sharedPrefs.getBoolean("setting_quick", false)) {
        	maxKeyNum = 2;
        }
        if (this.strokecount < maxKeyNum) {
            this.charbuffer[this.strokecount++] = c;
        }
        this.candidateView.updateInputBox(new String(this.charbuffer,0,this.strokecount));
        this.updateInputCode(new String(this.charbuffer,0,this.strokecount));
        ArrayList<String> words = this.wordProcessor.getChineseWordDictArrayList(new String(this.charbuffer,0,this.strokecount));
        updateCandidates(words);
    }
    
    private void handleBackspace() {
        if (imeSwitch.isChinese()) {
            if (this.strokecount > 1) {
                this.strokecount -= 1;
                this.candidateView.updateInputBox(new String(this.charbuffer,0,this.strokecount));
                this.updateInputCode(new String(this.charbuffer,0,this.strokecount));
                ArrayList<String> words = this.wordProcessor.getChineseWordDictArrayList(new String(this.charbuffer,0,this.strokecount));
                updateCandidates(words);
            } else if (this.strokecount > 0) {
                this.strokereset();
                //this.setCandidatesViewShown(false);
            } else {
                //this.setCandidatesViewShown(false);
                keyDownUp(KeyEvent.KEYCODE_DEL);
                this.strokereset();
            }
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
    }
    
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        this.strokereset();
        if (isInputViewShown()) {
            if (inputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
                inputView.setShifted(!(!imeSwitch.getCurrentKeyboard().isCapLock() && inputView.isShifted()));
            }
        }
        getCurrentInputConnection().commitText(String.format("%c", primaryCode), 1);
    }

    private void handleEnter() {
        this.keyDownUp(KeyEvent.KEYCODE_ENTER);
    }
    
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
    
    private void updateCandidates(ArrayList<String> words) {
        if (words.isEmpty()) {
            this.candidateView.setSuggestion(words);
            //setCandidatesViewShown(false);
        } else {
            this.candidateView.setSuggestion(words);
            setCandidatesViewShown(true);
        }   
    }
    
    public void onChooseWord(String word) {
        InputConnection ic = getCurrentInputConnection();        
        ic.commitText(word, 1);
        this.strokereset();
        if (this.wordProcessor.getChinesePhraseDictLinkedHashMap(word) != null) {
            this.candidateView.setSuggestion(new ArrayList<String>(this.wordProcessor.getChinesePhraseDictLinkedHashMap(word)));
        }
        //setCandidatesViewShown(false);
    }
    
    private void handleClose() {
        this.strokereset();
        requestHideSelf(0);
        inputView.closing();
    }

    private void updateInputCode(String code) {
        InputConnection ic = getCurrentInputConnection();
        String output = WordProcessor.translateToChangjieCode(code);
        ic.setComposingText(output, output.length());
    }
}
