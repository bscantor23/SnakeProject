package com.example.snakeproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.snakeproject.object.Player;
import com.example.snakeproject.object.Room;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "firestore";

    public static ListenerRegistration registration;

    public static final String GAME_SETTINGS = "games";
    public static final String BEST_SCORE = "best";

    public static Dialog dialogScore;
    public static TextView txtScore, txtBest,txtDialogScore,txtDialogBest;
    public Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkRooms();
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

    public void checkRooms(){
        registration = FirebaseFirestore.getInstance()
                .collection("rooms")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: ", e);
                            return;
                        }
                        if (queryDocumentSnapshots != null) {
                            Log.d(TAG, "onEvent: ---------------------------");
                            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                            boolean inRoom = false;
                            for (DocumentSnapshot snapshot : snapshotList) {
                                Log.d(TAG, "onEvent: " + snapshot.getData());
                                if(((List<Object>) snapshot.getData().get("players")).size() == 1){
                                    inRoom = true;
                                    DocumentReference roomRef = snapshot.getReference();
                                    roomRef.update("players", FieldValue.arrayUnion(new Player(2,0,0,0)));
                                    registration.remove();
                                }
                            }

                            if(!inRoom){
                                registration.remove();
                                String id = FirebaseFirestore.getInstance().collection("rooms").document().getId();
                                Room newRoom = new Room("waiting");
                                FirebaseFirestore.getInstance().collection("rooms").document(id)
                                        .set(newRoom)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });
                            }
                        } else {
                            Log.e(TAG, "onEvent: query snapshot was null");
                        }
                    }
                });
    }


    public void getAllSnakesWithRealtimeUpdates() {



        FirebaseFirestore.getInstance()
                .collection("snakes")
                .document("f6cnCGsjS4ksD0WTtHhk")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: ",e );
                            return;
                        }
                        if (documentSnapshot != null) {
                            Log.d(TAG, "onEvent: -------------------");
                            Log.d(TAG, "onEvent: " + documentSnapshot.getData());
                        } else {
                            Log.e(TAG, "onEvent: NULL");
                        }
                    }
                });

    }



    //Desplegar ventana dialog
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