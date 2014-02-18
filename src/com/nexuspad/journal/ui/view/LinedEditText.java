package com.nexuspad.journal.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;
import com.nexuspad.R;

/**
 * User: edmond
 */
public class LinedEditText extends EditText {

    private final Rect mLineRect = new Rect();
    private Paint mLinePaint;

    public LinedEditText(Context context) {
        super(context);
        init();
    }

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(getResources().getColor(R.color.darker_blue));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Rect lineRect = mLineRect;
        final Paint linePaint = mLinePaint;

        final int viewHeight = getHeight();  // view height
        final int lineHeight = getLineHeight();  // line height (vertical space between each line)

        final int viewLinesCount = viewHeight / lineHeight;  // lines visible (truncate intended)
        final int lineCount = getLineCount();  // # of lines of text (maybe more than viewLinesCount)
        final int count = Math.max(lineCount, viewLinesCount);  // draw all the lines

        int yPixel = getLineBounds(0, lineRect);  // y-position of the line; init to the first line

        for (int i = 0; i < count; i++) {
            canvas.drawLine(lineRect.left, yPixel, lineRect.right, yPixel, linePaint);
            yPixel += lineHeight;  // for the next line
        }

        super.onDraw(canvas);  // do the TextView thing (draw text, etc.)
    }
}
