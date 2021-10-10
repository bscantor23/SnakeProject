package com.example.snakeproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // TAG LOGCAT
    private static final String TAG = "firestore";

    // STORAGE VARIABLES
    public static final String GAME_SETTINGS = "games";
    public static final String BEST_SCORE = "best";

    public static Dialog dialogScore,dialogRooms, dialogGame;
    public static TextView txtScore, txtBest,txtDialogScore,txtDialogBest;
    public Game game;

    // LIST ITEMS
    public ListView list;
    public List<Rlist> mAdapter = new ArrayList();

    // NECCESARY VARIABLES
    public Activity context = this;
    public String roomCode;
    public boolean host = false;
    public DocumentSnapshot gameInfo;

    // STOP SNAPSHOTS
    public static ListenerRegistration registration, room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SET SCREEN SIZES
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        setContentView(R.layout.activity_main);

        game = findViewById(R.id.game);

        // Revisar dimensionamiento de la pantalla para setear medidas del tablero de juego
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
        initialDialog();
    }

    // Desplegar ventana dialog
    private void initialDialog() {

        Button rl_start = dialogScore.findViewById(R.id.rl_start);
        // Cuando pulse el botón de start resetear el game.
        rl_start.setOnClickListener(v -> {
            setSinglePlayerGame();
            game.reset();
            dialogScore.dismiss();
        });

        Button rl_multiplayer = dialogScore.findViewById(R.id.rl_multiplayer);

        // Cuando pulse el botón de start multiplayer mostrar ventana de lista de salas.
        rl_multiplayer.setOnClickListener(v -> {
            dialogRooms();
        });

        dialogScore.show();
    }

    private void setSinglePlayerGame(){
        int bestScore = 0;
        // Tomar en cuenta anterior partida
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
    }

    // Desplegar ventana rooms
    private void dialogRooms() {
        host = false;
        dialogRooms = new Dialog(this);
        dialogRooms.setContentView(R.layout.activity_rooms);

        // Snapshot listener collections rooms
        registration = FirebaseFirestore.getInstance().collection("rooms")
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    // Si existe un error
                    if (e != null) {
                        Log.e(TAG, "onEvent: ", e);
                        return;
                    }

                    AdapterRoomList adapter2 = new AdapterRoomList(context, new ArrayList());
                    list = dialogRooms.findViewById(R.id.list_rooms);
                    list.setAdapter(adapter2);

                    // Si existen colecciones
                    if (queryDocumentSnapshots != null) {
                        mAdapter = new ArrayList(); // Inicializar adaptador
                        Rlist match;
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot : snapshotList) {

                            // Añadir nuevo elemento al ListView
                            match = new Rlist();
                            match.setCod(snapshot.getId());
                            match.setState((String) snapshot.getData().get("state"));
                            match.setPlayers(((List<Object>) snapshot.getData().get("players")).size());
                            mAdapter.add(match);
                            adapter2 = new AdapterRoomList(context, mAdapter);
                            list = dialogRooms.findViewById(R.id.list_rooms);
                            list.setAdapter(adapter2);

                            itemTouch();
                        }
                    } else {
                        Log.e(TAG, "onEvent: query snapshot was null");
                    }
                }
            });

        Button rl_new_room = dialogRooms.findViewById(R.id.rl_new_room);

        // Escucha evento onclick de botón "Nueva Partida"
        rl_new_room.setOnClickListener(v -> {
            registration.remove();  // Eliminar snapshot listener
            String id = FirebaseFirestore.getInstance().collection("rooms").document().getId();
            Room newRoom = new Room("En espera");

            // Insertar nuevo documento de sala
            FirebaseFirestore.getInstance().collection("rooms").document(id)
                .set(newRoom)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        host = true;
                        dialogGame(id); // Mostrar diálogo de juego
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        });

        dialogRooms.show();
        Window window = dialogRooms.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    }

    private void itemTouch() {
        // Identificar item pulsado
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Si existen dos jugadores
                if(mAdapter.get(position).getPlayers() == 2){
                    // Mostrar alerta de capacidad
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Capacidad de jugadores alcanzada").setTitle("Alert Dialog");
                    builder.setNeutralButton("Entendido", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i){
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                // Si existe un sólo jugador
                if(mAdapter.get(position).getPlayers() == 1){
                    // Actualizar documento
                    DocumentReference roomRef = FirebaseFirestore.getInstance()
                            .collection("rooms")
                            .document(mAdapter.get(position).getCod());
                    // Actualizar array
                    roomRef.update("players", FieldValue.arrayUnion(new Player(2,0,0,0)));
                    // Actualizar estado
                    roomRef.update("state","Sala preparada");
                    // Eliminar snapshot listener
                    registration.remove();
                    host = false;
                    // Mostrar diálogo de juego
                    dialogGame(mAdapter.get(position).getCod());
                }
            }
        });
    }

    // Desplegar ventana de juego
    private void dialogGame(String code) {
        dialogGame = new Dialog(this);
        dialogGame.setCanceledOnTouchOutside(false);
        dialogGame.setContentView(R.layout.dialog_room);
        dialogRooms.dismiss();

        // Cambio estado de la sala
        TextView txtState = dialogGame.findViewById(R.id.txt_rol);
        txtState.setText(host ? "Host" : "Inv");

        // Snapshopt listener de cambios de la sala
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(code);
        room = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                // Si existen errores
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if(snapshot.getData() == null){
                    // Mostrar alerta de capacidad
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Partida finalizada").setTitle("Alert Dialog");
                    builder.setNeutralButton("Entendido", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i){
                            dialogInterface.dismiss();
                            room.remove();
                            dialogGame.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                // Si existe un cambio
                if (snapshot != null && snapshot.exists()) {
                    gameInfo = snapshot;

                    // Cambio nombre de la sala
                    TextView txtCode = dialogGame.findViewById(R.id.txt_room_play_code);
                    txtCode.setText(snapshot.getId().substring(0,4).toUpperCase(Locale.ROOT));

                    // Cambio estado de la sala
                    TextView txtState = dialogGame.findViewById(R.id.txt_state);
                    txtState.setText((String) snapshot.getData().get("state"));
                    dialogGame.show();

                    Log.d(TAG, "Current data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        roomCode = code; // Guardar código de la partida

        // Eliminar partida si se sale
        Button exit = dialogGame.findViewById(R.id.room_exit);
        exit.setOnClickListener(v -> {
            alertDialogDelete();
        });

        dialogGame.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == 1) {
                    alertDialogDelete();
                }
                return true;
            }
        });

    }

    private void alertDialogDelete() {
        // Mostrar alerta de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(host ? "¿Deseas finalizar la sala?" : "¿Deseas salir de la sala?").setTitle("Alert Dialog");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
                dialogInterface.dismiss();
                if(host){
                    deleteRoom();
                }else{
                   exitRoom();
                }
                dialogGame.dismiss();
                room.remove();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void exitRoom() {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(roomCode);
        docRef.update("players", FieldValue.arrayRemove(((List<Map>) gameInfo.get("players")).get(1)));
        docRef.update("state","En espera");
    }

    private void deleteRoom() {
        System.out.println(roomCode);
        FirebaseFirestore.getInstance().collection("rooms").document(roomCode)
            .delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error deleting document", e);
                }
            });
    }
}