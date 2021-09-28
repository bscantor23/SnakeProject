package com.example.snakeproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Canvas;

import androidx.annotation.Nullable;

import com.example.snakeproject.object.Apple;
import com.example.snakeproject.object.Grass;
import com.example.snakeproject.object.Snake;


public class Game extends View {

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    public static int size;

    // Cantidad de celdas visibles y disponibles en el Game
    private int w = 12, h = 21;

    private Bitmap bmGrassLight, bmGrassDark, bmSprites;
    private Grass[][] grass = new Grass[h][w];

    private Snake snake;
    private Apple apple;

    public Game(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        Game.size = 75 * SCREEN_WIDTH / 1080;

        // Estilo de Celda con fondo Claro
        bmGrassLight = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmGrassLight.eraseColor(Color.parseColor("#B5835E"));

        // Estilo de Celda con fondo Oscuro
        bmGrassDark = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmGrassDark.eraseColor(Color.parseColor("#694F3C"));

        // Sprites dispuestos para el juego.
        bmSprites = BitmapFactory.decodeResource(this.getResources(), R.drawable.sprites);
        bmSprites = Bitmap.createScaledBitmap(bmSprites, 5*size, 4*size,true);

        boolean color = true;

        // Adaptación de los estilos en la matriz
        for(int i=0; i<h; i++){
            for(int j=0; j<w; j++){
                grass[i][j] = new Grass((color) ? bmGrassLight : bmGrassDark,j * size + SCREEN_WIDTH / 2 - (w / 2) * size, i * size + 50 * SCREEN_HEIGHT / 1920, size, size);
                color = !color;
            }
            color = !color;
        }

        //Creación del Snake con posición inicial en el centro del juego
        snake = new Snake(bmSprites,grass[10][6].getX(), grass[10][6].getY(), 4);

        //Creación del Apple con posición inicial random
        Point random = randomApple();
        apple = new Apple(bmSprites, grass[random.y][random.x].getX(), grass[random.y][random.x].getY());

    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        // Dibujado del Background
        for(int i=0; i<h; i++){
            for(int j=0; j<w; j++){
                Grass current = grass[i][j];
                canvas.drawBitmap(current.getBm(),current.getX(),current.getY(),null);
            }
        }

        // Dibujado del Snake y del Apple
        snake.drawSnake(canvas);
        apple.draw(canvas);
    }

    private Point randomApple(){
        return new Point(1,1);
    }

}
