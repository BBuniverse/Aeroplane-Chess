package xyz.chengzi.aeroplanechess.view;

import xyz.chengzi.aeroplanechess.controller.GameController;
import xyz.chengzi.aeroplanechess.listener.GameStateListener;
import xyz.chengzi.aeroplanechess.model.ChessBoard;
import xyz.chengzi.aeroplanechess.model.ChessBoardLocation;
import xyz.chengzi.aeroplanechess.model.ChessPiece;
import xyz.chengzi.aeroplanechess.model.Square;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

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

//        ImageIcon icon=new ImageIcon("F:\\Aeroplane-Chess\\src\\xyz\\chengzi\\aeroplanechess\\view\\sky.jpg");
//        System.out.println(icon);
//        JLabel label=new JLabel(icon);
//        label.setBounds(0,0,772,825);
//        this.add(label,new Integer(Integer.MIN_VALUE));


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
                int dice1 = controller.getDices()[1];

                if (dice != -1) {
                    statusLabel.setText(String.format("[%s] Rolled a %c (%d) and %c (%d)",
                            PLAYER_NAMES[controller.getCurrentPlayer()], '\u267F' + dice, dice, '\u267F' + dice1, dice1));
                } else {
                    dice = controller.getDices()[0];
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

                int index = JOptionPane.showOptionDialog(null, "Returns the option of your choice",
                        PLAYER_NAMES[controller.getCurrentPlayer()] + " Click a button",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
//
//                if ((index < 0) || (index > 3) || steps[index] < 0) {
//                    controller.changeRolledNumber(0);
//                } else {
                controller.changeRolledNumber(steps[index]);
//                }
            } else {
                // Manually choose steps
                controller.changeRolledNumber((int) diceSelectorComponent.getSelectedDice() * 100);
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

                ChessPiece piece = new ChessPiece(player, landed);
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

                        // Player, color, index, numberOfPlanes, finished
                        out.write(square.getPiece().getPlayer() + "\t" + location.getColor() +
                                "\t" + location.getIndex() + "\t" + square.number_Of_Planes + "\t" + square.getPiece().finished());
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
