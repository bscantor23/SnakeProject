package com.example.snakeproject.object;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private String state;
    private ArrayList<Player> players = new ArrayList<Player>();

    public Room() {
    }

    public Room(String state) {
        this.state = state;
        this.players.add(new Player(1,0,0,0));
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }
}
