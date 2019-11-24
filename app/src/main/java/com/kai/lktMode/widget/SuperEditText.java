package com.kai.lktMode.widget;

import android.content.*;
import android.widget.*;
import android.graphics.*;
import android.util.*;
import android.text.*;
import android.view.*;

import androidx.appcompat.widget.AppCompatEditText;

import com.kai.lktMode.R;

public class SuperEditText extends AppCompatEditText {
    private Paint line;
    private Context context;
    public SuperEditText(Context context, AttributeSet As) {
        super(context, As);
        this.context=context;
        setFocusable(true);
        setLongClickable(true);
        line = new Paint();
        line.setColor(getResources().getColor(R.color.colorPrimaryDark));
        line.setStrokeWidth(2);
        setPadding(60, 0, 0, 0);
        setGravity(Gravity.TOP);
    }
    @Override
    protected void onDraw(final Canvas canvas) {
        int k=getLineHeight();
        int i=getLineCount();
        canvas.drawRect(0, 10, 0, getHeight() + (i * k), line);
//int y=(getLayout().getLineForOffset(getSelectionStart()) + 1) * k;
// canvas.drawLine(0, y, getWidth(), y, line);
        canvas.save();
        if (getText().toString().length() != 0) {
            float y=0;
            Paint p=new Paint();
            p.setColor(getResources().getColor(R.color.colorPrimaryDark));
            p.setTextSize(25);
            for (int l=0;l < getLineCount();l++) {
                if (getCurrentCursorLine(this) == l + 1) {
                    p.setColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    p.setColor(Color.GRAY);
                }

                y = ((l + 1) * getLineHeight()) - (getLineHeight() / 4);
                canvas.drawText(String.valueOf(l + 1), 8, y+7, p);
                canvas.save();
            }
        }
        canvas.restore();
        super.onDraw(canvas);
    }

    private int getCurrentCursorLine(EditText editText) {
        int selectionStart = Selection.getSelectionStart(editText.getText());
        Layout layout = editText.getLayout();
        if (selectionStart != -1) {
            return layout.getLineForOffset(selectionStart) + 1;
        }
        return -1;
    }
}
