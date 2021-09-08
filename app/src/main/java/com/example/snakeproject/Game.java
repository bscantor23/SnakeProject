package com.example.snakeproject;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Canvas;

import androidx.annotation.Nullable;

public class Game extends View {

    static final TextPaint textPaint = new TextPaint();
    void setup(){
        textPaint.setColor(Color.RED);
        textPaint.setStrokeWidth(2f);
    }

    public Game(Context context) {
        super(context);
        setup();
    }

    public Game(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawColor(Color.GREEN);
    }
}
