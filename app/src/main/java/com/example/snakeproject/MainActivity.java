package com.example.snakeproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.snakeproject.object.Player;
import com.example.snakeproject.object.Room;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "firestore";
    public static final String GAME_SETTINGS = "games";
    public static final String BEST_SCORE = "best";

    public static Dialog dialogScore,dialogRooms;
    public static TextView txtScore, txtBest,txtDialogScore,txtDialogBest;
    public Game game;

    public ListView list;
    public List<Rlist> mAdapter = new ArrayList();
    public Activity context = this;
    public static ListenerRegistration registration;

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
        Button rl_start = dialogScore.findViewById(R.id.rl_start);

        //Cuando pulse el botón de start resetear el game.
        rl_start.setOnClickListener(v -> {
            game.reset();
            dialogScore.dismiss();
        });

        Button rl_multiplayer = dialogScore.findViewById(R.id.rl_multiplayer);

        //Cuando pulse el botón de start resetear el game.
        rl_multiplayer.setOnClickListener(v -> {
            dialogRooms();
        });

        dialogScore.show();
    }

    //Desplegar ventana rooms
    private void dialogRooms() {
        dialogRooms = new Dialog(this);
        mAdapter = new ArrayList();
        dialogRooms.setContentView(R.layout.activity_rooms);
        registration = FirebaseFirestore.getInstance().collection("rooms")
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.e(TAG, "onEvent: ", e);
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        Rlist match;
                        Log.d(TAG, "onEvent: ---------------------------");
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        boolean inRoom = false;
                        for (DocumentSnapshot snapshot : snapshotList) {
                            Log.d(TAG, "onEvent: " + ((List<Object>) snapshot.getData().get("players")).size());
                            match = new Rlist();
                            match.setCod(snapshot.getId());
                            match.setPlayers(((List<Object>) snapshot.getData().get("players")).size());
                            mAdapter.add(match);

                            AdapterRoomList adapter2 = new AdapterRoomList(context, mAdapter);
                            list = (ListView) dialogRooms.findViewById(R.id.list_rooms);
                            list.setAdapter(adapter2);
                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    System.out.println(mAdapter.get(position).getCod());
                                    DocumentReference roomRef = FirebaseFirestore.getInstance()
                                            .collection("rooms")
                                            .document(mAdapter.get(position).getCod());
                                    roomRef.update("players", FieldValue.arrayUnion(new Player(2,0,0,0)));
                                    registration.remove();
                                }
                            });

                            dialogRooms.show();
                            Window window = dialogRooms.getWindow();
                            window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        }
                    } else {
                        Log.e(TAG, "onEvent: query snapshot was null");
                    }
                }
            });

        Button rl_new_room = dialogRooms.findViewById(R.id.rl_new_room);

        //Cuando pulse el botón de start resetear el game.
        rl_new_room.setOnClickListener(v -> {
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
        });

    }
}