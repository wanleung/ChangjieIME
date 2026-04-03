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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom keyboard view that replaces the removed android.inputmethodservice.KeyboardView.
 * Draws keys using Canvas and handles touch events.
 *
 * <p>Long-pressing the SHIFT key fires CAPLOCK instead of a normal SHIFT key event.
 * Long-pressing the DELETE key repeats the key code at a fixed interval.</p>
 */
public class ChangjieKeyboardView extends View {

    /**
     * Listener interface for key events, replacing KeyboardView.OnKeyboardActionListener.
     */
    public interface OnKeyboardActionListener {
        /**
         * Called when a key is pressed and released.
         *
         * @param primaryCode the Unicode code point or one of the KeyboardLayout.KEYCODE_* values
         */
        void onKey(int primaryCode);

        /**
         * Called when a key is first pressed down.
         *
         * @param primaryCode the key code
         */
        void onPress(int primaryCode);

        /**
         * Called when a key is released.
         *
         * @param primaryCode the key code
         */
        void onRelease(int primaryCode);
    }

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Height of a single key row in dp. */
    private static final int ROW_HEIGHT_DP = 56;
    /** Padding between keys in dp. */
    private static final int KEY_PADDING_DP = 2;
    /** Corner radius for key background in dp. */
    private static final int KEY_CORNER_RADIUS_DP = 4;

    // Key background colors
    private static final int COLOR_KEY_NORMAL = 0xFFFFFFFF;
    private static final int COLOR_KEY_SPECIAL = 0xFFAAAAAA;
    private static final int COLOR_KEY_PRESSED = 0xFFBDBDBD;
    private static final int COLOR_LABEL = 0xFF212121;
    private static final int COLOR_SUBLABEL = 0xFF9E9E9E;

    // Text sizes in sp
    private static final int LABEL_TEXT_SIZE_SP = 18;
    private static final int SPECIAL_LABEL_TEXT_SIZE_SP = 13;
    private static final int SUBLABEL_TEXT_SIZE_SP = 10;

    /** Initial delay before repeating DELETE (ms). */
    private static final int REPEAT_INITIAL_DELAY_MS = 400;
    /** Interval between repeated DELETE fires (ms). */
    private static final int REPEAT_INTERVAL_MS = 50;
    /** Delay before SHIFT becomes CAPLOCK on long press (ms). */
    private static final long LONG_PRESS_SHIFT_DELAY_MS = 500;

    // -------------------------------------------------------------------------
    // Internal cell for hit-testing
    // -------------------------------------------------------------------------

    /** Maps a KeyboardLayout.Key to its bounding rectangle on screen. */
    private static class KeyCell {
        final KeyboardLayout.Key key;
        final RectF bounds;

        KeyCell(KeyboardLayout.Key key, RectF bounds) {
            this.key = key;
            this.bounds = bounds;
        }
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private KeyboardLayout keyboard;
    private OnKeyboardActionListener listener;

    /** Flat array of KeyCells built when layout changes. */
    private final List<KeyCell> keyCells = new ArrayList<>();

    /** Currently pressed key cell (for visual feedback). */
    private KeyCell pressedCell;

    /** Whether a long-press CAPLOCK was fired (to suppress normal SHIFT on up). */
    private boolean longPressCapLockFired = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Repeat runnable for DELETE key
    private Runnable repeatRunnable;

    // Long-press runnable for SHIFT → CAPLOCK
    private Runnable longPressRunnable;

    // Paint objects
    private final Paint keyBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sublabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Cached dp/sp conversions
    private float rowHeightPx;
    private float keyPaddingPx;
    private float keyCornerRadiusPx;
    private float labelTextSizePx;
    private float specialLabelTextSizePx;
    private float sublabelTextSizePx;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ChangjieKeyboardView(Context context) {
        super(context);
        init(context);
    }

    public ChangjieKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChangjieKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    private void init(Context context) {
        rowHeightPx = dp(ROW_HEIGHT_DP);
        keyPaddingPx = dp(KEY_PADDING_DP);
        keyCornerRadiusPx = dp(KEY_CORNER_RADIUS_DP);

        labelTextSizePx = sp(LABEL_TEXT_SIZE_SP);
        specialLabelTextSizePx = sp(SPECIAL_LABEL_TEXT_SIZE_SP);
        sublabelTextSizePx = sp(SUBLABEL_TEXT_SIZE_SP);

        labelPaint.setColor(COLOR_LABEL);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        sublabelPaint.setColor(COLOR_SUBLABEL);
        sublabelPaint.setTextAlign(Paint.Align.CENTER);
        sublabelPaint.setTextSize(sublabelTextSizePx);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Sets the keyboard layout to display and triggers a layout pass.
     *
     * @param kb keyboard layout (must not be null)
     */
    public void setKeyboard(KeyboardLayout kb) {
        this.keyboard = kb;
        keyCells.clear();
        // If the view is already laid out, rebuild immediately; requestLayout() alone
        // won't trigger onSizeChanged() (size hasn't changed) so keyCells would stay empty.
        if (getWidth() > 0 && getHeight() > 0) {
            buildKeyLayout(getWidth(), getHeight());
        }
        requestLayout();
        invalidate();
    }

    /** Returns the currently displayed keyboard layout. */
    public KeyboardLayout getKeyboard() {
        return keyboard;
    }

    /**
     * Sets the listener that receives key events.
     *
     * @param l listener
     */
    public void setOnKeyboardActionListener(OnKeyboardActionListener l) {
        this.listener = l;
    }

    // -------------------------------------------------------------------------
    // View measurement and layout
    // -------------------------------------------------------------------------

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int rowCount = (keyboard != null) ? keyboard.getRows().size() : 4;
        int height = (int) (rowCount * rowHeightPx);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            buildKeyLayout(w, h);
        }
    }

