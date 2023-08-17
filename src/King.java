/**
 * @param isWhite Determine if the piece is white or black
 */
public record King(boolean isWhite) implements Piece {

    @Override
    public boolean isValidMove(int currentX, int currentY, int newX, int newY, Piece[][] board, ChessBoard.CastlingState castlingState) {
        int xDistance = Math.abs(newX - currentX);
        int yDistance = Math.abs(newY - currentY);

        // Check if the piece at the destination is not the same color
        boolean isDestinationOccupiedSameColor = board[newX][newY] != null && board[newX][newY].isWhite() == this.isWhite;

        // Check for castling
        if (xDistance == 0 && yDistance == 2 && !isDestinationOccupiedSameColor) {
            // Determine the direction of castling
            int direction = (newY > currentY) ? 1 : -1;

            // Check the squares between the king and rook for vacancy
            for (int y = currentY + direction; y != newY; y += direction) {
                if (board[currentX][y] != null) {
                    return false;
                }
            }

            if (castlingState.canCastle(this.isWhite, newY > currentY)) {
                return true;
            }
        }

        // King's specific movement rule (1 square in any direction)
        return !isDestinationOccupiedSameColor && xDistance <= 1 && yDistance <= 1;
    }


    @Override
    public String getImageName() {
        return isWhite ? "white-king-piece.png" : "dark-king-piece.png";
    }
}