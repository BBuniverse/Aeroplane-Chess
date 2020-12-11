package xyz.chengzi.aeroplanechess.model;

import xyz.chengzi.aeroplanechess.listener.ChessBoardListener;
import xyz.chengzi.aeroplanechess.listener.Listenable;
import xyz.chengzi.aeroplanechess.controller.GameController;
import xyz.chengzi.aeroplanechess.view.SquareComponent;

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
//  this is sychonized with the grid and full of stacks
    public final Stack[][] stacks;
    private final int dimension, endDimension;
    int[] movingList = {0, 10, 7, 4, 1, 11, 8, 5, 2, 12, 9, 6, 3};
    private static final String[] PLAYER_NAMES = {"Yellow", "Blue", "Green", "Red"};
    public int[] landed_Planes = {0,0,0,0};
    public int[] onTheBoardPlanes = {0,0,0,0};
    private int[] shortCutIndex = {4,7};

//    public void

    /**
     * @param dimension    13
     * @param endDimension 6
     */
    public ChessBoard(int dimension, int endDimension,int number_Players) {
        this.number_Players = number_Players;
        this.grid = new Square[INITIAL_PLANES][dimension + endDimension + INITIAL_PLANES];
        this.stacks = new Stack[INITIAL_PLANES][dimension + endDimension];
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
//        System.out.println(dest.getIndex());
        // FIXME: This just naively move the chess forward without checking anything
        int lColor, lIndex, player = getChessPieceAt(src).getPlayer();
        boolean readyToLand = false, landed = false;

        if(steps == 0){
            return;
        }
        if (src.getIndex() > 18 ){
            if(steps == 6){
                dest = grid[player][0].getLocation();
                if(getGridAt(dest).getPiece()!= null ){
                    if(getGridAt(dest).getPiece().getPlayer() != player){
                        int opponent_Player = getGridAt(dest).getPiece().getPlayer();
//                        int oppo = stacks[dest.getColor()][dest.getIndex()].planeQuantity;
                        int oppo = getGridAt(dest).number_Of_Planes;

//                        stacks[dest.getColor()][dest.getIndex()]=null;
                        getGridAt(dest).number_Of_Planes=0;

                        int index_Local = 0;
                        for (int opppnent_Index = 19; opppnent_Index <23 ; opppnent_Index++) {
                            if(grid[opponent_Player][opppnent_Index].getPiece() == null){
                                setChessPieceAt(grid[opponent_Player][opppnent_Index].getLocation(),new ChessPiece(opponent_Player));
                                index_Local ++;
                                if(index_Local == oppo){
                                    break;
                                }
                            }
                        }
                        System.out.println("ChessBoard " + PLAYER_NAMES[player] + " sent " +
                                PLAYER_NAMES[opponent_Player] + " back to hangar");
                        removeChessPieceAt(dest);
                    }
                    else{
//                        stacks[dest.getColor()][dest.getIndex()].planeQuantity += 1;
                        getGridAt(dest).number_Of_Planes +=1;
                        setChessPieceAt(dest, removeChessPieceAt(src));
                        return;
                    }
                }
                onTheBoardPlanes[player]++;
                setChessPieceAt(dest, removeChessPieceAt(src));
//                stacks[dest.getColor()][dest.getIndex()] =  new Stack(new ChessPiece(player),dest,1);
                getGridAt(dest).number_Of_Planes =1;
            }
        }else{
            System.out.println("ChessBoard " + PLAYER_NAMES[player] + " got " + steps + " steps");
//            int number_Planes = stacks [dest.getColor()][dest.getIndex()].planeQuantity;
            int number_Planes = getGridAt(dest).number_Of_Planes;
//            stacks [dest.getColor()][dest.getIndex()] = null;
            getGridAt(dest).number_Of_Planes = 0;
            for (int i = 1; i <= steps; i++) {
                // Ready to land
                if (dest.getIndex() >= 12 && player == dest.getColor()) {
//                    stacks[dest.getColor()][dest.getIndex()] = null;
                    getGridAt(dest).number_Of_Planes = 0;
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
//                  if it is because it touches the same color then it should be able to
                    dest = new ChessBoardLocation(lColor, lIndex);
                }
            }
//            stacks[dest.getColor()][dest.getIndex()] = new Stack(new ChessPiece(player),dest,number_Planes);
            if (!readyToLand && player == dest.getColor() && dest.getIndex()<12 ) {
                    ChessBoardLocation start = dest;
                    dest = nextLocation(dest);
                    ChessBoardLocation end = dest;
                    sendBackPath(start,end,player);
            }
            if(dest.getIndex() == shortCutIndex[0] && player == dest.getColor()){
                ChessBoardLocation start = dest;
                dest= new ChessBoardLocation(dest.getColor(),shortCutIndex[1]);
                ChessBoardLocation end   = dest;
                sendBackPath(start,end,player);
            }

            if (getGridAt(dest).getPiece() != null ) {
                if( getGridAt(dest).getPiece().getPlayer() != player){
                    ChessBoardLocation opponent = getGridAt(dest).getLocation();
//                    int quantity_Oppo = stacks[opponent.getColor()][opponent.getIndex()].planeQuantity;
                    int quantity_Oppo = getGridAt(opponent).number_Of_Planes;
//                    stacks[opponent.getColor()][opponent.getIndex()] = null;
                    getGridAt(opponent).number_Of_Planes = 0;
                    System.out.println("ChessBoard " + PLAYER_NAMES[player] + " sent " +
                            PLAYER_NAMES[getGridAt(opponent).getPiece().getPlayer()] + " back to hangar");

                    int index_Oppo = 0;
                    int opponent_Player = getGridAt(opponent).getPiece().getPlayer();
                    for (int oppend_Hangars = 19; oppend_Hangars < 23 ; oppend_Hangars++) {
                        if(grid[opponent_Player][oppend_Hangars].getPiece() == null){
                            setChessPieceAt(grid[opponent_Player][oppend_Hangars].getLocation(),new ChessPiece(opponent_Player));
                            index_Oppo ++;
                            if(index_Oppo == quantity_Oppo){
                                break;
                            }
                        }
                    }
                    removeChessPieceAt(opponent);
                }else{
//                    stacks[dest.getColor()][dest.getIndex()].planeQuantity +=1;
                    getGridAt(dest).number_Of_Planes+=1;
                    setChessPieceAt(dest, removeChessPieceAt(src));
                    return;
                }

            }

            setChessPieceAt(dest, removeChessPieceAt(src));
//            stacks[dest.getColor()][dest.getIndex()] = new Stack(new ChessPiece(player),dest,number_Planes);
            getGridAt(dest).number_Of_Planes=number_Planes;

            if (dest.getIndex() == 18) {
                int back_Planes = getGridAt(dest).number_Of_Planes;
                System.out.println(back_Planes);
                int index_Back_Plane = 0;
                for (int index_Back = 19; index_Back < 23 ; index_Back++) {
                    if(grid[player][index_Back].getPiece() == null){
                        setChessPieceAt(grid[player][index_Back].getLocation(),new ChessPiece(player));
                        index_Back_Plane ++;
                        landed_Planes[player] ++;
                        if(index_Back_Plane == back_Planes){
                            break;
                        }
                    }
                }
                removeChessPieceAt(dest);
                int player_Left = 4;
                for (int i = 0; i <landed_Planes.length ; i++) {
                    if(landed_Planes[i] == 4){
                        player_Left--;
                    }
                }
                if(player_Left == 1){
                    JOptionPane.showMessageDialog(null, "GAME OVER" + PLAYER_NAMES[player] ,
                            "Loses "+ getGridAt(dest).number_Of_Planes + " Planes.", JOptionPane.INFORMATION_MESSAGE);
                }
                JOptionPane.showMessageDialog(null, "ChessBoard " + PLAYER_NAMES[player] + "Finish"
                                + getGridAt(dest).number_Of_Planes + " Planes.",
                        "Finished ", JOptionPane.INFORMATION_MESSAGE);
            }
            }
    }
