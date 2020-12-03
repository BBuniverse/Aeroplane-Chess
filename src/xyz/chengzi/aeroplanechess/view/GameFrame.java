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

        setTitle("2020 CS102A Project Demo");
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
                if (dice != -1) {
                    statusLabel.setText(String.format("[%s] Rolled a %c (%d)",
                            PLAYER_NAMES[controller.getCurrentPlayer()], '\u267F' + dice, dice));
                } else {
                    JOptionPane.showMessageDialog(this, "You have already rolled the dice");
                }
            } else {
                JOptionPane.showMessageDialog(this, "You selected " + diceSelectorComponent.getSelectedDice());
            }
        });
        button.setFont(button.getFont().deriveFont(18.0f));
        button.setBounds(668, 756, 90, 30);
        add(button);


        JButton option = new JButton("extension");
        option.addActionListener((e) -> {
            Object[] options = {"restart", "load", "save"};
            String s = (String) JOptionPane.showInputDialog(null, "Please select your option:\n",
                    "Option", JOptionPane.PLAIN_MESSAGE, new ImageIcon("icon.png"), options, "restart");

            switch (s) {
                case "restart" -> Restart(controller);
                case "load" -> Load(controller);
                case "save" -> Save(controller);
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

    // Yellow 16-15-29
    public void Load(GameController Controller) {
        try {
            Controller.initializeGame();
            // Empty the board

            empty(Controller);
            String filename = JOptionPane.showInputDialog("Please input a value");
            File file = new File("..\\Aeroplane-Chess\\localgame\\" + filename + ".txt");

            BufferedReader in = new BufferedReader(new FileReader(file));  //
            String line;
            while ((line = in.readLine()) != null) {
                String[] input = line.split("\t");
                int player = Integer.parseInt(input[0]);
                int index = Integer.parseInt(input[1]);
                int color = Integer.parseInt(input[2]);

                ChessPiece piece = new ChessPiece(player);
                ChessBoardLocation location = new ChessBoardLocation(color, index);
                Controller.getModel().setChessPieceAt(location, piece);
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
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
            String filename = PLAYER_NAMES[Controller.getCurrentPlayer()] + " " + sdf.format(date) + ".txt";
            File file = new File("..\\Aeroplane-Chess\\localgame\\" + filename);

            FileWriter out = new FileWriter(file);

            ChessBoard board = Controller.getModel();
            for (int player = 0; player < 4; player++) {
                for (int index = 0; index < board.getDimension() + board.getEndDimension(); index++) {
                    ChessBoardLocation location = new ChessBoardLocation(player, index);
                    Square square = Controller.getModel().getGridAt(location);
                    if (square.getPiece() != null) {
                        out.write(square.getPiece().getPlayer() + "\t" + location.getIndex() + "\t" + location.getColor());
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
        for (int player = 0; player < 4; player++) {
            for (int index = 0; index < board.getDimension() + board.getEndDimension(); index++) {
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
