package com.example.snakeproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SET SCREEN SIZES
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        //Guardar como atributos estaticos las medidas de la pantalla
        Game.SCREEN_WIDTH = dm.widthPixels ;
        Game.SCREEN_HEIGHT = dm.heightPixels;

        setContentView(R.layout.activity_main);
    }

    public void onClickReset(View view) {
    }
}