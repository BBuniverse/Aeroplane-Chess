package xyz.chengzi.aeroplanechess.model;

public class Stack {
    private ChessPiece chessPiece;
    private ChessBoardLocation chessBoardLocation;
    public int planeQuantity;
    public Stack(ChessPiece chessPiece,ChessBoardLocation chessBoardLocation,int planeQuantity){
        this.chessPiece = chessPiece;
        this.chessBoardLocation = chessBoardLocation;
        this.planeQuantity = planeQuantity;
    }
}
