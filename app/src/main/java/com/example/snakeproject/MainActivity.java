package com.example.snakeproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String GAME_SETTINGS = "games";
    public static final String BEST_SCORE = "best";

    public static Dialog dialogScore;
    public static TextView txtScore, txtBest,txtDialogScore,txtDialogBest;
    public Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SET SCREEN SIZES
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        setContentView(R.layout.activity_main);

        game = findViewById(R.id.game);

        //Revisar dimensionamiento de la pantalla para setear medidas del tablero de juego
        game.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                MainActivity.this.game.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Game.SCREEN_WIDTH  =  MainActivity.this.game.getMeasuredWidth();
                Game.SCREEN_HEIGHT =  MainActivity.this.game.getMeasuredHeight();
                MainActivity.this.game.init();
            }
        });

        txtScore = findViewById(R.id.txtScore);
        txtBest = findViewById(R.id.txtBest);
        dialogScore();
    }

    //Despleguar ventana dialog
    private void dialogScore() {
        int bestScore = 0;
        //Tomar en cuenta anterior partida
        SharedPreferences sp = this.getSharedPreferences(GAME_SETTINGS, Context.MODE_PRIVATE);
        if(sp!=null){
            bestScore = sp.getInt(BEST_SCORE,0);
        }

        MainActivity.txtBest.setText(bestScore+"");

        dialogScore = new Dialog(this);
        dialogScore.setContentView(R.layout.dialog_start);
        txtDialogScore = dialogScore.findViewById(R.id.txt_dialog_score);
        txtDialogBest = dialogScore.findViewById(R.id.txt_dialog_best_score);
        txtDialogBest.setText(bestScore + "");
        dialogScore.setCanceledOnTouchOutside(false);
        RelativeLayout rl_start = dialogScore.findViewById(R.id.rl_start);

        //Cuando pulse el botón de start resetear el game.
        rl_start.setOnClickListener(v -> {
            game.reset();
            dialogScore.dismiss();
        });
        dialogScore.show();
    }

}