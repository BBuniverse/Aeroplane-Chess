package xyz.chengzi.aeroplanechess.model;

public class ChessPiece {
    private final int player;
    // 0 not finished, 1 finished
    public final int landed;

    // 0 not moved, 1 moved
    public int moved;

    public ChessPiece(int player, int finished, int moved) {
        this.player = player;
        this.landed = finished;
        this.moved = moved;
    }

    public int getPlayer() {
        return player;
    }

    public int finished(){
        return landed;
    }

    public int getMoved() { return moved; }
}
