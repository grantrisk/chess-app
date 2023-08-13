import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

public class ChessBoard {
    private final Piece[][] pieces = new Piece[8][8];
    private final JFrame frame;
    private final JButton[][] buttons;
    private Piece selectedPiece = null;
    private int selectedX = -1;
    private int selectedY = -1;

    public ChessBoard() {
        frame = new JFrame("Chess");
        buttons = new JButton[8][8];
        frame.setLayout(new GridLayout(8, 8));

        initializePieces();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                JButton button = createButton(i, j);
                buttons[i][j] = button;
                frame.add(button);
            }
        }

        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new ChessBoard();
    }

    private JButton createButton(int i, int j) {
        JButton button = new JButton();
        setIcon(button, pieces[i][j]);
        button.addActionListener(new ButtonClickListener(i, j));
        button.setOpaque(true);
        button.setBorderPainted(true);
        Color bgColor = ((i + j) % 2 == 0) ? Color.WHITE : new Color(162, 92, 0, 255);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createLineBorder(bgColor, 3));
        return button;
    }

    private void initializePieces() {
        pieces[0] = new Piece[]{new Rook(false), new Knight(false), new Bishop(false), new Queen(false), new King(false), new Bishop(false), new Knight(false), new Rook(false)};
        pieces[1] = new Piece[]{new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false), new Pawn(false)};
        pieces[6] = new Piece[]{new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true), new Pawn(true)};
        pieces[7] = new Piece[]{new Rook(true), new Knight(true), new Bishop(true), new Queen(true), new King(true), new Bishop(true), new Knight(true), new Rook(true)};
    }

    private void setIcon(JButton button, Piece piece) {
        if (piece != null) {
            URL imgUrl = getClass().getClassLoader().getResource(piece.getImageName());
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image image = icon.getImage().getScaledInstance(50, 65, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(image));
            } else {
                System.err.println("Couldn't find file: " + piece.getImageName());
            }
        } else {
            button.setIcon(null);
        }
    }

    private class ButtonClickListener implements ActionListener {
        int x, y;

        public ButtonClickListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            handleAction();
        }

        private void handleAction() {
            Piece clickedPiece = pieces[x][y];
            if (selectedPiece != null) {
                handleSelectedPiece(clickedPiece);
            } else {
                handleUnselectedPiece(clickedPiece);
            }
            playClickSound();
        }

        private void handleSelectedPiece(Piece clickedPiece) {
            if (clickedPiece != null && clickedPiece.isWhite() == selectedPiece.isWhite()) {
                switchSelection(clickedPiece);
            } else {
                processMove(clickedPiece);
            }
        }

        private void switchSelection(Piece clickedPiece) {
            selectedPiece = clickedPiece;
            selectedX = x;
            selectedY = y;
            unhighlightSquares();
            highlightValidMoves();
            System.out.println("Switched selection to another piece of the same color.");
        }

        private void processMove(Piece clickedPiece) {
            unhighlightSquares();
            if (selectedPiece.isValidMove(selectedX, selectedY, x, y, pieces)) {
                makeMove();
            } else {
                System.out.println("You cannot move the selected piece to this square.");
            }
            resetSelectedPiece();
        }

        private void makeMove() {
            if (isKingInCheck(selectedPiece.isWhite())) {
                System.out.println("This move would put you in check. Please choose a different move.");
                return;
            }

            System.out.println("You can move the selected piece to this square.");
            movePiece();
            checkCheckmate();
        }

        private void movePiece() {
            pieces[x][y] = selectedPiece;
            pieces[selectedX][selectedY] = null;
            setIcon(buttons[x][y], selectedPiece);
            setIcon(buttons[selectedX][selectedY], null);
        }

        private void checkCheckmate() {
            if (isCheckmate(!selectedPiece.isWhite())) {
                System.out.println("Checkmate! Game over.");
                showGameOverDialog();
            }
        }

        private void showGameOverDialog() {
            JDialog gameOverDialog = new JDialog(frame, "Game Over", true);
            gameOverDialog.setLayout(new BorderLayout());

            // Customize the label
            JLabel label = new JLabel("Checkmate! Game over.", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 18));
            label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Customize the button
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> gameOverDialog.dispose());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);

            // Add components to the dialog
            gameOverDialog.add(label, BorderLayout.CENTER);
            gameOverDialog.add(buttonPanel, BorderLayout.SOUTH);

            // Set dialog size and show it
            gameOverDialog.setSize(300, 150);
            gameOverDialog.setLocationRelativeTo(frame);
            gameOverDialog.setVisible(true);
        }

        private void resetSelectedPiece() {
            selectedPiece = null;
            selectedX = -1;
            selectedY = -1;
        }

        private void handleUnselectedPiece(Piece clickedPiece) {
            selectedPiece = clickedPiece;
            selectedX = x;
            selectedY = y;
            if (selectedPiece != null) {
                String pieceType = selectedPiece.getClass().getSimpleName();
                String color = selectedPiece.isWhite() ? "White" : "Dark";
                System.out.println("Selected a " + color + " " + pieceType);
                highlightValidMoves();
            } else {
                System.out.println("Selected an empty square");
            }
        }

        private void playClickSound() {
            try {
                URL soundUrl = getClass().getClassLoader().getResource("click-sound.wav"); // Replace with your sound file
                assert soundUrl != null;
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                ex.printStackTrace();
            }
        }

        // Method to check if a King is in check
        private boolean isKingInCheck(boolean isWhite) {
            // TODO: Implement this method
            return false;
            /*int kingX = -1, kingY = -1;

            // Find the King's position
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    Piece piece = pieces[i][j];
                    if (piece instanceof King && piece.isWhite() == isWhite) {
                        kingX = i;
                        kingY = j;
                        break;
                    }
                }
            }

            // Check if any opposing pieces can attack the King
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    Piece piece = pieces[i][j];
                    if (piece != null && piece.isWhite() != isWhite && piece.isValidMove(i, j, kingX, kingY, pieces)) {
                        return true; // King is in check
                    }
                }
            }

            return false; // King is not in check*/
        }

        // Method to check if a player is in checkmate
        private boolean isCheckmate(boolean isWhite) {
            // TODO: Implement this method
            return false;
            /*if (!isKingInCheck(isWhite)) {
                return false; // Player is not in check, so cannot be in checkmate
            }

            // Check if there are any legal moves that would get the King out of check
            Piece[][] tempPieces = new Piece[8][8];
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    Piece piece = pieces[i][j];
                    if (piece != null && piece.isWhite() == isWhite) {
                        for (int x = 0; x < 8; x++) {
                            for (int y = 0; y < 8; y++) {
                                if (piece.isValidMove(i, j, x, y, pieces)) {
                                    // Simulate the move
                                    System.arraycopy(pieces[i], 0, tempPieces[i], 0, 8);
                                    tempPieces[x][y] = tempPieces[i][j];
                                    tempPieces[i][j] = null;

                                    // Check if the King is still in check after the move
                                    if (!isKingInCheck(isWhite)) {
                                        return false; // There is a legal move that gets the King out of check
                                    }

                                    // Undo the simulated move
                                    System.arraycopy(tempPieces[i], 0, pieces[i], 0, 8);
                                }
                            }
                        }
                    }
                }
            }

            return true; // No legal moves that get the King out of check, so player is in checkmate*/
        }

        private void highlightValidMoves() {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (selectedPiece.isValidMove(selectedX, selectedY, i, j, pieces)) {
                        buttons[i][j].setBorder(BorderFactory.createLineBorder(Color.GREEN, 3)); // Set green border
                    }
                }
            }
        }

        private void unhighlightSquares() {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if ((i + j) % 2 == 0) {
                        buttons[i][j].setBackground(Color.WHITE);
                    } else {
                        buttons[i][j].setBackground(new Color(162, 92, 0, 255));
                    }
                    buttons[i][j].setBorder(BorderFactory.createEmptyBorder()); // Reset border
                }
            }
        }
    }
}
