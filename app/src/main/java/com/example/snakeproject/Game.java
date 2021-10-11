package com.example.snakeproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.snakeproject.object.Apple;
import com.example.snakeproject.object.Grass;
import com.example.snakeproject.object.PartSnake;
import com.example.snakeproject.object.Player;
import com.example.snakeproject.object.Snake;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Game extends View {

    public final int DELAY = 200;

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    public static int size;

    // Cantidad de celdas visibles y disponibles en el Game
    private int w = 12, h = 17;

    private Bitmap bmGrassLight, bmGrassDark, bmSprites;
    private Grass[][] grass = new Grass[h][w];

    private Snake snake, opponentSnake;
    private Apple apple;

    private Handler handler;
    private Runnable runnable;

    private boolean move = false;
    private float mx, my;

    public static boolean isPlaying = false;
    public static int score = 0;
    public static int bestScore = 0;

    private Context context;

    private QuerySnapshot infoGame;
    private int modGame;
    private boolean host;
    private String roomCode;

    public static ListenerRegistration opponent;

    public Game(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.infoGame = null;
        loadBest(context);
    }

    public void init() {
        Game.size = 90 * SCREEN_WIDTH / 1080;

        // Estilo de Celda con fondo Claro
        bmGrassLight = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmGrassLight.eraseColor(Color.parseColor("#B5835E"));

        // Estilo de Celda con fondo Oscuro
        bmGrassDark = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmGrassDark.eraseColor(Color.parseColor("#A87854"));

        // Sprites dispuestos para el juego.
        bmSprites = BitmapFactory.decodeResource(this.getResources(), R.drawable.sprites);
        bmSprites = Bitmap.createScaledBitmap(bmSprites, 5 * size, 4 * size, true);

        reset(0);

        //Inicialización del hilo
        handler = new Handler();
        runnable = () -> invalidate();
    }

    public void reset(int game) {
        modGame = game;
        System.gc();

        // Adaptación de los estilos en la matriz
        boolean color = true;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                grass[i][j] = new Grass((color) ? bmGrassLight : bmGrassDark, j * size + SCREEN_WIDTH / 2 - (w / 2) * size, i * size + 50 * SCREEN_HEIGHT / 1920, size, size);
                color = !color;
            }
            color = !color;
        }

        if (modGame == 0) {
            //Creación del Snake con posición inicial en el centro del juego
            snake = new Snake(bmSprites, grass[9][6].getX(), grass[9][6].getY(), 4);
        }

        if (modGame == 1 && infoGame != null) {
            //Creación del Snake con posición inicial en el centro del juego

            for (DocumentSnapshot player : infoGame) {
                if(((String) player.getId()).equals("1")){
                    snake = new Snake(bmSprites,
                            grass[((Long) player.get("y")).intValue()][((Long) player.get("x")).intValue()].getX(),
                            grass[((Long) player.get("y")).intValue()][((Long) player.get("x")).intValue()].getY(),
                            4);
                    snake.setScore(((Long) player.get("score")).intValue());
                    snake.setId(((Long) player.get("id")).intValue());
                }
                if(((String) player.getId()).equals("2")){
                    opponentSnake = new Snake(bmSprites,
                            grass[((Long) player.get("y")).intValue()][((Long) player.get("x")).intValue()].getX(),
                            grass[((Long) player.get("y")).intValue()][((Long) player.get("x")).intValue()].getY(),
                            4);
                    opponentSnake.setScore(((Long) player.get("score")).intValue());
                    opponentSnake.setId(((Long) player.get("id")).intValue());
                }
            }

        }

        //Creación del Apple con posición inicial random
        Point random = randomApple();
        apple = new Apple(bmSprites, grass[random.y][random.x].getX(), grass[random.y][random.x].getY());

        score = 0;
        MainActivity.txtScore.setText(score + "");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (size == 0) {
            return;
        }

        canvas.drawColor(context.getResources().getColor(R.color.green_bg));

        // Dibujado del Background
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Grass current = grass[i][j];
                canvas.drawBitmap(current.getBm(), current.getX(), current.getY(), null);
            }
        }

        PartSnake head = snake.getBody().get(0);

        if (isPlaying) {

            snake.updateMovement();
            head = snake.getBody().get(0);

            // Validar si la cabeza sale del tablero de juego
            if (head.getX() < grass[0][0].getX()
                    || head.getY() < grass[0][0].getY()
                    || head.getY() + size > grass[h - 1][w - 1].getY() + size
                    || head.getX() + size > grass[h - 1][w - 1].getX() + size) {
                gameOver();
            }

            //Validar si la cabeza toca alguna parte del cuerpo
            for (int i = 1; i < snake.getBody().size(); i++) {
                if (head.getrBody().intersect(snake.getBody().get(i).getrBody())) {
                    gameOver();
                }
            }
        }

        // Dibujado del Snake y del Apple
        snake.drawSnake(canvas);
        apple.draw(canvas);

        // Validar que la serpiente se alimenta
        if (head.getrBody().intersect(apple.getR())) {

            // Setear una nueva posición a una nueva manzana
            Point random = randomApple();
            apple.reset(grass[random.y][random.x].getX(), grass[random.y][random.x].getY());

            //Añadir una nueva parte
            snake.addPart();
            score++;

            MainActivity.txtScore.setText(score + "");
            if (score > bestScore) {
                bestScore = score;
                saveBest();
                MainActivity.txtBest.setText(bestScore + "");
            }

        }

        if (modGame == 1 && isPlaying) {

            DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(roomCode).collection("players").document("1");
            docRef.update("players", FieldValue.arrayUnion(snake.getSnake()));
        }

        //Delay del movimiento de la Snake
        handler.postDelayed(runnable, DELAY);

    }

    // Guardar la información de la mejor puntuación de partida obtenida
    private void loadBest(@NonNull Context context) {
        SharedPreferences sp = context.getSharedPreferences(MainActivity.GAME_SETTINGS, Context.MODE_PRIVATE);
        if (sp != null) {
            bestScore = sp.getInt(MainActivity.BEST_SCORE, 0);
        }
    }

    // Storage interno de la información de puntuación de partida
    private void saveBest() {
        SharedPreferences sp = context.getSharedPreferences(MainActivity.GAME_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(MainActivity.BEST_SCORE, bestScore);
        editor.apply();
    }

    // Finalizar juego y mostrar Dialog
    private void gameOver() {
        isPlaying = false;
        if(modGame == 0){
            MainActivity.dialogScore.show();
        }else{
            MainActivity.dialogGame.show();
            resetMultiplayer();
        }

        MainActivity.txtDialogBest.setText(bestScore + "");
        MainActivity.txtDialogScore.setText(score + "");
    }

    private void resetMultiplayer(){
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(roomCode);
        Map<String,Object> updates = new HashMap<>();
        updates.put("players", FieldValue.delete());
        docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                List<Map> players = new ArrayList<Map>();
                players.add(snake.getPredSnake());
                Map<String,Object> field  = new HashMap<>();
                field.put("state","Sala preparada");
                field.put("players",players);
                docRef.set(field);
            }
        });


    }

    // Generar posición aleatoria de la manzana
    private Point randomApple() {
        Point point = new Point();
        Random r = new Random();
        point.y = r.nextInt(h - 1);
        point.x = r.nextInt(w - 1);
        Grass current = grass[point.y][point.x];

        // Algoritmo para verificar que la nueva posición de la manzana no tenga una parte del cuerpo de la serpiente
        Rect rect = new Rect(current.getX(), current.getY(), current.getX() + size, current.getY() + size);
        boolean check = true;
        while (check) {
            check = false;
            for (int i = 0; i < snake.getBody().size(); i++) {
                if (rect.intersect(snake.getBody().get(i).getrBody())) {
                    check = true;
                    point.y = r.nextInt(h - 1);
                    point.x = r.nextInt(w - 1);
                    rect = new Rect(current.getX(), current.getY(), current.getX() + size, current.getY() + size);
                }
            }
        }
        return point;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int a = event.getActionMasked();
        switch (a) {
            // Cuando realiza un touch
            case MotionEvent.ACTION_MOVE: {
                if (move == false) {
                    mx = event.getX();
                    my = event.getY();
                    move = true;
                } else {
                    // Si el movimiento es hacia la izquierda
                    if (mx - event.getX() > 100 && !snake.isMoveRight()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMoveLeft(true);
                        isPlaying = true;
                        // Si el movimiento es hacia la derecha
                    } else if (event.getX() - mx > 100 && !snake.isMoveLeft()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMoveRight(true);
                        isPlaying = true;
                        // Si el movimiento es hacia abajo
                    } else if (event.getY() - my > 100 && !snake.isMoveUp()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMoveDown(true);
                        isPlaying = true;
                        // Si el movimiento es hacia arriba
                    } else if (my - event.getY() > 100 && !snake.isMoveDown()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMoveUp(true);
                        isPlaying = true;
                    }
                }
                break;
            }
            // Cuando suelta el touch
            case MotionEvent.ACTION_UP: {
                mx = 0;
                my = 0;
                move = false;
                break;
            }
        }
        return true;
    }

    public QuerySnapshot getInfoGame() {
        return infoGame;
    }

    public void setInfoGame(QuerySnapshot infoGame,String roomCode, boolean host) {
        this.infoGame = infoGame;
        this.host = host;
        this.roomCode = roomCode;
    }
}
