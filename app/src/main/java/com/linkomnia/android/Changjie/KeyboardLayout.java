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

/**
 * Keyboard layout model, replacing the removed android.inputmethodservice.Keyboard.
 * Contains rows of keys and factory methods for each keyboard type.
 */
public class KeyboardLayout {

    // Standard keycodes matching android.inputmethodservice.Keyboard
    public static final int KEYCODE_SHIFT = -1;
    public static final int KEYCODE_MODE_CHANGE = -2;
    public static final int KEYCODE_CANCEL = -3;
    public static final int KEYCODE_DONE = -4;
    public static final int KEYCODE_DELETE = -5;
    public static final int KEYCODE_ENTER = 10;

    // Custom keycodes matching old IMEKeyboard
    public static final int KEYCODE_CAPLOCK = -200;
    public static final int KEYCODE_MODE_CHANGE_CHAR = -300;
    public static final int KEYCODE_MODE_CHANGE_SIMLEY = -400;
    public static final int KEYCODE_MODE_CHANGE_CHSYMBOL = -500;
    public static final int KEYCODE_MODE_CHANGE_LANG = -600;

    /**
     * Represents a single key in the keyboard layout.
     */
    public static class Key {
        /** Display label (main face of key). */
        public final String label;
        /** Label shown when keyboard is shifted; null means uppercase of label. */
        public final String shiftLabel;
        /** The key code (Unicode codepoint or one of the KEYCODE_ constants). */
        public final int code;
        /** Relative width weight (1.0 = normal key width). */
        public final float widthWeight;
        /** Whether this key fires repeatedly when held (e.g., DELETE). */
        public final boolean repeatable;

        /**
         * Full constructor.
         *
         * @param label       displayed label
         * @param shiftLabel  shifted label (null = auto-uppercase)
         * @param code        key code
         * @param widthWeight relative width
         * @param repeatable  true if key should auto-repeat on long press
         */
        public Key(String label, String shiftLabel, int code,
                   float widthWeight, boolean repeatable) {
            this.label = label;
            this.shiftLabel = shiftLabel;
            this.code = code;
            this.widthWeight = widthWeight;
            this.repeatable = repeatable;
        }

        /** Convenience: normal key (weight 1.0, not repeatable). */
        public Key(String label, int code) {
            this(label, null, code, 1.0f, false);
        }

        /** Convenience: normal key with custom width weight. */
        public Key(String label, int code, float widthWeight) {
            this(label, null, code, widthWeight, false);
        }
    }

    /**
     * A row of keys in the keyboard layout.
     */
    public static class Row {
        /** Keys in this row. */
        public final List<Key> keys = new ArrayList<>();

