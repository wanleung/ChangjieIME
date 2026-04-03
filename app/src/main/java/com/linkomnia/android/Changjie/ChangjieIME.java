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

import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;

import androidx.preference.PreferenceManager;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The main Input Method Service for Changjie Chinese input.
 *
 * <p>Rewritten to use the custom {@link ChangjieKeyboardView} and {@link KeyboardLayout}
 * instead of the removed {@link android.inputmethodservice.KeyboardView} and
 * {@link android.inputmethodservice.Keyboard} classes.</p>
 */
public class ChangjieIME extends InputMethodService
        implements ChangjieKeyboardView.OnKeyboardActionListener {

    private ChangjieKeyboardView inputView;
    private CandidateView candidateView;

    private WordProcessor wordProcessor;
    private IMESwitch imeSwitch;

    private final char[] charbuffer = new char[5];
    private int strokecount = 0;

    private SharedPreferences sharedPrefs;

    // -------------------------------------------------------------------------
    // Service lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        wordProcessor = new WordProcessor(this);
        wordProcessor.init();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onInitializeInterface() {
        imeSwitch = new IMESwitch(this);
    }

    @Override
    public View onCreateInputView() {
        View root = getLayoutInflater().inflate(R.layout.main, null);
        inputView = root.findViewById(R.id.keyboard);
        inputView.setOnKeyboardActionListener(this);
        candidateView = root.findViewById(R.id.candidate_view);
        candidateView.setDelegate(this);
        return root;
    }

    @Override
    public View onCreateCandidatesView() {
        // Candidates bar is embedded inside the input view; nothing to do here.
        return null;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        strokereset();
    }

    @Override
    public void onFinishInput() {
        strokereset();
        super.onFinishInput();
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        imeSwitch.init();
        inputView.setKeyboard(imeSwitch.getCurrentKeyboard());
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
    }

    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        super.onDisplayCompletions(completions);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // -------------------------------------------------------------------------
    // ChangjieKeyboardView.OnKeyboardActionListener
    // -------------------------------------------------------------------------

    /**
     * Called when a key is pressed and released on the keyboard view.
     *
     * @param primaryCode Unicode code point or one of the {@link KeyboardLayout} KEYCODE_ constants
     */
    @Override
    public void onKey(int primaryCode) {
        // Handle keyboard mode switches first
        if (imeSwitch.handleKey(primaryCode)) {
            strokereset();
            inputView.setKeyboard(imeSwitch.getCurrentKeyboard());
            return;
        }

        if (primaryCode == KeyboardLayout.KEYCODE_CANCEL) {
            handleClose();
            return;
        }
        if (primaryCode == KeyboardLayout.KEYCODE_DELETE) {
            handleBackspace();
            return;
        }
        if (primaryCode == KeyboardLayout.KEYCODE_ENTER) {
            handleEnter();
            return;
        }
        if (primaryCode == KeyboardLayout.KEYCODE_DONE) {
            return;
        }
        handleKey(primaryCode);
    }

    @Override
    public void onPress(int primaryCode) {
        // no-op
    }

    @Override
    public void onRelease(int primaryCode) {
        // no-op
    }

    // -------------------------------------------------------------------------
    // Key handling
    // -------------------------------------------------------------------------

    /**
     * Routes a character or space key press to the appropriate handler.
     *
     * @param keyCode the pressed key code
     */
    private void handleKey(int keyCode) {
        if (imeSwitch.isChinese() && keyCode == ' ') {
            if (strokecount == 0) {
                handleCharacter(keyCode);
            } else if (strokecount == 1) {
                onChooseWord(WordProcessor.translateToChangjieCode(
                        new String(charbuffer, 0, strokecount)));
            } else {
                if (sharedPrefs.getBoolean("setting_quick", false)) {
                    candidateView.goRight();
                } else {
                    if (candidateView.getSuggestion().size() > 0) {
                        onChooseWord(candidateView.getSuggestion().get(0));
                    }
                }
            }
        } else if (imeSwitch.isChinese() && keyCode >= 'a' && keyCode <= 'z') {
            typingStroke(keyCode);
        } else {
            handleCharacter(keyCode);
        }
    }

    /**
     * Appends a Changjie stroke to the composing buffer and updates candidates.
     *
     * @param keycode the letter key code (a–z)
     */
    private void typingStroke(int keycode) {
        char c = (char) keycode;
        int maxKeyNum = 5;
        if (sharedPrefs.getBoolean("setting_quick", false)) {
            maxKeyNum = 2;
        }
        if (strokecount < maxKeyNum) {
            charbuffer[strokecount++] = c;
        }
        String typed = new String(charbuffer, 0, strokecount);
        candidateView.updateInputBox(typed);
        updateInputCode(typed);
        ArrayList<String> words = wordProcessor.getChineseWordDictArrayList(typed);
        updateCandidates(words);
    }

    /**
     * Handles the DELETE key; removes the last stroke or sends a backspace event.
     */
    private void handleBackspace() {
        if (imeSwitch.isChinese()) {
            if (strokecount > 1) {
                strokecount -= 1;
                String typed = new String(charbuffer, 0, strokecount);
                candidateView.updateInputBox(typed);
                updateInputCode(typed);
                ArrayList<String> words = wordProcessor.getChineseWordDictArrayList(typed);
                updateCandidates(words);
            } else if (strokecount > 0) {
                strokereset();
            } else {
                keyDownUp(KeyEvent.KEYCODE_DEL);
                strokereset();
            }
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
    }

    /**
     * Commits a single character to the input connection.
     * Handles shift/caps-lock state for letter keys and uses
     * {@link Character#toChars(int)} to correctly encode emoji and supplementary characters.
     *
     * @param primaryCode the Unicode code point to commit
     */
    private void handleCharacter(int primaryCode) {
        strokereset();
        KeyboardLayout currentKb = imeSwitch.getCurrentKeyboard();
        if (primaryCode >= 'a' && primaryCode <= 'z'
                && (currentKb.isShifted() || currentKb.isCapLock())) {
            primaryCode = Character.toUpperCase(primaryCode);
            if (!currentKb.isCapLock()) {
                currentKb.setShifted(false);
                inputView.invalidate();
            }
        }
        if (primaryCode > 0) {
            // Use Character.toChars to correctly handle supplementary (emoji) codepoints
            String text = new String(Character.toChars(primaryCode));
            getCurrentInputConnection().commitText(text, 1);
        }
    }

    /** Sends an ENTER key event to the input connection. */
    private void handleEnter() {
        keyDownUp(KeyEvent.KEYCODE_ENTER);
    }

    /**
     * Hides the soft keyboard.
     */
    private void handleClose() {
        strokereset();
        requestHideSelf(0);
    }

    // -------------------------------------------------------------------------
    // Word selection callback (called from CandidateView)
    // -------------------------------------------------------------------------

    /**
     * Called when the user taps a word in the candidate bar.
     * Commits the word and loads phrase suggestions.
     *
     * @param word the chosen word
     */
    public void onChooseWord(String word) {
        InputConnection ic = getCurrentInputConnection();
        ic.commitText(word, 1);
        strokereset();
        java.util.concurrent.CopyOnWriteArrayList<String> phraseResult =
                wordProcessor.getChinesePhraseDictLinkedHashMap(word);
        if (phraseResult != null && !phraseResult.isEmpty()) {
            candidateView.setSuggestion(new ArrayList<>(phraseResult));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Resets the stroke buffer and clears the composing text and candidate list.
     */
    private void strokereset() {
        Arrays.fill(charbuffer, (char) 0);
        strokecount = 0;
        if (candidateView != null) {
            updateInputCode(new String(charbuffer, 0, strokecount));
            candidateView.updateInputBox(new String(charbuffer, 0, strokecount));
            updateCandidates(new ArrayList<>());
        }
    }

    /**
     * Updates the composing text shown in the text field with the translated radical string.
     *
     * @param code the typed Changjie code (letters a–z)
     */
    private void updateInputCode(String code) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            String output = WordProcessor.translateToChangjieCode(code);
            ic.setComposingText(output, output.length());
        }
    }

    /**
     * Pushes the candidate list to the candidate bar.
     *
     * @param words new list of candidates
     */
    private void updateCandidates(ArrayList<String> words) {
        candidateView.setSuggestion(words);
    }

    /**
     * Sends a down-then-up key event for the given Android key code.
     *
     * @param keyEventCode {@link KeyEvent} key code
     */
    private void keyDownUp(int keyEventCode) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
        }
    }
}
