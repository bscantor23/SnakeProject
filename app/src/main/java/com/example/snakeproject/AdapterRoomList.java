package com.example.snakeproject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AdapterRoomList extends BaseAdapter {
    protected Activity activity;
    protected List<Rlist> item;

    public AdapterRoomList(Activity activity, List<Rlist> item){
        this.activity = activity;
        this.item=item;
    }

    public void clear(){item.clear();}
    @Override
    public int getCount() {
        return item.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.item_layout, null);
            Rlist dir = item.get(position);
            if (!dir.getCod().isEmpty()) {
                TextView cod, players;
                cod = (TextView) v.findViewById(R.id.txt_room_code);
                players = (TextView) v.findViewById(R.id.txt_num_players);
                cod.setText(dir.getCod().substring(0,4));
                players.setText(String.valueOf(dir.getPlayers()));
            }
        }
        return v;
    }
}
