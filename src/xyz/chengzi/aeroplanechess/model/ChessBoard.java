package xyz.chengzi.aeroplanechess.model;

import xyz.chengzi.aeroplanechess.listener.ChessBoardListener;
import xyz.chengzi.aeroplanechess.listener.Listenable;

import java.util.ArrayList;
import java.util.List;

public class ChessBoard implements Listenable<ChessBoardListener> {
    private final List<ChessBoardListener> listenerList = new ArrayList<>();
    private final Square[][] grid;
    private final int dimension, endDimension;
    int[] movingList = {0, 10, 7, 4, 1, 11, 8, 5, 2, 12, 9, 6, 3};


    /**
     * @param dimension    13
     * @param endDimension 6
     */
    public ChessBoard(int dimension, int endDimension) {
        this.grid = new Square[4][dimension + endDimension];
        this.dimension = dimension;
        this.endDimension = endDimension;

        initGrid();
    }

    private void initGrid() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < dimension + endDimension; j++) {
                grid[i][j] = new Square(new ChessBoardLocation(i, j));
            }
        }
    }

    public void placeInitialPieces() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < dimension + endDimension; j++) {
                grid[i][j].setPiece(null);
            }
        }
        // FIXME: Demo implementation: initial for four planes at the start position
        grid[0][12].setPiece(new ChessPiece(0));
        grid[1][12].setPiece(new ChessPiece(1));
        grid[2][12].setPiece(new ChessPiece(2));
        grid[3][12].setPiece(new ChessPiece(3));
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
        System.out.println("ChessBoard " + player + " got " + steps + " steps");

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
                dest = new ChessBoardLocation(lColor, lIndex);
            }
            if (winning(steps - i, dest.getIndex())) {
                System.out.println("ChessBoard " + player + " win the game");
            }
        }
        if (!readyToLand && player == dest.getColor()) {
            dest = nextLocation(dest);
        }
        setChessPieceAt(dest, removeChessPieceAt(src));
    }

    public ChessBoardLocation nextLocation(ChessBoardLocation location) {
        // FIXME: This move the chess to next jump location instead of nearby next location
        return new ChessBoardLocation(location.getColor(), location.getIndex() + 1);
    }

    /**
     * @param leftSteps how many step left
     * @param index     18 means winning
     * @return win or not
     */
    public boolean winning(int leftSteps, int index) {
        return leftSteps == 0 && index == 18;
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
