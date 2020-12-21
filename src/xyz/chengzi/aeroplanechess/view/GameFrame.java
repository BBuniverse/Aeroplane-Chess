package xyz.chengzi.aeroplanechess.view;

import xyz.chengzi.aeroplanechess.controller.GameController;
import xyz.chengzi.aeroplanechess.listener.GameStateListener;
import xyz.chengzi.aeroplanechess.model.ChessBoard;
import xyz.chengzi.aeroplanechess.model.ChessBoardLocation;
import xyz.chengzi.aeroplanechess.model.ChessPiece;
import xyz.chengzi.aeroplanechess.model.Square;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observer;

public class GameFrame extends JFrame implements GameStateListener {
    private static final String[] PLAYER_NAMES = {"Yellow", "Blue", "Green", "Red"};

    private final JLabel statusLabel = new JLabel();

    public GameFrame(GameController controller) {

        controller.registerListener(this);

        setTitle("2020 CS102A Project");
        setSize(772, 825);
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(null);


        statusLabel.setBounds(0, 758, 400, 20);
        statusLabel.setFont(statusLabel.getFont().deriveFont(18.0f));
        add(statusLabel);

        DiceSelectorComponent diceSelectorComponent = new DiceSelectorComponent();
        diceSelectorComponent.setLocation(396, 758);
        add(diceSelectorComponent);

        JButton button = new JButton("roll");
        button.addActionListener((e) -> {
            if (diceSelectorComponent.isRandomDice()) {
                int dice = controller.rollDice();
                int dice1 = controller.getDice1();

                if (dice != -1) {
                    statusLabel.setText(String.format("[%s] Rolled a %c (%d) and %c (%d)",
                            PLAYER_NAMES[controller.getCurrentPlayer()], '\u267F' + dice, dice, '\u267F' + dice1, dice1));
                } else {
                    JOptionPane.showMessageDialog(this, "You have already rolled the dices");
                }

                String addition = Math.max(dice, dice1) + " + " + Math.min(dice, dice1) + " = " + (dice + dice1);
                String subtraction = Math.max(dice, dice1) + " - " + Math.min(dice, dice1) + " = " + (Math.abs(dice - dice1));
                String multiple = Math.max(dice, dice1) + " * " + Math.min(dice, dice1) + " = ";
                String divide = Math.max(dice, dice1) + " / " + Math.min(dice, dice1) + " = ";

                subtraction = Math.abs(dice - dice1) != 0 ? subtraction : "Invalid";
                multiple = dice * dice1 <= 12 ? multiple + (dice * dice1) : "Invalid";

                int div = Math.max(dice, dice1) / Math.min(dice, dice1);
                divide = ((double) Math.max(dice, dice1) / Math.min(dice, dice1)) % 1 == 0 ?
                        divide + div : "Invalid";

                String[] options = {addition, subtraction, multiple, divide};
                int[] steps = {
                        dice + dice1,
                        Math.abs(dice - dice1) != 0 ? Math.abs(dice - dice1) : 0,
                        dice * dice1 <= 12 ? dice * dice1 : 0,
                        ((double) Math.max(dice, dice1) / Math.min(dice, dice1)) % 1 == 0 ? div : 0
                };
//                if the player is the bot
                ChessBoard chessBoard = controller.getModel();
                if (chessBoard.number_Bots > 0 &&
                        (controller.getCurrentPlayer() >= (chessBoard.number_Players - chessBoard.number_Bots))) {
                    if (chessBoard.cleverness) {
                        make_The_Smarter_Bot_Move(steps, controller);
                    } else {
                        make_The_Simple_Bot_Move(steps, controller);
                    }

                } else {
                    int index = -1;
                    index = JOptionPane.showOptionDialog(null, "Returns the option of your choice",
                            PLAYER_NAMES[controller.getCurrentPlayer()] + " Click a button",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                    if (index >= 0) {
                        controller.changeRolledNumber(steps[index], 0);
                    }
                }
            } else {
                // Manually choose steps
                String dices = diceSelectorComponent.getSelectedDice().toString();
                controller.changeRolledNumber(Integer.parseInt(String.valueOf(dices.charAt(0))), Integer.parseInt(String.valueOf(dices.charAt(2))));
                JOptionPane.showMessageDialog(this, "You selected " + diceSelectorComponent.getSelectedDice());
            }
        });
        button.setFont(button.getFont().deriveFont(18.0f));
        button.setBounds(668, 756, 90, 30);
        add(button);


        JButton option = new JButton("extension");
        option.addActionListener((e) -> {
            String[] options = {"restart", "load", "save"};
            int index = JOptionPane.showOptionDialog(null, "Please select your options",
                    "Click a button",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            switch (options[index]) {
                case "restart": {
                    Restart(controller);
                    break;
                }
                case "load": {
                    Load(controller);
                    break;
                }
                case "save": {
                    Save(controller);
                    break;
                }
            }
        });
        option.setBounds(250, 756, 140, 30);
        option.setFont(option.getFont().deriveFont(18.0f));
        add(option);
    }

    public void make_The_Simple_Bot_Move(int[] steps, GameController controller) {
        JOptionPane.showMessageDialog(this, "Robot chose " + steps[0] +
                " from (" + controller.getDice0() + " , " + controller.getDice1() + ")");
        controller.changeRolledNumber(steps[0], 0);
        ChessBoardLocation location;
        for (int player_Index = 0; player_Index < controller.getModel().INITIAL_PLANES; player_Index++) {
            for (int position_Index = 0; position_Index < 23; position_Index++) {
                ChessBoardLocation location_Local = new ChessBoardLocation(player_Index, position_Index);
                if (controller.getModel().getGridAt(location_Local).getPiece() != null) {
                    if (controller.getModel().getGridAt(location_Local).getPiece().getPlayer() == controller.getCurrentPlayer()) {
                        location = new ChessBoardLocation(player_Index, position_Index);
                        SquareComponent chessBoardComponent = controller.getView().gridComponents[player_Index][position_Index];
                        controller.onPlayerClickChessPiece(location, (ChessComponent) chessBoardComponent.getComponent(0));
                        return;
                    }
                }
            }
        }
    }

    public void make_The_Smarter_Bot_Move(int[] steps, GameController controller) {
        int steps_Choose = steps[0];

        ChessBoardLocation location;

        for (int player_Index = 0; player_Index < controller.getModel().INITIAL_PLANES; player_Index++) {
            for (int position_Index = 0; position_Index < 23; position_Index++) {
                ChessBoardLocation location_Local = new ChessBoardLocation(player_Index, position_Index);
                if (controller.getModel().getGridAt(location_Local).getPiece() != null) {

                    if (controller.getModel().getGridAt(location_Local).getPiece().getPlayer() == controller.getCurrentPlayer()) {
                        location = new ChessBoardLocation(player_Index, position_Index);
                        int difference;
                        if(position_Index >= 12 && position_Index <= 18){
                            for (int index = 0; index < steps.length; index++) {
                                difference = Math.abs(steps[index] + position_Index-18);
                                if(difference <= Math.abs(steps_Choose+position_Index - 18)){
                                    steps_Choose = steps[index];
                                }
                            }
                        }else{
                            for (int index = 0; index < steps.length; index++) {
                                if(steps[index] > steps_Choose){
                                    steps_Choose = steps[index];
                                }
                            }
                        }
                        controller.changeRolledNumber(steps_Choose, 0);
                        JOptionPane.showMessageDialog(this, "Robot chose " + steps_Choose +
                                " from (" + controller.getDice0() + " , " + controller.getDice1() + ")");

                        SquareComponent chessBoardComponent = controller.getView().gridComponents[player_Index][position_Index];
                        controller.onPlayerClickChessPiece(location, (ChessComponent) chessBoardComponent.getComponent(0));

                        return;
                    }
                }
            }
        }
    }


    /**
     * @param Controller Current board
     */
    public void Restart(GameController Controller) {
        JOptionPane.showMessageDialog(this, String.format("%s decides to restart",
                PLAYER_NAMES[Controller.getCurrentPlayer()]));
        System.out.println("GameFrame " + String.format("%s decides to restart",
                PLAYER_NAMES[Controller.getCurrentPlayer()]));

        // Empty the board
        empty(Controller);

        // Initial the board
        Controller.initializeGame();
    }

    /**
     * @param Controller Current board
     */

    public void Load(GameController Controller) {
        try {
            Controller.initializeGame();
            // Empty the board
            empty(Controller);
            String filename = JOptionPane.showInputDialog("Please input the local file name");
            File file = new File("..\\Aeroplane-Chess\\localgame\\" + filename + ".txt");

            BufferedReader in = new BufferedReader(new FileReader(file));  //
            String line;
            while ((line = in.readLine()) != null) {
                String[] input = line.split("\t");
                int player = Integer.parseInt(input[0]);
                int color = Integer.parseInt(input[1]);
                int index = Integer.parseInt(input[2]);
                int numPlane = Integer.parseInt(input[3]);
                int landed = Integer.parseInt(input[4]);
                int moved = Integer.parseInt(input[5]);

                ChessPiece piece = new ChessPiece(player, landed, moved);
                ChessBoardLocation location = new ChessBoardLocation(color, index);
                Controller.getModel().setChessPieceAt(location, piece);
                Controller.getModel().grid[color][index].number_Of_Planes = numPlane;
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("GameFrame Sorry, no such file available");
        }
    }

    /**
     * @param Controller Current board
     */
    public void Save(GameController Controller) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("HH-mm-ss");
            Date date = new Date();
            String filename = PLAYER_NAMES[Controller.getCurrentPlayer()] + "-" + sdf.format(date);
            File file;
            FileWriter out;
            file = new File("..\\Aeroplane-Chess\\localgame\\" + filename + ".txt");
            out = new FileWriter(file);

            ChessBoard board = Controller.getModel();
            for (int player = 0; player < 4; player++) {
                for (int index = 0; index < board.getDimension() + board.getEndDimension() + 4; index++) {
                    ChessBoardLocation location = new ChessBoardLocation(player, index);
                    Square square = Controller.getModel().getGridAt(location);
                    if (square.getPiece() != null) {

                        // Player, color, index, numberOfPlanes, finished, moved
                        out.write(square.getPiece().getPlayer() + "\t" + location.getColor() +
                                "\t" + location.getIndex() + "\t" + square.getNumber_Of_Planes() + "\t" + square.getPiece().finished() +
                                "\t" + square.getPiece().getMoved());
                        out.write("\r\n");
                    }
                }
            }
            out.close();

            System.out.println("GameFrame saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void empty(GameController Controller) {
        ChessBoard board = Controller.getModel();
        for (int player = 0; player < Controller.getModel().number_Players; player++) {
            for (int index = 0; index < board.getDimension() + board.getEndDimension() + 4; index++) {
                ChessBoardLocation location = new ChessBoardLocation(player, index);
                board.removeChessPieceAt(location);
            }
        }
    }

    @Override
    public void onPlayerStartRound(int player) {
        statusLabel.setText(String.format("[%s] Please roll the dice", PLAYER_NAMES[player]));
    }

    @Override
    public void onPlayerEndRound(int player) {

    }
}
