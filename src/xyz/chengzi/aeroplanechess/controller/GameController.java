package xyz.chengzi.aeroplanechess.controller;

import xyz.chengzi.aeroplanechess.listener.GameStateListener;
import xyz.chengzi.aeroplanechess.listener.InputListener;
import xyz.chengzi.aeroplanechess.listener.Listenable;
import xyz.chengzi.aeroplanechess.model.ChessBoard;
import xyz.chengzi.aeroplanechess.model.ChessBoardLocation;
import xyz.chengzi.aeroplanechess.model.ChessPiece;
import xyz.chengzi.aeroplanechess.util.RandomUtil;
import xyz.chengzi.aeroplanechess.view.ChessBoardComponent;
import xyz.chengzi.aeroplanechess.view.ChessComponent;
import xyz.chengzi.aeroplanechess.view.SquareComponent;

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

    public int[] getDices() {
        return new int[]{rolledNumber0, rolledNumber1};
    }

    public void nextPlayer() {
        rolledNumber0 = null;
        currentPlayer = (currentPlayer + 1) % this.model.number_Players;
    }


    @Override
    public void onPlayerClickSquare(ChessBoardLocation location, SquareComponent component) {
        System.out.println("clicked " + location.getColor() + "," + location.getIndex());
    }
    private  int round = 1;
    @Override
    public void onPlayerClickChessPiece(ChessBoardLocation location, ChessComponent component) {
        if (this.model.landed_Planes[currentPlayer] == 4) {
            nextPlayer();
        }
        if (rolledNumber != null) {
//          if the sum if more than 10
            ChessPiece piece = model.getChessPieceAt(location);
            if (piece.getPlayer() == currentPlayer) {
                // Manually choose the dice number
                if (rolledNumber > 50) {
                    rolledNumber /= 100;
                    if (location.getIndex() > 18 && location.getColor() == currentPlayer && rolledNumber != 6) {
                        System.out.println("Need a 6 to land");
                    } else {
                        model.moveChessPiece(location, rolledNumber);
                    }
                } else {
                    if (location.getIndex() > 18 && location.getColor() == currentPlayer && !(rolledNumber0 == 6 || rolledNumber1 == 6)) {
                        System.out.println("Need a 6 to land");
                    } else if (location.getIndex() > 18 && location.getColor() == currentPlayer && (rolledNumber0 == 6 || rolledNumber1 == 6)) {
                        if (this.model.landed_Planes[currentPlayer] + this.model.onTheBoardPlanes[currentPlayer] == 4) {
                            System.out.println("Sorry, you can only have 4 planes on the board and hangars");
                        } else {
                            model.moveChessPiece(location, 6);
                        }
                    } else {
                        model.moveChessPiece(location, rolledNumber);
                    }
                }
                rolledNumber = null;

//              if the sum of rolledNumbers is more than 10
                listenerList.forEach(listener -> listener.onPlayerEndRound(currentPlayer));

                if(rolledNumber1 + rolledNumber0 > 10){

                    //if the round is 3 and then next player then send back former 2
                    if(round == 3){

                        nextPlayer();
                    }
                    round ++;
                }

                listenerList.forEach(listener -> listener.onPlayerStartRound(currentPlayer));


            } else {
                System.out.println("It is not your turn !");
            }
        } else {
            System.out.println("GameController There is " + this.model.getGridAt(location).number_Of_Planes + " Planes");
        }
    }

    public void changeRolledNumber(int RolledNumber) {
        rolledNumber = RolledNumber;
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