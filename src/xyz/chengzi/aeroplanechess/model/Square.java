package xyz.chengzi.aeroplanechess.model;

public class Square {
    private final ChessBoardLocation location;
    private ChessPiece piece;
    public int number_Of_Planes;

    public Square(ChessBoardLocation location) {
        this.location = location;
    }

    public ChessBoardLocation getLocation() {
        return location;
    }

    public ChessPiece getPiece() {
        return piece;
    }

    public int getNumber_Of_Planes(){
        return number_Of_Planes;
    }

    public void setPiece(ChessPiece piece) {
        this.piece = piece;
    }
}
