package xyz.chengzi.aeroplanechess.model;

public class ChessPiece {
    private final int player;
    // 0 not finished, 1 finished
    private final int landed;

    public ChessPiece(int player, int finished) {
        this.player = player;
        this.landed = finished;
    }

    public int getPlayer() {
        return player;
    }

    public int finished(){
        return landed;
    }
}
