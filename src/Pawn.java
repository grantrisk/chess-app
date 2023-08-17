/**
 * @param isWhite Determine if the piece is white or black.
 */
public record Pawn(boolean isWhite) implements Piece {

    @Override
    public boolean isValidMove(int currentX, int currentY, int newX, int newY, Piece[][] board, ChessBoard.CastlingState castlingState) {
        int forwardDirection = isWhite ? -1 : 1; // White Pawns move up, Black Pawns move down
        boolean isDestinationOccupiedSameColor = board[newX][newY] != null && board[newX][newY].isWhite() == this.isWhite;
        boolean isDestinationOccupiedOppositeColor = board[newX][newY] != null && board[newX][newY].isWhite() != this.isWhite;

        // Normal move forward
        if (newX == currentX + forwardDirection && newY == currentY && !isDestinationOccupiedSameColor && !isDestinationOccupiedOppositeColor) {
            return true;
        }

        // Double move forward (only from starting position)
        if ((isWhite && currentX == 6 || !isWhite && currentX == 1) && newX == currentX + 2 * forwardDirection && newY == currentY && !isDestinationOccupiedSameColor && !isDestinationOccupiedOppositeColor) {
            return true;
        }

        // Diagonal capture
        return newX == currentX + forwardDirection && Math.abs(newY - currentY) == 1 && isDestinationOccupiedOppositeColor;
    }



    @Override
    public String getImageName() {
        return isWhite ? "white-pawn-piece.png" : "dark-pawn-piece.png";
    }
}