        /**
         * Adds a key to this row and returns this row for chaining.
         *
         * @param key key to add
         * @return this row
         */
        public Row add(Key key) {
            keys.add(key);
            return this;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private boolean shifted = false;
    private boolean capLock = false;
    /** Whether to display Cangjie radical sub-labels on letter keys. */
    private boolean showRadicals = false;

    /** Returns all rows of this keyboard layout. */
    public List<Row> getRows() {
        return rows;
    }

    /** Returns true if the keyboard is currently in shifted state. */
    public boolean isShifted() {
        return shifted;
    }

    /** Returns true if caps-lock is engaged. */
    public boolean isCapLock() {
        return capLock;
    }

    /** Returns true if Cangjie radical sub-labels should be shown. */
    public boolean isShowRadicals() {
        return showRadicals;
    }

    /** Sets the shifted state. */
    public void setShifted(boolean shifted) {
        this.shifted = shifted;
    }

    /**
     * Sets caps-lock state; also updates shifted to match.
     *
     * @param capLock true to engage caps-lock
     */
    public void setCapLock(boolean capLock) {
        this.capLock = capLock;
        this.shifted = capLock;
    }

    /** Adds a row to this layout. */
    protected void addRow(Row row) {
        rows.add(row);
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates the Changjie input keyboard layout.
     * QWERTY arrangement with Cangjie radical sub-labels shown under each letter key.
     *
     * @return configured KeyboardLayout
     */
    public static KeyboardLayout createChangjie() {
        KeyboardLayout kb = new KeyboardLayout();
        kb.showRadicals = true;

        // Row 1: q w e r t y u i o p
        Row row1 = new Row();
        row1.add(new Key("q", 'q')).add(new Key("w", 'w')).add(new Key("e", 'e'))
            .add(new Key("r", 'r')).add(new Key("t", 't')).add(new Key("y", 'y'))
            .add(new Key("u", 'u')).add(new Key("i", 'i')).add(new Key("o", 'o'))
            .add(new Key("p", 'p'));
        kb.addRow(row1);

        // Row 2: a s d f g h j k l
        Row row2 = new Row();
        row2.add(new Key("a", 'a')).add(new Key("s", 's')).add(new Key("d", 'd'))
            .add(new Key("f", 'f')).add(new Key("g", 'g')).add(new Key("h", 'h'))
            .add(new Key("j", 'j')).add(new Key("k", 'k')).add(new Key("l", 'l'));
        kb.addRow(row2);

        // Row 3: ⇪(CAPLOCK 1.5w) z x c v b n m ⌫(DELETE 1.5w repeatable)
        Row row3 = new Row();
        row3.add(new Key("⇪", KEYCODE_CAPLOCK, 1.5f))
            .add(new Key("z", 'z')).add(new Key("x", 'x')).add(new Key("c", 'c'))
            .add(new Key("v", 'v')).add(new Key("b", 'b')).add(new Key("n", 'n'))
            .add(new Key("m", 'm'))
            .add(new Key("⌫", null, KEYCODE_DELETE, 1.5f, true));
        kb.addRow(row3);

        // Row 4: EN(LANG 1.5w) 符(MODE_CHANGE 1.5w) space(4.0w) 中符(CHSYMBOL 1.5w) ↵(ENTER 1.5w)
        Row row4 = new Row();
        row4.add(new Key("EN", KEYCODE_MODE_CHANGE_LANG, 1.5f))
            .add(new Key("符", KEYCODE_MODE_CHANGE, 1.5f))
            .add(new Key(" ", ' ', 4.0f))
            .add(new Key("中符", KEYCODE_MODE_CHANGE_CHSYMBOL, 1.5f))
            .add(new Key("↵", KEYCODE_ENTER, 1.5f));
        kb.addRow(row4);

        return kb;
    }

    /**
     * Creates a standard QWERTY English keyboard layout.
     *
     * @return configured KeyboardLayout
     */
    public static KeyboardLayout createQwerty() {
        KeyboardLayout kb = new KeyboardLayout();

        // Row 1: q-p with uppercase shift labels
        Row row1 = new Row();
        for (char c : "qwertyuiop".toCharArray()) {
            row1.add(new Key(String.valueOf(c), String.valueOf(Character.toUpperCase(c)),
                    c, 1.0f, false));
        }
        kb.addRow(row1);

        // Row 2: a-l
        Row row2 = new Row();
        for (char c : "asdfghjkl".toCharArray()) {
            row2.add(new Key(String.valueOf(c), String.valueOf(Character.toUpperCase(c)),
                    c, 1.0f, false));
        }
        kb.addRow(row2);

        // Row 3: ⇧(SHIFT 1.5w) z-m ⌫(DELETE 1.5w repeatable)
        Row row3 = new Row();
        row3.add(new Key("⇧", KEYCODE_SHIFT, 1.5f));
        for (char c : "zxcvbnm".toCharArray()) {
            row3.add(new Key(String.valueOf(c), String.valueOf(Character.toUpperCase(c)),
                    c, 1.0f, false));
        }
        row3.add(new Key("⌫", null, KEYCODE_DELETE, 1.5f, true));
        kb.addRow(row3);

        // Row 4: 中(LANG 1.5w) ?123(MODE_CHANGE 1.5w) space(4.0w) ,(1.0w) ↵(ENTER 1.5w)
        Row row4 = new Row();
        row4.add(new Key("中", KEYCODE_MODE_CHANGE_LANG, 1.5f))
            .add(new Key("?123", KEYCODE_MODE_CHANGE, 1.5f))
            .add(new Key(" ", ' ', 4.0f))
            .add(new Key(",", ',', 1.0f))
            .add(new Key("↵", KEYCODE_ENTER, 1.5f));
        kb.addRow(row4);

        return kb;
    }

    /**
     * Creates an English number/symbol keyboard layout.
     *
     * @return configured KeyboardLayout
     */
    public static KeyboardLayout createSymbolsEn() {
        KeyboardLayout kb = new KeyboardLayout();

        // Row 1: 1-0
        Row row1 = new Row();
        for (char c : "1234567890".toCharArray()) {
            row1.add(new Key(String.valueOf(c), c));
        }
        kb.addRow(row1);

        // Row 2: @ # $ % & - + ( ) /
        Row row2 = new Row();
        for (char c : "@#$%&-+()/".toCharArray()) {
            row2.add(new Key(String.valueOf(c), c));
        }
        kb.addRow(row2);

        // Row 3: =\<(SHIFT 1.5w) * " ' : ; ! ? ⌫(DELETE 1.5w repeatable)
        Row row3 = new Row();
        row3.add(new Key("=\\<", KEYCODE_SHIFT, 1.5f));
        for (char c : "*\"':;!?".toCharArray()) {
            row3.add(new Key(String.valueOf(c), c));
        }
        row3.add(new Key("⌫", null, KEYCODE_DELETE, 1.5f, true));
        kb.addRow(row3);

        // Row 4: ABC(MODE_CHANGE_CHAR 1.5w) 中符(CHSYMBOL 1.5w) space(4.0w) ,(1.0w) ↵(ENTER 1.5w)
        Row row4 = new Row();
        row4.add(new Key("ABC", KEYCODE_MODE_CHANGE_CHAR, 1.5f))
            .add(new Key("中符", KEYCODE_MODE_CHANGE_CHSYMBOL, 1.5f))
            .add(new Key(" ", ' ', 4.0f))
            .add(new Key(",", ',', 1.0f))
            .add(new Key("↵", KEYCODE_ENTER, 1.5f));
        kb.addRow(row4);

        return kb;
    }

    /**
     * Creates an English shifted symbol keyboard layout.
     *
     * @return configured KeyboardLayout
     */
    public static KeyboardLayout createSymbolsEnShift() {
        KeyboardLayout kb = new KeyboardLayout();

        // Row 1: 1-0
        Row row1 = new Row();
        for (char c : "1234567890".toCharArray()) {
            row1.add(new Key(String.valueOf(c), c));
        }
        kb.addRow(row1);

        // Row 2: ~ ` | • √ π ÷ × ¶ ∆
        Row row2 = new Row();
        String[] row2labels = {"~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆"};
        for (String label : row2labels) {
            row2.add(new Key(label, label.codePointAt(0)));
        }
        kb.addRow(row2);

        // Row 3: ?123(SHIFT 1.5w) £ ¢ € ¥ ^ ° = { } ⌫(DELETE 1.5w repeatable)
        Row row3 = new Row();
        row3.add(new Key("?123", KEYCODE_SHIFT, 1.5f));
        String[] row3labels = {"£", "¢", "€", "¥", "^", "°", "=", "{", "}"};
        for (String label : row3labels) {
            row3.add(new Key(label, label.codePointAt(0)));
        }
        row3.add(new Key("⌫", null, KEYCODE_DELETE, 1.5f, true));
        kb.addRow(row3);

        // Row 4: ABC(MODE_CHANGE_CHAR 1.5w) 中符(CHSYMBOL 1.5w) space(4.0w) ,(1.0w) ↵(ENTER 1.5w)
        Row row4 = new Row();
        row4.add(new Key("ABC", KEYCODE_MODE_CHANGE_CHAR, 1.5f))
            .add(new Key("中符", KEYCODE_MODE_CHANGE_CHSYMBOL, 1.5f))
            .add(new Key(" ", ' ', 4.0f))
            .add(new Key(",", ',', 1.0f))
            .add(new Key("↵", KEYCODE_ENTER, 1.5f));
        kb.addRow(row4);

        return kb;
    }

    /**
     * Creates a Chinese fullwidth/punctuation symbol keyboard layout.
     *
     * @return configured KeyboardLayout
     */
    public static KeyboardLayout createSymbolsCh() {
        KeyboardLayout kb = new KeyboardLayout();

        // Row 1: ！ ？ ， 。 ； ： 「 」 （ ）
        Row row1 = new Row();
        String[] row1labels = {"！", "？", "，", "。", "；", "：", "「", "」", "（", "）"};
        for (String label : row1labels) {
            row1.add(new Key(label, label.codePointAt(0)));
        }
        kb.addRow(row1);

        // Row 2: 《 》 〈 〉 【 】 … — ～ ·
        Row row2 = new Row();
        String[] row2labels = {"《", "》", "〈", "〉", "【", "】", "…", "—", "～", "·"};
        for (String label : row2labels) {
            row2.add(new Key(label, label.codePointAt(0)));
        }
        kb.addRow(row2);

        // Row 3: 、 ￥ 『 』 〔 〕 ※ § ¤ ⌫(DELETE 1.5w repeatable)
        Row row3 = new Row();
        String[] row3labels = {"、", "￥", "『", "』", "〔", "〕", "※", "§", "¤"};
        for (String label : row3labels) {
            row3.add(new Key(label, label.codePointAt(0)));
        }
        row3.add(new Key("⌫", null, KEYCODE_DELETE, 1.5f, true));
        kb.addRow(row3);

        // Row 4: ABC(MODE_CHANGE_CHAR 1.5w) ?123(MODE_CHANGE 1.5w) space(4.0w) ↵(ENTER 2.0w)
        Row row4 = new Row();
        row4.add(new Key("ABC", KEYCODE_MODE_CHANGE_CHAR, 1.5f))
            .add(new Key("?123", KEYCODE_MODE_CHANGE, 1.5f))
            .add(new Key(" ", ' ', 4.0f))
            .add(new Key("↵", KEYCODE_ENTER, 2.0f));
        kb.addRow(row4);

        return kb;
    }

    /**
     * Creates a smiley/emoji keyboard layout.
     *
     * @return configured KeyboardLayout
     */
    public static KeyboardLayout createSmiley() {
        KeyboardLayout kb = new KeyboardLayout();

        // Row 1: 😀😂😍😎😭😡👍👎❤🎉
        Row row1 = new Row();
        int[] row1codes = {0x1F600, 0x1F602, 0x1F60D, 0x1F60E, 0x1F62D,
                           0x1F621, 0x1F44D, 0x1F44E, 0x2764, 0x1F389};
        for (int code : row1codes) {
            String emoji = new String(Character.toChars(code));
            row1.add(new Key(emoji, code));
        }
        kb.addRow(row1);

        // Row 2: 😊🤔😴🤣😱🙏💪🔥✅😘
        Row row2 = new Row();
        int[] row2codes = {0x1F60A, 0x1F914, 0x1F634, 0x1F923, 0x1F631,
                           0x1F64F, 0x1F4AA, 0x1F525, 0x2705, 0x1F618};
        for (int code : row2codes) {
            String emoji = new String(Character.toChars(code));
            row2.add(new Key(emoji, code));
        }
        kb.addRow(row2);

        // Row 3: 😜🥳🤩😔💯🤷😇👋🤗😏
        Row row3 = new Row();
        int[] row3codes = {0x1F61C, 0x1F973, 0x1F929, 0x1F614, 0x1F4AF,
                           0x1F937, 0x1F607, 0x1F44B, 0x1F917, 0x1F60F};
        for (int code : row3codes) {
            String emoji = new String(Character.toChars(code));
            row3.add(new Key(emoji, code));
        }
        kb.addRow(row3);

        // Row 4: ABC(MODE_CHANGE_CHAR 1.5w) ?123(MODE_CHANGE 1.5w) space(4.0w)
        //        ⌫(DELETE 1.5w repeatable) ↵(ENTER 1.5w)
        Row row4 = new Row();
        row4.add(new Key("ABC", KEYCODE_MODE_CHANGE_CHAR, 1.5f))
            .add(new Key("?123", KEYCODE_MODE_CHANGE, 1.5f))
            .add(new Key(" ", ' ', 4.0f))
            .add(new Key("⌫", null, KEYCODE_DELETE, 1.5f, true))
            .add(new Key("↵", KEYCODE_ENTER, 1.5f));
        kb.addRow(row4);

        return kb;
    }
}
