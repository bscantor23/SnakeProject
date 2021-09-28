package com.example.snakeproject.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.example.snakeproject.Game;

import java.util.ArrayList;

public class Snake {

    // Principal Bitmap
    private Bitmap bm;

    // Bitmaps de Cabeza, Cuerpo y Cola
    private Bitmap bmHeadDown, bmHeadLeft, bmHeadRight, bmHeadUp;
    private Bitmap bmBodyVertical, bmBodyHorizontal;
    private Bitmap bmBodyBottomLeft, bmBodyBottomRight, bmBodyTopLeft, bmBodyTopRight;
    private Bitmap bmTailDown, bmTailRight, bmTailLeft, bmTailUp;

    // Cuerpo de la Snake como lista de PartSnake
    private ArrayList<PartSnake> body = new ArrayList<>();

    // Cantidad de elementos del cuerpo
    private int length;

    // Booleans para el control del movimiento
    private boolean moveUp, moveLeft, moveRight, moveDown;

    public Snake(Bitmap bm, int x, int y, int length) {
        this.bm = bm;
        this.length = length;

        // Cantidad de elementos a escoger en los sprites
        int w = bm.getWidth() / 5;
        int h = bm.getHeight() / 4;

        // Elementos de la primera fila de los sprites
        bmBodyBottomRight = Bitmap.createBitmap(bm, 0, 0, w, h);
        bmBodyHorizontal = Bitmap.createBitmap(bm, 1 * w, 0, w, h);
        bmBodyBottomLeft = Bitmap.createBitmap(bm, 2 * w,  0, w, h);
        bmHeadUp = Bitmap.createBitmap(bm, 3 * w, 0, w, h);
        bmHeadRight = Bitmap.createBitmap(bm, 4 * w, 0, w, h);

        // Elementos de la segunda fila de los sprites
        bmBodyTopRight = Bitmap.createBitmap(bm, 0, h, w, h);
        bmBodyVertical = Bitmap.createBitmap(bm, 2 * w, h, w, h);
        bmHeadLeft = Bitmap.createBitmap(bm, 3 * w, h, w, h);
        bmHeadDown = Bitmap.createBitmap(bm, 4 * w, h, w, h);

        // Elementos de la tercera fila de los sprites
        bmBodyTopLeft = Bitmap.createBitmap(bm, 2 * w, 2 * h, w, h);
        bmTailUp = Bitmap.createBitmap(bm, 3 * w, 2 * h, w, h);
        bmTailRight = Bitmap.createBitmap(bm, 4 * w, 2 * h, w, h);

        // Elementos de la cuarta fila de los sprites
        bmTailLeft = Bitmap.createBitmap(bm, 3 * w, 3 * h, w, h);
        bmTailDown = Bitmap.createBitmap(bm, 4 * w, 3 * h, w, h);

        // Al inicializar se setea que la snake comience en dirección hacia la derecha
        moveRight = true;

        // Partes del cuerpo en dirección hacia la derecha
        body.add(new PartSnake(bmHeadRight, x, y));
        for (int i = 1; i < length - 1; i++) {
            this.body.add(new PartSnake(bmBodyHorizontal, this.body.get(i - 1).getX() - Game.size, y));
        }
        body.add(new PartSnake(bmTailRight, body.get(length - 2).getX() - Game.size, body.get(length - 2).getY()));
    }


    //Dibujar en Canvas
    public void drawSnake(Canvas canvas) {
        for (int i = length - 1; i >= 0; i--) {
            canvas.drawBitmap(body.get(i).getBm(), body.get(i).getX(), body.get(i).getY(), null);
        }
    }

    public void movements() {
        this.moveRight = false;
        this.moveDown = false;
        this.moveLeft = false;
        this.moveUp = false;
    }

