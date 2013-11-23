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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.math.*;

import com.linkomnia.android.Changjie.R;

public class CandidateView extends LinearLayout {
    
    private Context ctx;
    private Button leftButton;
    private Button rightButton;
    private LinearLayout topbar;
    private LinearLayout bottombar;
    private LinearLayout wordbar;
    private ChangjieIME mDelegate;
    
    private int result_max;
    
    private ArrayList<ImageButton> inputBox;
    private ArrayList<Button> wordbuttonList;
    private int wordlevel;

    private ArrayList<String> wordList;
    
    private int show_div;
    private int show_rem;
    private int show_max;

    public CandidateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        result_max = Integer.parseInt(this.getResources().getString(R.string.result_max));
        this.wordList = new ArrayList<String>();
        this.setBackgroundColor(getResources().getColor(R.color.candidate_background));
        
        this.leftButton = (Button) findViewById(R.id.arrow_left);
        this.leftButton.setText("<");
        this.leftButton.setOnClickListener(new ButtonOnClickListener(this, 0));

        this.rightButton = (Button) findViewById(R.id.arrow_right);
        this.rightButton.setText(">");
        this.rightButton.setOnClickListener(new ButtonOnClickListener(this, 1));

        this.wordbuttonList = new ArrayList<Button>();
        this.wordbar = (LinearLayout)this.findViewById(R.id.wordbar);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);

        for (int i = 0; i < result_max; i++) {
            Button b = new Button(ctx);
            b.setTextColor(this.getResources().getColor(R.color.candidate_normal));
            b.setTextSize(this.getResources().getDimension(R.dimen.keychar_size));
            b.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button));
            this.wordbuttonList.add(b);
            this.wordbar.addView(b, lp);
            b.setOnClickListener(new WordButtonOnClickListener(this));

        }
        
        this.bottombar = (LinearLayout)this.findViewById(R.id.bottombar);
        this.inputBox = new ArrayList<ImageButton>();
        for (int i = 0; i < 5; i++) {
            ImageButton b = new ImageButton(ctx);
            b.setImageResource(R.drawable.stroke0_show);
            b.setBackgroundColor(getResources().getColor(R.color.candidate_background));
            this.inputBox.add(b);
            this.bottombar.addView(b, lp);
        }
        
    }

    
    public void setDelegate(ChangjieIME target) {
        mDelegate =  target;
    }
    
    public ChangjieIME getDelegate() {
        return this.mDelegate;
    }
    
    public void didChooseWord() {
        this.wordlevel = 0;
    }
    
    public void setSuggestion(ArrayList<String> list) {
        wordlevel = 0;
        this.wordList = list;
        
        if (this.wordList.size() == 0) {
            this.cleanWords();
            return;
        }
        
        show_div = (this.wordList.size()-1) / result_max;
        show_rem = (this.wordList.size()-1) % result_max;
        
        show_max = show_div;//show_rem == 0?show_div-1:show_div;
        this.showWords();
    }
    
    public void updateInputBox(String input) {
        for (int i = 0; i < 5; i++) {
            if (i<input.length()) {
                switch (input.charAt(i)) {
                    case 'm': {
                        this.inputBox.get(i).setImageResource(R.drawable.stroke1_show);
                        break;
                    }
                    case '/': {
                        this.inputBox.get(i).setImageResource(R.drawable.stroke2_show);
                        break;
                    }
                    case ',': {
                        this.inputBox.get(i).setImageResource(R.drawable.stroke3_show);
                        break;
                    }
                    case '.': {
                        this.inputBox.get(i).setImageResource(R.drawable.stroke4_show);
                        break;
                    }
                    case 'n': {
                        this.inputBox.get(i).setImageResource(R.drawable.stroke5_show);
                        break;
                    }
                    default: {
                        this.inputBox.get(i).setImageResource(R.drawable.stroke0_show);
                        break;
                    }
                }
            } else {
                this.inputBox.get(i).setImageResource(R.drawable.stroke0_show);
            }
        }
    }
    
    protected void showWords() {
        if (this.wordList.size() == 0) {
            this.cleanWords();
            return;
        }
        int show = (this.wordlevel == show_max)?show_rem:result_max;
        for (int i = 0; i < result_max; i++) {
            Button b = this.wordbuttonList.get(i);
            if (i > show) {
                b.setText("\u3000");
            } else {
                b.setText(this.wordList.get(this.wordlevel * result_max + i));
            }
        }
        this.invalidate();
    }
    
    protected void cleanWords() {
        for (int i = 0; i < result_max; i++) {
            Button b = this.wordbuttonList.get(i);
            b.setText("\u3000");
        }
        this.invalidate();
    }
    
    protected int getWordlevel() {
        return this.wordlevel;
    }
    
    protected void setWordlevel(int i) {
        this.wordlevel = Math.max(i,0);
        this.wordlevel = Math.min(this.show_div, this.wordlevel);
    } 
}

class ButtonOnClickListener implements View.OnClickListener {
    private CandidateView parnet;
    private int type;
    
    public ButtonOnClickListener(CandidateView p, int t) {
        this.type = t; //0 = left, 1 = right
        this.parnet = p;
    }
    
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (this.type == 0) {
            parnet.setWordlevel(parnet.getWordlevel()-1);
        } else {
            parnet.setWordlevel(parnet.getWordlevel()+1);
        }
        parnet.showWords();
    }
    
}

class WordButtonOnClickListener implements View.OnClickListener {
    private CandidateView parnet;
    
    public WordButtonOnClickListener(CandidateView p) {
        this.parnet = p;
    }
    
    public void onClick(View v) {
        // TODO Auto-generated method stub
        String str = ((Button)v).getText().toString();
        if (!(str.isEmpty() || str.equals("\u3000"))) {
            parnet.getDelegate().onChooseWord(((Button)v).getText().toString());
            parnet.didChooseWord();            
        }
    }
}
