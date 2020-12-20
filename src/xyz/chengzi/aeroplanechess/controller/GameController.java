package xyz.chengzi.aeroplanechess.controller;

import xyz.chengzi.aeroplanechess.listener.GameStateListener;
import xyz.chengzi.aeroplanechess.listener.InputListener;
import xyz.chengzi.aeroplanechess.listener.Listenable;
import xyz.chengzi.aeroplanechess.model.*;
import xyz.chengzi.aeroplanechess.util.RandomUtil;
import xyz.chengzi.aeroplanechess.view.ChessBoardComponent;
import xyz.chengzi.aeroplanechess.view.ChessComponent;
import xyz.chengzi.aeroplanechess.view.SquareComponent;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GameController implements InputListener, Listenable<GameStateListener> {
    private final List<GameStateListener> listenerList = new ArrayList<>();
    private final ChessBoardComponent view;
    private final ChessBoard model;


    private Integer rolledNumber0; // Record the last rolling outcome
    private Integer rolledNumber1;
    private Integer rolledNumber;
    private int currentPlayer;
    private boolean hasBeenMoved = false;

    public GameController(ChessBoardComponent chessBoardComponent, ChessBoard chessBoard) {
        this.view = chessBoardComponent;
        this.model = chessBoard;

        view.registerListener(this);
        model.registerListener(view);
    }

    public ChessBoardComponent getView() {
        return view;
    }

    public ChessBoard getModel() {
        return model;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void initializeGame() {
        model.placeInitialPieces();
        rolledNumber0 = null;
        currentPlayer = 0;
        listenerList.forEach(listener -> listener.onPlayerStartRound(currentPlayer));
    }

    public int rollDice() {
        if (rolledNumber0 == null) {
            rolledNumber1 = RandomUtil.nextInt(1, 6);
            return rolledNumber0 = RandomUtil.nextInt(1, 6);
        } else {
            return -1;
        }
    }

    public int getDice0() {
        return rolledNumber0;
    }

    public int getDice1() {
        return rolledNumber1;
    }

    public void nextPlayer() {
        rolledNumber = null;
        rolledNumber0 = null;
        rolledNumber1 = null;
        currentPlayer = (currentPlayer + 1) % this.model.number_Players;
        round = 1;

        // Empty the movement for current player
        for (int player = 0; player < model.number_Players; player++) {
            for (int index = 0; index < 23; index++) {
                ChessPiece piece = model.grid[player][index].getPiece();
                if (piece != null && piece.moved == 1) {
                    piece.moved = 0;
                }
            }
        }
    }

    @Override
    public void onPlayerClickSquare(ChessBoardLocation location, SquareComponent component) {
        System.out.println("clicked " + location.getColor() + "," + location.getIndex());
    }

    private int round = 1;
    private ChessBoardLocation roundLocation = null;

    @Override
    public void onPlayerClickChessPiece(ChessBoardLocation location, ChessComponent component) {
        if (rolledNumber0 == null) {
            rolledNumber0 = 1;
        }
        if (rolledNumber1 == null) {
            rolledNumber1 = 1;
        }
        boolean hasASix = rolledNumber0 == 6 || rolledNumber1 == 6;
        boolean selfHangar = location.getColor() == currentPlayer;

        // Record the original location when dice >= 10 for 3 times
        if (roundLocation == null) roundLocation = location;

        if (this.model.landed_Planes[currentPlayer] == 4) {
            nextPlayer();
        }
        if (rolledNumber != null) {
//          if the sum if more than 10
            ChessPiece piece = model.getChessPieceAt(location);
            if (piece.getPlayer() == currentPlayer) {
                // Manually choose the dice number
                if (rolledNumber > 100) {
                    rolledNumber /= 100;
                    if (location.getIndex() > 18 && selfHangar && !hasASix) {
                        System.out.println("Manually Need a 6 to land");
                        hasBeenMoved = model.onTheBoardPlanes[currentPlayer] == 0 && !hasASix;

                        if (hasBeenMoved) {
                            nextPlayer();
                        }
                    } else {
                        if (rolledNumber >= 10) {
                            roundTest(location);
                        } else {
                            if (location.getIndex() > 18) {
                                model.moveChessPiece(location, 6);
                            } else {
                                model.moveChessPiece(location, rolledNumber);
                            }
                            nextPlayer();
                        }
                    }
                    if (!hasBeenMoved)
                        JOptionPane.showMessageDialog(null, "Please move a plane");
                } else {
                    // Random dices
                    if (location.getIndex() > 18 && selfHangar && !hasASix) {
                        System.out.println("Need a 6 to land");
                        hasBeenMoved = model.onTheBoardPlanes[currentPlayer] == 0 && !hasASix;
                        if (hasBeenMoved) {
                            nextPlayer();
                        }
                    } else {
                        // Ready to count for 3 times
                        if (rolledNumber >= 10) {
                            roundTest(location);
                        } else {
                            if (location.getIndex() > 18 && selfHangar && hasASix && rolledNumber != 0) {
                                model.moveChessPiece(location, 6);
                            } else {
                                model.moveChessPiece(location, rolledNumber);
                            }
                            nextPlayer();
                        }
                    }
                    if (!hasBeenMoved)
                        JOptionPane.showMessageDialog(null, "Please move a plane");
                }

                listenerList.forEach(listener -> listener.onPlayerEndRound(currentPlayer));
                listenerList.forEach(listener -> listener.onPlayerStartRound(currentPlayer));
            } else {
                System.out.println("GameController It is not your turn !");
            }
        } else {
            // Need to roll the dice
            System.out.println("GameController There is " + this.model.getGridAt(location).number_Of_Planes + " Planes "
                    + this.model.getGridAt(location).getPiece().moved);
        }

    }

    public void changeRolledNumber(int RolledNumber, int temp) {
        if (temp == 0) {
            rolledNumber = RolledNumber;
        } else {
            rolledNumber0 = RolledNumber;
            rolledNumber1 = temp;
            rolledNumber = (rolledNumber0 + rolledNumber1) * 100;
        }
    }

    /***
     * If roundTest fail then send some planes to hangar
     * @param location Click location
     */
    public void roundTest(ChessBoardLocation location) {
        if (round == 3) {
            eraseMovement();
            nextPlayer();
        } else {
            for (int i = 0; i <getModel().number_Players; i++) {
                nextPlayer();
            }

//            if (location.getIndex() > 18) {
//                model.moveChessPiece(location, 6);
//            } else {
//                model.moveChessPiece(location, rolledNumber);
//            }
            hasBeenMoved = true;
            round++;
        }
        rolledNumber = null;
    }

    /**
     * Send those planes who has moved in last 3 round
     */
    public void eraseMovement() {
        for (int player = 0; player < 4; player++) {
            for (int index = 0; index < 23; index++) {
                ChessPiece piece = model.getGridAt(new ChessBoardLocation(player, index)).getPiece();
                if (piece != null && piece.moved == 1) {
                    int count = 0;
                    for (int hangarIndex = 19; hangarIndex < 23; hangarIndex++) {
                        if (model.grid[piece.getPlayer()][hangarIndex].getPiece() == null) {
                            model.setChessPieceAt(model.grid[piece.getPlayer()][hangarIndex].getLocation(),
                                    new ChessPiece(piece.getPlayer(), 0, 0));
                            model.removeChessPieceAt(new ChessBoardLocation(player, index));
                            count++;
                            if (count == model.grid[player][index].number_Of_Planes) {
                                break;
                            }
                        }
                    }

                }
            }
        }
    }

    @Override
    public void registerListener(GameStateListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void unregisterListener(GameStateListener listener) {
        listenerList.remove(listener);
    }
}