    public void addPart() {
        PartSnake p = this.body.get(length - 1);

        this.length += 1;

        if (p.getBm() == bmTailRight) {
            this.body.add(new PartSnake(bmTailRight, p.getX() - Game.size, p.getY()));
        } else if (p.getBm() == bmTailLeft) {
            this.body.add(new PartSnake(bmTailLeft, p.getX() + Game.size, p.getY()));
        } else if (p.getBm() == bmTailUp) {
            this.body.add(new PartSnake(bmTailUp, p.getX(), p.getY() + Game.size));
        } else if (p.getBm() == bmTailDown) {
            this.body.add(new PartSnake(bmTailUp, p.getX(), p.getY() - Game.size));
        }
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    public Bitmap getBmHeadDown() {
        return bmHeadDown;
    }

    public void setBmHeadDown(Bitmap bmHeadDown) {
        this.bmHeadDown = bmHeadDown;
    }

    public Bitmap getBmHeadLeft() {
        return bmHeadLeft;
    }

    public void setBmHeadLeft(Bitmap bmHeadLeft) {
        this.bmHeadLeft = bmHeadLeft;
    }

    public Bitmap getBmHeadRight() {
        return bmHeadRight;
    }

    public void setBmHeadRight(Bitmap bmHeadRight) {
        this.bmHeadRight = bmHeadRight;
    }

    public Bitmap getBmHeadUp() {
        return bmHeadUp;
    }

    public void setBmHeadUp(Bitmap bmHeadUp) {
        this.bmHeadUp = bmHeadUp;
    }

    public Bitmap getBmBodyVertical() {
        return bmBodyVertical;
    }

    public void setBmBodyVertical(Bitmap bmBodyVertical) {
        this.bmBodyVertical = bmBodyVertical;
    }

    public Bitmap getBmBodyHorizontal() {
        return bmBodyHorizontal;
    }

    public void setBmBodyHorizontal(Bitmap bmBodyHorizontal) {
        this.bmBodyHorizontal = bmBodyHorizontal;
    }

    public Bitmap getBmBodyBottomLeft() {
        return bmBodyBottomLeft;
    }

    public void setBmBodyBottomLeft(Bitmap bmBodyBottomLeft) {
        this.bmBodyBottomLeft = bmBodyBottomLeft;
    }

    public Bitmap getBmBodyBottomRight() {
        return bmBodyBottomRight;
    }

    public void setBmBodyBottomRight(Bitmap bmBodyBottomRight) {
        this.bmBodyBottomRight = bmBodyBottomRight;
    }

    public Bitmap getBmBodyTopLeft() {
        return bmBodyTopLeft;
    }

    public void setBmBodyTopLeft(Bitmap bmBodyTopLeft) {
        this.bmBodyTopLeft = bmBodyTopLeft;
    }

    public Bitmap getBmBodyTopRight() {
        return bmBodyTopRight;
    }

    public void setBmBodyTopRight(Bitmap bmBodyTopRight) {
        this.bmBodyTopRight = bmBodyTopRight;
    }

    public Bitmap getBmTailUp() {
        return bmTailUp;
    }

    public void setBmTailUp(Bitmap bmTailUp) {
        this.bmTailUp = bmTailUp;
    }

    public Bitmap getBmTailDown() {
        return bmTailDown;
    }

    public void setBmTailDown(Bitmap bmTailDown) {
        this.bmTailDown = bmTailDown;
    }

    public Bitmap getBmTailRight() {
        return bmTailRight;
    }

    public void setBmTailRight(Bitmap bmTailRight) {
        this.bmTailRight = bmTailRight;
    }

    public Bitmap getBmTailLeft() {
        return bmTailLeft;
    }

    public void setBmTailLeft(Bitmap bmTailLeft) {
        this.bmTailLeft = bmTailLeft;
    }

    public ArrayList<PartSnake> getBody() {
        return body;
    }

    public void setBody(ArrayList<PartSnake> body) {
        this.body = body;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isMoveLeft() {
        return moveLeft;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.movements();
        this.moveLeft = moveLeft;
    }

    public boolean isMoveRight() {
        return moveRight;
    }

    public void setMoveRight(boolean moveRight) {
        this.movements();
        this.moveRight = moveRight;
    }

    public boolean isMoveUp() {
        return moveUp;
    }

    public void setMoveUp(boolean moveUp) {
        this.movements();
        this.moveUp = moveUp;
    }

    public boolean isMoveDown() {
        return moveDown;
    }

    public void setMoveDown(boolean moveDown) {
        this.movements();
        this.moveDown = moveDown;
    }
}