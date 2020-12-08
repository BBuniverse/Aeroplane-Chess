package xyz.chengzi.aeroplanechess;

import xyz.chengzi.aeroplanechess.controller.GameController;
import xyz.chengzi.aeroplanechess.model.ChessBoard;
import xyz.chengzi.aeroplanechess.view.ChessBoardComponent;
import xyz.chengzi.aeroplanechess.view.GameFrame;



import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class AeroplaneChess {
    public static void main(String[] args) {
        JFrame windows_Choose = new JFrame("2020 CS102A Project");
        windows_Choose.setSize(772,825);
        windows_Choose.setLocationRelativeTo(null);

        final int [] number_Of_Player= {0}; // default
        TextField number_Input = new TextField("Please Input the number of players you want");
        JButton read_Input = new JButton("Check");
        read_Input.setSize(50,50);
        number_Input.setLocation(300,400);

        number_Input.setSize(200,50);
        number_Input.setLocation(300,300);
        read_Input.addActionListener(e -> {
            try{
                number_Of_Player [0] = Integer.parseInt( number_Input.getText());
            }
            catch (Exception exception ){
                number_Input.setText("");
            }
            if(number_Of_Player[0] >4 || number_Of_Player[0]< 2){
                System.out.println("Please input an Integer between 2-4");
            }else{
                windows_Choose.dispatchEvent(new WindowEvent(windows_Choose,WindowEvent.WINDOW_CLOSING) );
                System.setProperty("sun.java2d.win.uiScaleX", "96dpi");
                System.setProperty("sun.java2d.win.uiScaleY", "96dpi");
                SwingUtilities.invokeLater(() -> {
                    ChessBoardComponent chessBoardComponent = new ChessBoardComponent(760, 13, 6,number_Of_Player[0]);
                    ChessBoard chessBoard = new ChessBoard(13, 6,number_Of_Player[0]);
                    GameController controller = new GameController(chessBoardComponent, chessBoard);

                    GameFrame mainFrame = new GameFrame(controller);
                    mainFrame.add(chessBoardComponent);
                    mainFrame.setVisible(true);
                    controller.initializeGame();
                    windows_Choose.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                });
            }
        });
        windows_Choose.add(number_Input);
        windows_Choose.add(read_Input);
        windows_Choose.setVisible(true);


    }


}
