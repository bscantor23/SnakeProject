package com.example.snakeproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Canvas;

import androidx.annotation.Nullable;


public class Game extends View {


    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    private int size = 75 * SCREEN_WIDTH / 1080;
    private int w = 12, h = 21;

    private Bitmap bmGrassLight, bmGrassDark;
    private Grass [][] grass = new Grass[h][w];

    public Game(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        bmGrassLight = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmGrassLight.eraseColor(Color.parseColor("#7EAB85"));

        bmGrassDark = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmGrassDark.eraseColor(Color.parseColor("#527156"));

        boolean color = true;
        for(int i=0; i<h; i++){
            for(int j=0; j<w; j++){
                grass[i][j] = new Grass((color) ? bmGrassLight : bmGrassDark,j * size + SCREEN_WIDTH / 2 - (w / 2) * size, i * size + 50 * SCREEN_HEIGHT / 1920, size, size);
                color = !color;
            }
            color = !color;
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        for(int i=0; i<h; i++){
            for(int j=0; j<w; j++){
                Grass current = grass[i][j];
                canvas.drawBitmap(current.getBm(),current.getX(),current.getY(),null);
            }
        }
    }
}
