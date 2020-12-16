package xyz.chengzi.aeroplanechess.model;

import java.util.*;

public class Snapshot {
    public int Player;
    public int Color;
    public int Index;
    public int NunmberOfPlanes;
    public int Finished;

    public Snapshot(int player, int color, int index, int nunmberOfPlanes, int finished) {
        this.Player = player;
        this.Color = color;
        this.Index = index;
        this.NunmberOfPlanes = nunmberOfPlanes;
        this.Finished = finished;
    }

    public int getPlayer() {
        return Player;
    }

    public int getColor() {
        return Color;
    }

    public int getIndex() {
        return Index;
    }

    public int getNunmberOfPlanes() {
        return NunmberOfPlanes;
    }

    public int getFinished() {
        return Finished;
    }
}

