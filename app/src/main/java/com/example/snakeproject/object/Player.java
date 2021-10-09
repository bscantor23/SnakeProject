package com.example.snakeproject.object;

import android.graphics.Point;

public class Player extends Point{
    private int id;
    private int score;

    public Player() {
    }

    public Player(int id, int score, int x, int y) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
