/**
 * @param isWhite Determine if the piece is white or black
 */
public record Queen(boolean isWhite) implements Piece {

    @Override
    public boolean isValidMove(int currentX, int currentY, int newX, int newY, Piece[][] board, ChessBoard.CastlingState castlingState) {
        // Combining the logic of Rook and Bishop
        return new Rook(isWhite).isValidMove(currentX, currentY, newX, newY, board, castlingState) ||
                new Bishop(isWhite).isValidMove(currentX, currentY, newX, newY, board, castlingState);
    }


    @Override
    public String getImageName() {
        return isWhite ? "white-queen-piece.png" : "dark-queen-piece.png";
    }
}