    /**
     * Builds the flat KeyCell array from the current keyboard layout and view dimensions.
     * Each key's bounding RectF is computed from its widthWeight relative to the total row width.
     *
     * @param viewWidth  total view width in pixels
     * @param viewHeight total view height in pixels
     */
    private void buildKeyLayout(int viewWidth, int viewHeight) {
        keyCells.clear();
        if (keyboard == null) return;

        List<KeyboardLayout.Row> rows = keyboard.getRows();
        int rowCount = rows.size();
        float rowH = (float) viewHeight / rowCount;

        for (int r = 0; r < rowCount; r++) {
            KeyboardLayout.Row row = rows.get(r);
            float top = r * rowH;
            float bottom = top + rowH;

            // Compute total weight for this row
            float totalWeight = 0f;
            for (KeyboardLayout.Key key : row.keys) {
                totalWeight += key.widthWeight;
            }

            float x = 0f;
            for (KeyboardLayout.Key key : row.keys) {
                float keyWidth = (key.widthWeight / totalWeight) * viewWidth;
                RectF bounds = new RectF(x + keyPaddingPx, top + keyPaddingPx,
                        x + keyWidth - keyPaddingPx, bottom - keyPaddingPx);
                keyCells.add(new KeyCell(key, bounds));
                x += keyWidth;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (keyboard == null) return;

        for (KeyCell cell : keyCells) {
            drawKey(canvas, cell);
        }
    }

    /**
     * Draws a single key cell on the canvas.
     *
     * @param canvas canvas to draw on
     * @param cell   the key cell to draw
     */
    private void drawKey(Canvas canvas, KeyCell cell) {
        KeyboardLayout.Key key = cell.key;
        RectF bounds = cell.bounds;
        boolean isPressed = (cell == pressedCell);

        // Determine background colour
        int bgColor;
        if (isPressed) {
            bgColor = COLOR_KEY_PRESSED;
        } else if (isSpecialKey(key.code)) {
            bgColor = COLOR_KEY_SPECIAL;
        } else {
            bgColor = COLOR_KEY_NORMAL;
        }

        keyBgPaint.setColor(bgColor);
        canvas.drawRoundRect(bounds, keyCornerRadiusPx, keyCornerRadiusPx, keyBgPaint);

        // Determine label and text size
        String label = getDisplayLabel(key);
        boolean isLetterKey = (key.code >= 'a' && key.code <= 'z')
                || (key.code >= 'A' && key.code <= 'Z');
        float textSize = (isSpecialKey(key.code) || !isLetterKey)
                ? specialLabelTextSizePx : labelTextSizePx;
        labelPaint.setTextSize(textSize);
        labelPaint.setColor(COLOR_LABEL);

        float cx = bounds.centerX();
        float cy = bounds.centerY();

        // Sub-label (Cangjie radical)
        if (keyboard.isShowRadicals() && isLetterKey) {
            String subLabel = getRadicalSublabel(key);
            if (subLabel != null) {
                // Draw main label shifted slightly upward
                float mainY = cy - (labelPaint.descent() + labelPaint.ascent()) / 2
                        - sublabelTextSizePx * 0.3f;
                canvas.drawText(label, cx, mainY, labelPaint);

                // Draw sub-label below main label
                float subY = bounds.bottom - sublabelPaint.descent() - keyPaddingPx;
                canvas.drawText(subLabel, cx, subY, sublabelPaint);
                return;
            }
        }

        // Normal label centred in key
        float labelY = cy - (labelPaint.descent() + labelPaint.ascent()) / 2;
        canvas.drawText(label, cx, labelY, labelPaint);
    }

    /**
     * Returns the display label for a key given the current keyboard shift state.
     *
     * @param key the key
     * @return the string to display on the key face
     */
    private String getDisplayLabel(KeyboardLayout.Key key) {
        if (keyboard.isShifted() && key.shiftLabel != null) {
            return key.shiftLabel;
        }
        if (keyboard.isShifted() && key.code >= 'a' && key.code <= 'z') {
            return String.valueOf((char) Character.toUpperCase(key.code));
        }
        return key.label;
    }

    /**
     * Returns the Cangjie radical sub-label for a letter key (a-z → radical index 0-25).
     *
     * @param key the key
     * @return radical string, or null if not applicable
     */
    private String getRadicalSublabel(KeyboardLayout.Key key) {
        if (key.code >= 'a' && key.code <= 'z') {
            int index = key.code - 'a';
            if (index >= 0 && index < WordProcessor.cangjie_radicals.length) {
                return WordProcessor.cangjie_radicals[index];
            }
        }
        return null;
    }

    /**
     * Returns true if the key should use the special (gray) background.
     * Special keys are those with negative codes (action keys) or ENTER.
     *
     * @param code key code
     * @return true if this is a special/action key
     */
    private boolean isSpecialKey(int code) {
        return code < 0 || code == KeyboardLayout.KEYCODE_ENTER || code == ' ';
    }

    // -------------------------------------------------------------------------
    // Touch handling
    // -------------------------------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                KeyCell cell = findCell(x, y);
                pressedCell = cell;
                longPressCapLockFired = false;
                invalidate();
                if (cell != null) {
                    if (listener != null) listener.onPress(cell.key.code);
                    if (cell.key.code == KeyboardLayout.KEYCODE_SHIFT) {
                        startLongPress(cell.key.code);
                    } else if (cell.key.repeatable) {
                        startRepeat(cell.key.code);
                    }
                }
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                KeyCell cell = findCell(x, y);
                if (cell != pressedCell) {
                    stopRepeat();
                    stopLongPress();
                    if (pressedCell != null && listener != null) {
                        listener.onRelease(pressedCell.key.code);
                    }
                    pressedCell = cell;
                    longPressCapLockFired = false;
                    if (cell != null) {
                        if (listener != null) listener.onPress(cell.key.code);
                        if (cell.key.code == KeyboardLayout.KEYCODE_SHIFT) {
                            startLongPress(cell.key.code);
                        } else if (cell.key.repeatable) {
                            startRepeat(cell.key.code);
                        }
                    }
                    invalidate();
                }
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                stopRepeat();
                stopLongPress();
                KeyCell released = pressedCell;
                pressedCell = null;
                invalidate();
                if (released != null) {
                    if (listener != null) listener.onRelease(released.key.code);
                    // Fire key event only if long-press CAPLOCK was not already fired
                    if (!longPressCapLockFired && listener != null) {
                        listener.onKey(released.key.code);
                    }
                    longPressCapLockFired = false;
                }
                return true;
            }

            default:
                return super.onTouchEvent(event);
        }
    }

    /**
     * Finds the KeyCell whose bounds contain the given coordinates.
     *
     * @param x x coordinate in view space
     * @param y y coordinate in view space
     * @return matching KeyCell, or null if none found
     */
    private KeyCell findCell(float x, float y) {
        for (KeyCell cell : keyCells) {
            if (cell.bounds.contains(x, y)) {
                return cell;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Repeat (DELETE long-press)
    // -------------------------------------------------------------------------

    /**
     * Starts the auto-repeat sequence for the given key code.
     * The key fires once immediately after {@link #REPEAT_INITIAL_DELAY_MS},
     * then every {@link #REPEAT_INTERVAL_MS}.
     *
     * @param keyCode key code to repeat
     */
    private void startRepeat(final int keyCode) {
        stopRepeat();
        repeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (pressedCell != null && listener != null) {
                    listener.onKey(keyCode);
                    mainHandler.postDelayed(this, REPEAT_INTERVAL_MS);
                }
            }
        };
        mainHandler.postDelayed(repeatRunnable, REPEAT_INITIAL_DELAY_MS);
    }

    /** Cancels any pending repeat. */
    private void stopRepeat() {
        if (repeatRunnable != null) {
            mainHandler.removeCallbacks(repeatRunnable);
            repeatRunnable = null;
        }
    }

    // -------------------------------------------------------------------------
    // Long-press SHIFT → CAPLOCK
    // -------------------------------------------------------------------------

    /**
     * Starts a delayed runnable that fires CAPLOCK if the SHIFT key is held
     * for {@link #LONG_PRESS_SHIFT_DELAY_MS} milliseconds.
     *
     * @param keyCode expected to be {@link KeyboardLayout#KEYCODE_SHIFT}
     */
    private void startLongPress(final int keyCode) {
        stopLongPress();
        longPressRunnable = () -> {
            if (pressedCell != null && listener != null) {
                longPressCapLockFired = true;
                listener.onKey(KeyboardLayout.KEYCODE_CAPLOCK);
            }
        };
        mainHandler.postDelayed(longPressRunnable, LONG_PRESS_SHIFT_DELAY_MS);
    }

    /** Cancels any pending long-press runnable. */
    private void stopLongPress() {
        if (longPressRunnable != null) {
            mainHandler.removeCallbacks(longPressRunnable);
            longPressRunnable = null;
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Converts dp to pixels using the display density.
     *
     * @param dp value in density-independent pixels
     * @return value in pixels
     */
    private float dp(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     * Converts sp to pixels using the display font scale.
     *
     * @param sp value in scale-independent pixels
     * @return value in pixels
     */
    private float sp(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }
}
