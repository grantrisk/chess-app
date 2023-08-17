/**
 * @param isWhite Determine if the piece is white or black
 */
public record Bishop(boolean isWhite) implements Piece {

    @Override
    public boolean isValidMove(int currentX, int currentY, int newX, int newY, Piece[][] board, ChessBoard.CastlingState castlingState) {
        int xDistance = Math.abs(newX - currentX);
        int yDistance = Math.abs(newY - currentY);

        boolean isDestinationOccupiedSameColor = board[newX][newY] != null && board[newX][newY].isWhite() == this.isWhite;

        if (isDestinationOccupiedSameColor || xDistance != yDistance) {
            return false;
        }

        // Check if there are any pieces in the path of the movement
        int xDirection = (newX - currentX) / xDistance; // +1 if moving right, -1 if moving left
        int yDirection = (newY - currentY) / yDistance; // +1 if moving down, -1 if moving up

        for (int i = 1; i < xDistance; i++) {
            int xCheck = currentX + i * xDirection;
            int yCheck = currentY + i * yDirection;

            if (board[xCheck][yCheck] != null) {
                return false; // There is a piece in the way
            }
        }

        return true;
    }



    @Override
    public String getImageName() {
        return isWhite ? "white-bishop-piece.png" : "dark-bishop-piece.png";
    }
}
