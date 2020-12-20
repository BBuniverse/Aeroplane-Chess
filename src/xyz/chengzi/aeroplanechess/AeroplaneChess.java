package xyz.chengzi.aeroplanechess;

import org.w3c.dom.Text;
import xyz.chengzi.aeroplanechess.controller.GameController;
import xyz.chengzi.aeroplanechess.model.ChessBoard;
import xyz.chengzi.aeroplanechess.view.ChessBoardComponent;
import xyz.chengzi.aeroplanechess.view.GameFrame;


import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class AeroplaneChess {
    public static void main(String[] args) {
        JFrame windows_Choose = new JFrame("2020 CS102A Project");
        windows_Choose.setSize(772, 825);
        windows_Choose.setLocationRelativeTo(null);

        final int[] number_Of_Player = {0}; // default
        TextField number_Input = new TextField("Please Input the number of players you want");
        JButton read_Input = new JButton("Check");
        read_Input.setSize(50, 50);
        read_Input.setLocation(300, 400);

        number_Input.setSize(200, 50);
        number_Input.setLocation(300, 300);


        read_Input.addActionListener(e -> {
            try {
                number_Of_Player[0] = Integer.parseInt(number_Input.getText());
            } catch (Exception exception) {
                number_Input.setText("");
            }
            if (number_Of_Player[0] > 4 || number_Of_Player[0] < 2) {
                System.out.println("AeroplaneChess Please input an Integer between 2-4");
            } else {
                int[] response ={-1};
                boolean[] clever= {false};
                if(choose_AI_Interface()==1) {
                        clever[0] = (choose_Cleverness()==1);
                        Object[] bs = new String[number_Of_Player[0] - 1];
                        for (int i = 0; i < bs.length; i++) {
                            bs[i] = (i + 1) + "";
                        }
                        response[0]= JOptionPane.showOptionDialog(null, "Number of AI choosed", "Number_AI",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bs, bs[0]);
                        windows_Choose.dispatchEvent(new WindowEvent(windows_Choose, WindowEvent.WINDOW_CLOSING));
                }
                System.setProperty("sun.java2d.win.uiScaleX", "96dpi");
                System.setProperty("sun.java2d.win.uiScaleY", "96dpi");
                SwingUtilities.invokeLater(() -> {


                    ChessBoardComponent chessBoardComponent = new ChessBoardComponent(760, 13, 6, number_Of_Player[0]);
                    ChessBoard chessBoard = new ChessBoard(13, 6, number_Of_Player[0]);
                    if(response[0]>=0) {
                        chessBoard.number_Bots = response[0] + 1;
                        chessBoard.cleverness = clever[0];
                    }
                    play_Music();
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

    public static int choose_AI_Interface(){
        Object[] decisions= {"NO","YES"};
        return JOptionPane.showOptionDialog(null, "Do you wanna have bot?",
                "Against AI", JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,
                        null, decisions,decisions[0]);
    }
    public static int choose_Cleverness()
    {
            Object[] cleverness= {"Stupid","Clever"};
            return JOptionPane.showOptionDialog(null, "Clever or Stupid",
                    "AI's cleverness", JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,
                    null, cleverness,cleverness[0]);
    }
    public static void play_Music(){

        try {
            URL cb;
            File f = new File(""); // 引号里面的是音乐文件所在的路径
            cb = f.toURL();
            AudioClip aau;
            aau = Applet.newAudioClip(cb);

            aau.play();
            aau.loop();
        } catch (MalformedURLException e) {

            e.printStackTrace();

        }
    }
}