//    private void
    private void sendBackPath(ChessBoardLocation start_Point, ChessBoardLocation end_Point,int player_Itself){
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

        next_Position = new ChessBoardLocation(lColor,lIndex);

        while(next_Position.getColor() != end_Point.getColor() || next_Position.getIndex() != end_Point.getIndex()){
//            System.out.println("271");
            if( getChessPieceAt(next_Position)!=null && getChessPieceAt(next_Position).getPlayer() != player_Itself){
                int oppo_Player = getChessPieceAt(next_Position).getPlayer();
//                int numbers = stacks[next_Position.getColor()][next_Position.getIndex()].planeQuantity;
                int numbers = getGridAt(next_Position).number_Of_Planes;
//                stacks[next_Position.getColor()][next_Position.getIndex()] =  null;
                getGridAt(next_Position).number_Of_Planes = 0;
                int index_Planes =0;
                for (int index_Oppo = 19; index_Oppo < 23; index_Oppo++) {
                    if(grid[oppo_Player][index_Oppo].getPiece()==null){
                        removeChessPieceAt(next_Position);
                        setChessPieceAt(grid[oppo_Player][index_Oppo].getLocation(),new ChessPiece(oppo_Player));
                        index_Planes++;
                        if(index_Planes==numbers){
                            break;
                        }
                    }
                }
            }

            lColor = (next_Position.getColor() + 1) % 4;

            for (int j = 0; j < movingList.length; j++) {
                if (movingList[j] == next_Position.getIndex()) {
                    index = j;
                    break;
                }
            }
            lIndex = movingList[(index + 1) % movingList.length];
            next_Position = new ChessBoardLocation(lColor,lIndex);
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
