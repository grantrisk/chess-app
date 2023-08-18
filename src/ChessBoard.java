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
    private final CastlingState castlingState = new CastlingState(false, false, false, false, false, false);

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

    private boolean isCheckmate(boolean isWhite) {
        if (!isKingInCheck(isWhite)) {
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
                            if (piece.isValidMove(i, j, x, y, pieces, castlingState)) {
                                // Simulate the move
                                System.arraycopy(pieces[i], 0, tempPieces[i], 0, 8);
                                tempPieces[x][y] = tempPieces[i][j];
                                tempPieces[i][j] = null;

                                // Check if the King's square is still attacked after the move
                                if (isSquareNotAttacked(x, y, isWhite)) {
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

        return true; // No legal moves that get the King out of check, so player is in checkmate
    }

    private boolean isKingInCheck(boolean isWhite) {
        // Find the King's position
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = pieces[i][j];
                if (piece instanceof King && piece.isWhite() == isWhite) {
                    // Check if the King's square is attacked
                    return !isSquareNotAttacked(i, j, isWhite);
                }
            }
        }
        return false; // King not found, so it's not in check
    }

    private boolean isSquareNotAttacked(int x, int y, boolean isWhite) {
        // Iterate through the board to check if any of the opponent's pieces can attack the given square
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = pieces[i][j];
                if (piece != null && piece.isWhite() != isWhite && piece.isValidMove(i, j, x, y, pieces, castlingState)) {
                    return false; // The square is attacked
                }
            }
        }
        return true; // The square is not attacked
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
            System.out.println("Selected piece: " + selectedPiece);
            System.out.println("Clicked on square (" + x + ", " + y + ")");

            Piece clickedPiece = pieces[x][y];
            if (selectedPiece != null) {
                // We currently have a piece selected
                handleSelectedPiece(clickedPiece);
            } else {
                // We do not have a piece selected
                handleUnselectedPiece(clickedPiece);
            }
            playClickSound();
        }

        private void handleSelectedPiece(Piece clickedPiece) {
            if (clickedPiece != null && clickedPiece.isWhite() == selectedPiece.isWhite()) {
                // Clicked on another piece of the same color
                switchSelection(clickedPiece);
            } else {
                // Clicked on an empty square or a square with an opposing piece
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
            if (selectedPiece.isValidMove(selectedX, selectedY, x, y, pieces, castlingState)) {
                // The selected piece can move to the clicked square


                // Check if the move puts us in check



                if (isKingInCheck(selectedPiece.isWhite())) {
                    System.out.println("This move would put you in check. Please choose a different move.");
                    return;
                }

                System.out.println("You can move the selected piece to this square.");
                movePiece();

                // Check if the move is a castling move
                // if the King moved more than two squares, then move the rook as well
                System.out.println("x: " + x + " y: " + y);
                if (selectedPiece instanceof King && Math.abs(y - selectedY) > 1){
                    System.out.println("Trying to castle. Swap the rook too.");
                    castleRook(y > selectedY);
                }

                // Check if opposing King is in check / checkmate
                if (isKingInCheck(!selectedPiece.isWhite())) {
                    System.out.println("Opposing King is in check.");
                    if (isCheckmate(!selectedPiece.isWhite())) {
                        System.out.println("Checkmate! Game over.");
                        showGameOverDialog();
                    }
                }

                if (selectedPiece instanceof Pawn) {
                    // Check if the Pawn has reached the other side of the board
                    if (selectedPiece.isWhite() && x == 0 || !selectedPiece.isWhite() && x == 7) {
                        // Promote the Pawn to a Queen
                        pieces[x][y] = new Queen(selectedPiece.isWhite());
                        setIcon(buttons[x][y], pieces[x][y]);
                    }
                }


//                checkCheckmate();
            } else {
                // The selected piece cannot move to the clicked square
                System.out.println("You cannot move the selected piece to this square.");
            }

            resetSelectedPiece();
        }
        private void castleRook(Boolean isKingSide) {
            System.out.println("selectedX: " + selectedX + " selectedY: " + selectedY);
            int rookX, rookY;
            int newRookX = selectedX;
            boolean isWhite = selectedPiece.isWhite();

            if (isKingSide) {
                rookX = selectedX;
                rookY = selectedY + 3; // Assuming the rook is 3 squares to the right of the king
                pieces[newRookX][selectedY + 1] = new Rook(selectedPiece.isWhite());

                if (isWhite) {
                    castlingState.whiteRightRookMoved = true;
                } else {
                    castlingState.blackRightRookMoved = true;
                }
            } else {
                rookX = selectedX;
                rookY = selectedY - 4; // Assuming the rook is 4 squares to the left of the king
                pieces[newRookX][selectedY - 1] = new Rook(selectedPiece.isWhite());

                if (isWhite) {
                    castlingState.whiteLeftRookMoved = true;
                } else {
                    castlingState.blackLeftRookMoved = true;
                }
            }

            buttons[newRookX][rookY < selectedY ? selectedY - 1 : selectedY + 1].setIcon(buttons[rookX][rookY].getIcon()); // Update the button icon using the original rook's icon
            setIcon(buttons[rookX][rookY], null);
            pieces[rookX][rookY] = null; // Clear the original rook's place

            System.out.println(castlingState);
        }



        private void movePiece() {
            updateCastlingState();
            updatePiecePositions();
            updateButtonIcons();
        }

        private void updateCastlingState() {
            if (selectedPiece instanceof King || selectedPiece instanceof Rook) {
                boolean isWhite = selectedPiece.isWhite();
                boolean isKing = selectedPiece instanceof King;
                boolean isRightRook = selectedPiece instanceof Rook && selectedY == 7;
                boolean isLeftRook = selectedPiece instanceof Rook && selectedY == 0;

                if (isWhite) {
                    castlingState.whiteKingMoved = castlingState.whiteKingMoved || isKing;
                    castlingState.whiteRightRookMoved = castlingState.whiteRightRookMoved || isRightRook;
                    castlingState.whiteLeftRookMoved = castlingState.whiteLeftRookMoved || isLeftRook;
                } else {
                    castlingState.blackKingMoved = castlingState.blackKingMoved || isKing;
                    castlingState.blackRightRookMoved = castlingState.blackRightRookMoved || isRightRook;
                    castlingState.blackLeftRookMoved = castlingState.blackLeftRookMoved || isLeftRook;
                }

                System.out.println(castlingState);
            }
        }

        private void updatePiecePositions() {
            pieces[x][y] = selectedPiece;
            pieces[selectedX][selectedY] = null;
        }

        private void updateButtonIcons() {
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

        private void highlightValidMoves() {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (selectedPiece.isValidMove(selectedX, selectedY, i, j, pieces, castlingState)) {
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

    public class CastlingState {
        public boolean whiteKingMoved;
        public boolean blackKingMoved;
        public boolean whiteLeftRookMoved;
        public boolean whiteRightRookMoved;
        public boolean blackLeftRookMoved;
        public boolean blackRightRookMoved;

        // Constructor
        public CastlingState(boolean whiteKingMoved, boolean blackKingMoved, boolean whiteLeftRookMoved, boolean whiteRightRookMoved, boolean blackLeftRookMoved, boolean blackRightRookMoved) {
            this.whiteKingMoved = whiteKingMoved;
            this.blackKingMoved = blackKingMoved;
            this.whiteLeftRookMoved = whiteLeftRookMoved;
            this.whiteRightRookMoved = whiteRightRookMoved;
            this.blackLeftRookMoved = blackLeftRookMoved;
            this.blackRightRookMoved = blackRightRookMoved;
        }

        public boolean canCastle(boolean isWhite, boolean isKingSide) {
            // Check if King is in check
            if (isKingInCheck(isWhite)) {
                System.out.println("You cannot castle while in check.");
                return false;
            }
            // Check if King and Rook have moved and if the squares are attacked
            if (isWhite) {
                if (isKingSide) {
                    return !whiteKingMoved && !whiteRightRookMoved && isSquareNotAttacked(7, 5, true) && isSquareNotAttacked(7, 6, true);
                } else {
                    return !whiteKingMoved && !whiteLeftRookMoved && isSquareNotAttacked(7, 3, true) && isSquareNotAttacked(7, 2, true) && isSquareNotAttacked(7, 1, true);
                }
            } else {
                if (isKingSide) {
                    return !blackKingMoved && !blackRightRookMoved && isSquareNotAttacked(0, 5, false) && isSquareNotAttacked(0, 7, false);
                } else {
                    return !blackKingMoved && !blackLeftRookMoved && isSquareNotAttacked(0, 3, false) && isSquareNotAttacked(0, 2, false) && isSquareNotAttacked(0, 1, false);
                }
            }
        }

        @Override
        public String toString() {
            return "CastlingState{" +
                    "whiteKingMoved=" + whiteKingMoved +
                    ", blackKingMoved=" + blackKingMoved +
                    ", whiteLeftRookMoved=" + whiteLeftRookMoved +
                    ", whiteRightRookMoved=" + whiteRightRookMoved +
                    ", blackLeftRookMoved=" + blackLeftRookMoved +
                    ", blackRightRookMoved=" + blackRightRookMoved +
                    '}';
        }
    }
}
