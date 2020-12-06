package xyz.chengzi.aeroplanechess.model;

import xyz.chengzi.aeroplanechess.listener.ChessBoardListener;
import xyz.chengzi.aeroplanechess.listener.Listenable;
import xyz.chengzi.aeroplanechess.controller.GameController;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ChessBoard implements Listenable<ChessBoardListener> {
//    this is the number of the players
    public int number_Players;
//    each color has 4 planes first.
    private final int INITIAL_PLANES = 4;
    private final List<ChessBoardListener> listenerList = new ArrayList<>();
    public final Square[][] grid;
    private final int dimension, endDimension;
    int[] movingList = {0, 10, 7, 4, 1, 11, 8, 5, 2, 12, 9, 6, 3};
    private static final String[] PLAYER_NAMES = {"Yellow", "Blue", "Green", "Red"};

    /**
     * @param dimension    13
     * @param endDimension 6
     */
    public ChessBoard(int dimension, int endDimension,int number_Players) {
        this.number_Players = number_Players;
        this.grid = new Square[INITIAL_PLANES][dimension + endDimension + INITIAL_PLANES];
        this.dimension = dimension;
        this.endDimension = endDimension;

        initGrid();
    }

    private void initGrid() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < dimension + endDimension+ INITIAL_PLANES; j++) {
                grid[i][j] = new Square(new ChessBoardLocation(i, j));
            }
        }
    }
//    FIXME: initialization
    public void placeInitialPieces() {
        for (int i = 0; i < INITIAL_PLANES; i++) {
            for (int j = 0; j < dimension + endDimension + INITIAL_PLANES; j++) {
                grid[i][j].setPiece(null);
            }
        }
//         FIXME: Demo implementation: initial for four planes at the start position
        for (int player = 0; player < number_Players ; player++) {
            for (int initial_Index = dimension + endDimension; initial_Index < dimension + endDimension + INITIAL_PLANES; initial_Index++) {
                grid[player][initial_Index].setPiece(new ChessPiece(player));
                ChessPiece piece = new ChessPiece(player);
                ChessBoardLocation location = grid[player][initial_Index].getLocation();
                this.setChessPieceAt(location, piece);
            }
        }
//        grid[0][0].setPiece(new ChessPiece(0));
        listenerList.forEach(listener -> listener.onChessBoardReload(this));
    }

    public Square getGridAt(ChessBoardLocation location) {
        return grid[location.getColor()][location.getIndex()];
    }

    public int getDimension() {
        return dimension;
    }

    public int getEndDimension() {
        return endDimension;
    }

    public ChessPiece getChessPieceAt(ChessBoardLocation location) {
        return getGridAt(location).getPiece();
    }

    public void setChessPieceAt(ChessBoardLocation location, ChessPiece piece) {
        getGridAt(location).setPiece(piece);
        listenerList.forEach(listener -> listener.onChessPiecePlace(location, piece));
    }

    public ChessPiece removeChessPieceAt(ChessBoardLocation location) {
        ChessPiece piece = getGridAt(location).getPiece();
        getGridAt(location).setPiece(null);
        listenerList.forEach(listener -> listener.onChessPieceRemove(location));
        return piece;
    }

    /**
     * @param src   Current chessboard
     * @param steps Moving steps
     */
    public void moveChessPiece(ChessBoardLocation src, int steps) {

        ChessBoardLocation dest = src;
        // FIXME: This just naively move the chess forward without checking anything
        int lColor, lIndex, player = getChessPieceAt(src).getPlayer();
        boolean readyToLand = false, landed = false;
        if (src.getIndex() > 18 ){
            if(steps == 6){
                dest = grid[player][0].getLocation();
                setChessPieceAt(dest, removeChessPieceAt(src));
            }
        }else{
            System.out.println("ChessBoard " + PLAYER_NAMES[player] + " got " + steps + " steps");
            for (int i = 1; i <= steps; i++) {
                // Ready to land
                if (dest.getIndex() >= 12 && player == dest.getColor()) {
                    if (landed) {
                        // Turn back
                        lIndex = dest.getIndex() - 1;
                    } else {
                        // Move forward to destination
                        lIndex = dest.getIndex() + 1;
                    }
                    dest = new ChessBoardLocation(player, lIndex);
                    if (dest.getIndex() == 18) {
                        landed = true;
                    }
                    readyToLand = true;
                } else {
                    int index = 0;
                    for (int j = 0; j < movingList.length; j++) {
                        if (movingList[j] == dest.getIndex()) {
                            index = j;
                            break;
                        }
                    }
                    lColor = (dest.getColor() + 1) % 4;
                    lIndex = movingList[(index + 1) % movingList.length];
                    boolean opponent = false;

                    dest = new ChessBoardLocation(lColor, lIndex);
                }
            }
            if (!readyToLand && player == dest.getColor()) {
                dest = nextLocation(dest);
            }
            if (getGridAt(dest).getPiece() != null && getGridAt(dest).getPiece().getPlayer() != player) {
                ChessBoardLocation opponent = new ChessBoardLocation(getGridAt(dest).getPiece().getPlayer(), 0);
                setChessPieceAt(opponent, removeChessPieceAt(dest));
                System.out.println("ChessBoard " + PLAYER_NAMES[player] + " sent " +
                        PLAYER_NAMES[getGridAt(opponent).getPiece().getPlayer()] + " back to hangar");
//                setChessPieceAt(grid[getGridAt(opponent).getPiece().getPlayer()][0].getLocation(),new ChessPiece(getGridAt(opponent).getPiece().getPlayer()));
//                setChessPieceAt(opponent, new ChessPiece(getGridAt(opponent).getPiece().getPlayer()));

            }
            setChessPieceAt(dest, removeChessPieceAt(src));
            if (dest.getIndex() == 18) {
                JOptionPane.showMessageDialog(null, "ChessBoard " + PLAYER_NAMES[player] + " win the game",
                        "Game Finished", JOptionPane.INFORMATION_MESSAGE);

            }
            }
    }

    public ChessBoardLocation nextLocation(ChessBoardLocation location) {
        // FIXME: This move the chess to next jump location instead of nearby next location
        return new ChessBoardLocation(location.getColor(), location.getIndex() + 1);
    }

    @Override
    public void registerListener(ChessBoardListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void unregisterListener(ChessBoardListener listener) {
        listenerList.remove(listener);
    }
}
