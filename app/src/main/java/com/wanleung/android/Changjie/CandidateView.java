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
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * The candidates bar displayed above the keyboard.
 *
 * <p>Shows a row of word candidates with left/right pagination buttons.
 * Rewritten to replace deprecated API calls with their modern equivalents.</p>
 */
public class CandidateView extends LinearLayout {

    private Context ctx;
    private Button leftButton;
    private Button rightButton;
    private LinearLayout wordbar;
    private ChangjieIME mDelegate;

    private int result_max;

    private ArrayList<Button> wordbuttonList;
    private int wordlevel;

    private ArrayList<String> wordList;

    private int show_div;
    private int show_rem;
    private int show_max;

    /**
     * Constructor used when inflating from XML.
     *
     * @param context application context
     * @param attrs   attribute set from XML
     */
    public CandidateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        result_max = Integer.parseInt(getResources().getString(R.string.result_max));
        wordList = new ArrayList<>();

        // Use ContextCompat for deprecated getColor call
        setBackgroundColor(ContextCompat.getColor(ctx, R.color.candidate_background));

        leftButton = findViewById(R.id.arrow_left);
        leftButton.setText("<");
        leftButton.setOnClickListener(new ButtonOnClickListener(this, 0));

        rightButton = findViewById(R.id.arrow_right);
        rightButton.setText(">");
        rightButton.setOnClickListener(new ButtonOnClickListener(this, 1));

        wordbuttonList = new ArrayList<>();
        wordbar = findViewById(R.id.wordbar);

        // LayoutParams.FILL_PARENT → MATCH_PARENT
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1);

        for (int i = 0; i < result_max; i++) {
            Button b = new Button(ctx);
            // Use ContextCompat for deprecated getColor call
            b.setTextColor(ContextCompat.getColor(ctx, R.color.candidate_normal));
            b.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.candidates_char_size));
            // Use ContextCompat for deprecated getDrawable; setBackground replaces setBackgroundDrawable
            b.setBackground(ContextCompat.getDrawable(ctx, R.drawable.button));
            wordbuttonList.add(b);
            wordbar.addView(b, lp);
            b.setOnClickListener(new WordButtonOnClickListener(this));
        }
    }

    /**
     * Sets the {@link ChangjieIME} delegate that receives word-chosen callbacks.
     *
     * @param target the IME service
     */
    public void setDelegate(ChangjieIME target) {
        mDelegate = target;
    }

    /**
     * Returns the current delegate.
     *
     * @return the IME service delegate
     */
    public ChangjieIME getDelegate() {
        return mDelegate;
    }

    /** Called when the user has chosen a word; resets the page to the first. */
    public void didChooseWord() {
        wordlevel = 0;
    }

    /**
     * Updates the candidate list and redraws.
     *
     * @param list new list of candidate strings
     */
    public void setSuggestion(ArrayList<String> list) {
        wordlevel = 0;
        wordList = list;

        if (wordList.size() == 0) {
            cleanWords();
            return;
        }

        show_div = (wordList.size() - 1) / result_max;
        show_rem = (wordList.size() - 1) % result_max;
        show_max = show_div;
        showWords();
    }

    /**
     * Returns the current list of candidate suggestions.
     *
     * @return candidate list
     */
    public ArrayList<String> getSuggestion() {
        return wordList;
    }

    /**
     * Called when the composing text changes.
     * Currently a no-op; the composing text display is handled via
     * {@link android.view.inputmethod.InputConnection#setComposingText} in {@link ChangjieIME}.
     *
     * @param input the current input code string
     */
    public void updateInputBox(String input) {
        // no-op: display handled by ChangjieIME.updateInputCode()
    }

    /** Displays the current page of candidates. */
    protected void showWords() {
        if (wordList.size() == 0) {
            cleanWords();
            return;
        }
        int show = (wordlevel == show_max) ? show_rem : result_max;
        for (int i = 0; i < result_max; i++) {
            Button b = wordbuttonList.get(i);
            if (i > show) {
                b.setText("\u3000");
            } else {
                b.setText(wordList.get(wordlevel * result_max + i));
            }
        }
        invalidate();
    }

    /** Clears all candidate buttons. */
    protected void cleanWords() {
        for (int i = 0; i < result_max; i++) {
            wordbuttonList.get(i).setText("\u3000");
        }
        invalidate();
    }

    /** Returns the current page index. */
    protected int getWordlevel() {
        return wordlevel;
    }

    /**
     * Sets the current page index, clamped to valid range.
     *
     * @param i desired page index
     */
    protected void setWordlevel(int i) {
        wordlevel = Math.max(i, 0);
        wordlevel = Math.min(show_div, wordlevel);
    }

    /** Navigates to the previous page of candidates. */
    public void goLeft() {
        setWordlevel(getWordlevel() - 1);
        showWords();
    }

    /** Navigates to the next page of candidates, wrapping around. */
    public void goRight() {
        if (wordlevel == show_max) {
            setWordlevel(0);
        } else {
            setWordlevel(getWordlevel() + 1);
        }
        showWords();
    }
}

/** Click listener for the left/right pagination buttons. */
class ButtonOnClickListener implements View.OnClickListener {

    private final CandidateView parent;
    /** 0 = left button, 1 = right button. */
    private final int type;

    /**
     * @param p the owning CandidateView
     * @param t button type: 0 = left, 1 = right
     */
    public ButtonOnClickListener(CandidateView p, int t) {
        this.type = t;
        this.parent = p;
    }

    @Override
    public void onClick(View v) {
        if (type == 0) {
            parent.setWordlevel(parent.getWordlevel() - 1);
        } else {
            parent.setWordlevel(parent.getWordlevel() + 1);
        }
        parent.showWords();
    }
}

/** Click listener for individual candidate word buttons. */
class WordButtonOnClickListener implements View.OnClickListener {

    private final CandidateView parent;

    /**
     * @param p the owning CandidateView
     */
    public WordButtonOnClickListener(CandidateView p) {
        this.parent = p;
    }

    @Override
    public void onClick(View v) {
        String str = ((Button) v).getText().toString();
        if (!str.isEmpty() && !str.equals("\u3000")) {
            parent.getDelegate().onChooseWord(str);
            parent.didChooseWord();
        }
    }
}
