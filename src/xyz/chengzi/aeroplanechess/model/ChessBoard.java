package xyz.chengzi.aeroplanechess.model;

import xyz.chengzi.aeroplanechess.listener.ChessBoardListener;
import xyz.chengzi.aeroplanechess.listener.Listenable;
import xyz.chengzi.aeroplanechess.util.RandomUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ChessBoard implements Listenable<ChessBoardListener> {
    //    this is the number of the players
    public int number_Players;
    //  number of the robots
    public int number_Bots;
    public boolean cleverness;
    public final int INITIAL_PLANES = 4;
    private final List<ChessBoardListener> listenerList = new ArrayList<>();
    public final Square[][] grid;
    private final int dimension, endDimension;
    int[] movingList = {0, 10, 7, 4, 1, 11, 8, 5, 2, 12, 9, 6, 3};
    private static final String[] PLAYER_NAMES = {"Yellow", "Blue", "Green", "Red"};
    public int[] landed_Planes = {0, 0, 0, 0};
    public int[] onTheBoardPlanes = {0, 0, 0, 0};
    private final int[] shortCutIndex = {4, 7};
    private boolean game_Over = false;

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
        //  FIXME: Demo implementation: initial for four planes at the start position
        for (int player = 0; player < number_Players; player++) {
            for (int initial_Index = dimension + endDimension; initial_Index < dimension + endDimension + INITIAL_PLANES; initial_Index++) {
                grid[player][initial_Index].setPiece(new ChessPiece(player, 0, 0));
                grid[player][initial_Index].number_Of_Planes = 1;
                ChessPiece piece = new ChessPiece(player, 0, 0);
                ChessBoardLocation location = grid[player][initial_Index].getLocation();
                this.setChessPieceAt(location, piece);
            }
        }
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
        ChessPiece tempPiece;
        ChessBoardLocation dest = src;
        // FIXME: This just naively move the chess forward without checking anything
        int lColor, lIndex, player = getChessPieceAt(src).getPlayer();
        boolean readyToLand = false, landedOnce = false;
        if (landedPlanes(player) == 4) {
            JOptionPane.showMessageDialog(null, "There are enough airplanes on the board or hangar",
                    "Too many", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (steps == 0 || getGridAt(src).getPiece().finished() == 1) {
            return;
        }
        if (game_Over) {
            JOptionPane.showMessageDialog(null, "Please Don't press since the game is over",
                    "Game over", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Hangar landing part
        if (src.getIndex() > 18) {
            if (onTheBoardPlanes[player] + landedPlanes(player) < 4) {
                // Start position
                ChessBoardLocation startPoint = grid[player][0].getLocation();
                if (getGridAt(startPoint).getPiece() != null) {
                    // Someone is there
                    int playerToHangar = getGridAt(startPoint).getPiece().getPlayer();
                    if (playerToHangar != player) {
                        // Send the other player's planes to its hangar
                        int numPlanesToHangar = getGridAt(startPoint).number_Of_Planes;

                        getGridAt(src).number_Of_Planes = 0;

                        sendPlanesToHangar(numPlanesToHangar, playerToHangar, 0);
                        System.out.println("ChessBoard " + PLAYER_NAMES[player] + " sent " +
                                PLAYER_NAMES[playerToHangar] + " back to hangar");
                        onTheBoardPlanes[playerToHangar] -= numPlanesToHangar;
                        removeChessPieceAt(startPoint);
                    } else {
                        getGridAt(startPoint).number_Of_Planes += 1;
                        tempPiece = removeChessPieceAt(src);
                        tempPiece.moved = 1;
                        setChessPieceAt(startPoint, tempPiece);
                        onTheBoardPlanes[player]++;
                        return;
                    }
                }
                onTheBoardPlanes[player]++;
                tempPiece = removeChessPieceAt(src);
                tempPiece.moved = 1;
                setChessPieceAt(startPoint, tempPiece);
                getGridAt(startPoint).number_Of_Planes = 1;
            }
        } else {
            // Moving on the board
            System.out.println("ChessBoard " + PLAYER_NAMES[player] + " got " + steps + " steps");
            int startPlanes = getGridAt(dest).number_Of_Planes;
            getGridAt(dest).number_Of_Planes = 0;

            // Steps moving
            for (int i = 1; i <= steps; i++) {
                // Ready to land
                if (dest.getIndex() >= 12 && player == dest.getColor()) {
                    lIndex = landedOnce ? dest.getIndex() - 1 : dest.getIndex() + 1;

                    dest = new ChessBoardLocation(player, lIndex);

                    landedOnce = dest.getIndex() == 18;
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

            // Jumping moving
            if (!readyToLand && player == dest.getColor() && dest.getIndex() < 12) {
                dest = (dest.getIndex() == shortCutIndex[0] && player == dest.getColor()) ?
                        new ChessBoardLocation(dest.getColor(), shortCutIndex[1]) : nextLocation(dest);
            }
            // Shortcut moving
            if (dest.getIndex() == shortCutIndex[0] && player == dest.getColor()) {
                dest = new ChessBoardLocation(dest.getColor(), shortCutIndex[1]);
            }
            // Destination has been occupied
            int winner = player;
            if (getGridAt(dest).getPiece() != null) {
                if (getGridAt(dest).getPiece().getPlayer() != player) {
                    int numPlanesToHangar = getGridAt(dest).number_Of_Planes,
                            removed = numPlanesToHangar,
                            playerToHangar = getGridAt(dest).getPiece().getPlayer(),
                            loser = playerToHangar;
                    // Need to battle
                    if (startPlanes > 1 && numPlanesToHangar > 1) {
                        winner = battle(player, playerToHangar);
                        loser = winner == player ? playerToHangar : player;
                        numPlanesToHangar = winner == player ? numPlanesToHangar : startPlanes;
                    }

                    playerToHangar = loser;
                    getGridAt(dest).number_Of_Planes = 0;

                    System.out.println("ChessBoard " + PLAYER_NAMES[winner] + " sent " +
                            PLAYER_NAMES[loser] + " back to hangar");
                    onTheBoardPlanes[loser] -= removed;

                    sendPlanesToHangar(numPlanesToHangar, playerToHangar, 0);
                } else {
                    // Destination has teams
                    getGridAt(dest).number_Of_Planes += startPlanes;
                    tempPiece = removeChessPieceAt(src);
                    tempPiece.moved = 1;
                    setChessPieceAt(dest, tempPiece);
                    return;
                }
            }

            // Destination is free
            removeChessPieceAt(src);
            setChessPieceAt(dest, new ChessPiece(winner, 0, 1));
            getGridAt(dest).number_Of_Planes = startPlanes;

            // This plane has finished the game
            if (dest.getIndex() == 18) {
                int back_Planes = getGridAt(dest).number_Of_Planes;
                sendPlanesToHangar(back_Planes, player, 1);

                removeChessPieceAt(dest);
                for (int i = 0; i < 4; i++) {
                    landed_Planes[i] = landedPlanes(i);
                }
                onTheBoardPlanes[player] -= back_Planes;

                JOptionPane.showMessageDialog(null, "ChessBoard " + PLAYER_NAMES[player] + " Finish "
                                + getGridAt(dest).number_Of_Planes + " Planes.",
                        "Finished ", JOptionPane.INFORMATION_MESSAGE);
                int number_Finished = 0,
                        index_Loser = -1;
                for (int i = 0; i < landed_Planes.length; i++) {
                    if (landed_Planes[i] == 4) {
                        number_Finished++;
                    } else {
                        if (index_Loser < 0) {
                            index_Loser = i;
                        }
                    }
                }
                game_Over = (number_Finished == number_Players - 1);
                if (game_Over) {
                    JOptionPane.showMessageDialog(null, "Game is over and Loser is " + PLAYER_NAMES[index_Loser],
                            "Game over!", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    /***
     *
     * @param Color the player's color
     * @return the number that this color has been finished
     */
    public int landedPlanes(int Color) {
        int num = 0;
        for (int i = 19; i < 23; i++) {
            ChessBoardLocation location = new ChessBoardLocation(Color, i);
            ChessPiece chessPiece = getGridAt(location).getPiece();
            if (chessPiece != null && chessPiece.finished() == 1) {
                num++;
            }
        }
        return num;
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
                setChessPieceAt(grid[playerToHangar][hangarIndex].getLocation(), new ChessPiece(playerToHangar, finished, 0));
                count++;
                if (count == numPlanesToHangar) {
                    break;
                }
            }
        }
    }

    /***
     *
     * @param location current location
     * @return destination location
     */
    public ChessBoardLocation nextLocation(ChessBoardLocation location) {
        // FIXME: This move the chess to next jump location instead of nearby next location
        return new ChessBoardLocation(location.getColor(), location.getIndex() + 1);
    }

    /***
     *
     * @param player1 player with a stacks planes comes later
     * @param player2 player with a stacks planes stays early
     * @return winner
     */
    public int battle(int player1, int player2) {

        int winner = -1, p1 = 0, p2 = 0;
        while (winner < 0) {
            p1 = RandomUtil.nextInt(1, 6);
            p2 = RandomUtil.nextInt(1, 6);
            if (p1 != p2) {
                winner = p1 > p2 ? player1 : player2;
            }
        }
        JButton[] bs = {new JButton(PLAYER_NAMES[player1] + " get " + p1), new JButton(PLAYER_NAMES[player2] + " get " + p2)};
        JOptionPane.showOptionDialog(null, "The battle result is " + PLAYER_NAMES[winner] + " wins.", "battle",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bs, bs[0]);
        return winner;
    }

    @Override
    public void registerListener(ChessBoardListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void unregisterListener(ChessBoardListener listener) {
        listenerList.remove(listener);
    }

    /***
     *
     * @param start_Point   moving from
     * @param end_Point     moving to
     * @param player_Itself player
     */
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
}
