package xyz.chengzi.aeroplanechess.model;

import xyz.chengzi.aeroplanechess.listener.ChessBoardListener;
import xyz.chengzi.aeroplanechess.listener.Listenable;

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
    public int[] landed_Planes = {0, 0, 0, 0};
    public int[] onTheBoardPlanes = {0, 0, 0, 0};
    private final int[] shortCutIndex = {4, 7};
//    public ArrayList<ChessBoardLocation> record_Round= new ArrayList<ChessBoardLocation>();

//    public void

    /**
     * @param dimension    13
     * @param endDimension 6
     */
    public ChessBoard(int dimension, int endDimension, int number_Players) {
        this.number_Players = number_Players;
        this.grid = new Square[INITIAL_PLANES][dimension + endDimension + INITIAL_PLANES];
        this.dimension = dimension;
        this.endDimension = endDimension;

        initGrid();
    }

    private void initGrid() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < dimension + endDimension + INITIAL_PLANES; j++) {
                grid[i][j] = new Square(new ChessBoardLocation(i, j));
            }
        }
    }

    public void placeInitialPieces() {
        for (int i = 0; i < INITIAL_PLANES; i++) {
            for (int j = 0; j < dimension + endDimension + INITIAL_PLANES; j++) {
                grid[i][j].setPiece(null);
            }
        }
//         FIXME: Demo implementation: initial for four planes at the start position
        for (int player = 0; player < number_Players; player++) {
            for (int initial_Index = dimension + endDimension; initial_Index < dimension + endDimension + INITIAL_PLANES; initial_Index++) {
                grid[player][initial_Index].setPiece(new ChessPiece(player, 0));
                ChessPiece piece = new ChessPiece(player, 0);
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
//        System.out.println(dest.getIndex());
        // FIXME: This just naively move the chess forward without checking anything
        int lColor, lIndex, player = getChessPieceAt(src).getPlayer();
        boolean readyToLand = false, outOfRunWay = true, landedOnce = false;

        if (steps == 0) {
            return;
        }
        // Hangar landing part
        if (src.getIndex() > 18) {
            // Only 6 can land a plane
            if (steps == 6) {
                // Start position
                dest = grid[player][0].getLocation();
                if (getGridAt(dest).getPiece() != null) {
                    if (getGridAt(dest).getPiece().getPlayer() != player) {
                        // Send the other player's plane to hangar
                        int playerToHangar = getGridAt(dest).getPiece().getPlayer();
                        int numPlanesToHangar = getGridAt(dest).number_Of_Planes;

                        getGridAt(src).number_Of_Planes = 0;

                        sendPlanesToHangar(numPlanesToHangar, playerToHangar, 0);
                        System.out.println("ChessBoard " + PLAYER_NAMES[player] + " sent " +
                                PLAYER_NAMES[playerToHangar] + " back to hangar");
                        removeChessPieceAt(dest);

                    } else {
                        getGridAt(dest).number_Of_Planes += 1;
                        setChessPieceAt(dest, removeChessPieceAt(src));
//                        record_Round.add(dest);
                        return;
                    }
                }
                onTheBoardPlanes[player]++;
                setChessPieceAt(dest, removeChessPieceAt(src));
//                record_Round.add(dest);
                getGridAt(dest).number_Of_Planes = 1;
            }
        } else {
            // Moving on the board
            System.out.println("ChessBoard " + PLAYER_NAMES[player] + " got " + steps + " steps");
            int number_Planes = getGridAt(dest).number_Of_Planes;
            getGridAt(dest).number_Of_Planes = 0;

            // Steps moving
            for (int i = 1; i <= steps; i++) {
                // Ready to land
                if (dest.getIndex() >= 12 && player == dest.getColor()) {
                    if (landedOnce) {
                        // Turn back
                        lIndex = dest.getIndex() - 1;
                    } else {
                        // Move forward to destination
                        lIndex = dest.getIndex() + 1;
                    }
                    dest = new ChessBoardLocation(player, lIndex);
                    if (dest.getIndex() == 18) {
                        landedOnce = true;
                    }
                    readyToLand = true;
                } else {
                    // Moving on the board
                    int index = 0;
                    for (int j = 0; j < movingList.length; j++) {
                        if (movingList[j] == dest.getIndex()) {
                            index = j;
                            break;
                        }
                    }
                    lColor = (dest.getColor() + 1) % 4;
                    lIndex = movingList[(index + 1) % movingList.length];
                    // if it is because it touches the same color then it should be able to
                    dest = new ChessBoardLocation(lColor, lIndex);
                }
            }
            getGridAt(dest).number_Of_Planes += 1;
            // Steps moving finished

            // Jumping moving
            if (!readyToLand && player == dest.getColor() && dest.getIndex() < 12) {
                dest = nextLocation(dest);
            }else {
                // Shortcut moving
                if (dest.getIndex() == shortCutIndex[0] && player == dest.getColor()) {
                    dest = new ChessBoardLocation(dest.getColor(), shortCutIndex[1]);
                }
            }
            if (getGridAt(dest).getPiece() != null) {
                if (getGridAt(dest).getPiece().getPlayer() != player) {
                    ChessBoardLocation opponent = getGridAt(dest).getLocation();
                    int playerToHangar = getGridAt(opponent).number_Of_Planes;
                    getGridAt(opponent).number_Of_Planes = 0;
                    System.out.println("ChessBoard " + PLAYER_NAMES[player] + " sent " +
                            PLAYER_NAMES[getGridAt(opponent).getPiece().getPlayer()] + " back to hangar");

                    int numPlanesToHangar = getGridAt(opponent).getPiece().getPlayer();
                    sendPlanesToHangar(playerToHangar, numPlanesToHangar, 0);
                    removeChessPieceAt(opponent);
                } else {
                    getGridAt(dest).number_Of_Planes += 1;
                    setChessPieceAt(dest, removeChessPieceAt(src));
                    return;
                }

            }

            setChessPieceAt(dest, removeChessPieceAt(src));
            getGridAt(dest).number_Of_Planes = number_Planes;

            // This plane has finished the game
            if (dest.getIndex() == 18) {
                int back_Planes = getGridAt(dest).number_Of_Planes;
                sendPlanesToHangar(back_Planes, player, 1);

                removeChessPieceAt(dest);
                int player_Left = 4;
                for (int landed_plane : landed_Planes) {
                    if (landed_plane == 4) {
                        player_Left--;
                    }
                }
                if (player_Left == 1) {
                    JOptionPane.showMessageDialog(null, "GAME OVER" + PLAYER_NAMES[player],
                            "Loses " + getGridAt(dest).number_Of_Planes + " Planes.", JOptionPane.INFORMATION_MESSAGE);
                }

                JOptionPane.showMessageDialog(null, "ChessBoard " + PLAYER_NAMES[player] + "Finish"
                                + getGridAt(dest).number_Of_Planes + " Planes.",
                        "Finished ", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    private void sendBackPath(ChessBoardLocation start_Point, ChessBoardLocation end_Point, int player_Itself) {
        int lColor, lIndex;
        int index = 0;
        for (int j = 0; j < movingList.length; j++) {
            if (movingList[j] == start_Point.getIndex()) {
                index = j;
                break;
            }
        }
        ChessBoardLocation next_Position;
        lColor = (start_Point.getColor()) % 4;
        lIndex = movingList[(index) % movingList.length];

        next_Position = new ChessBoardLocation(lColor, lIndex);

        while (next_Position.getColor() != end_Point.getColor() || next_Position.getIndex() != end_Point.getIndex()) {
            if (getChessPieceAt(next_Position) != null && getChessPieceAt(next_Position).getPlayer() != player_Itself) {
                int playerToHangar = getChessPieceAt(next_Position).getPlayer();
                int numPlanesToHangar = getGridAt(next_Position).number_Of_Planes;
                getGridAt(next_Position).number_Of_Planes = 0;
                sendPlanesToHangar(playerToHangar, numPlanesToHangar, 0);
            }

            lColor = (next_Position.getColor() + 1) % 4;

            for (int j = 0; j < movingList.length; j++) {
                if (movingList[j] == next_Position.getIndex()) {
                    index = j;
                    break;
                }
            }
            lIndex = movingList[(index + 1) % movingList.length];
            next_Position = new ChessBoardLocation(lColor, lIndex);
        }
    }

    /***
     *
     * @param numPlanesToHangar   The number of planes will be sent back to hangar
     * @param playerToHangar      The one whose planes will be sent back to hangar
     */
    public void sendPlanesToHangar(int numPlanesToHangar, int playerToHangar, int finished) {
        int count = 0;
        for (int hangarIndex = 19; hangarIndex < 23; hangarIndex++) {
            if (grid[playerToHangar][hangarIndex].getPiece() == null) {
                setChessPieceAt(grid[playerToHangar][hangarIndex].getLocation(), new ChessPiece(playerToHangar, finished));
                count++;
                if (count == numPlanesToHangar) {
                    break;
                }
            }
        }
    }

    public ChessBoardLocation nextLocation(ChessBoardLocation location) {
        // FIXME: This move the chess to next jump location instead of nearby next location
        return new ChessBoardLocation(location.getColor(), location.getIndex() + 1);
    }
    public void Battle(int player1,int player2, ChessBoardLocation location){
        int winner = Integer.MIN_VALUE;
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
