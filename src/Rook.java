/**
 * @param isWhite Determine if the piece is white or black
 */
public record Rook(boolean isWhite) implements Piece {

    @Override
    public boolean isValidMove(int currentX, int currentY, int newX, int newY, Piece[][] board, ChessBoard chessBoard) {
        boolean isDestinationOccupiedSameColor = board[newX][newY] != null && board[newX][newY].isWhite() == this.isWhite;

        if (isDestinationOccupiedSameColor || (currentX != newX && currentY != newY)) {
            return false;
        }

        int xDirection = currentX == newX ? 0 : (newX - currentX) / Math.abs(newX - currentX);
        int yDirection = currentY == newY ? 0 : (newY - currentY) / Math.abs(newY - currentY);

        int distance = currentX == newX ? Math.abs(newY - currentY) : Math.abs(newX - currentX);

        for (int i = 1; i < distance; i++) {
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
        return isWhite ? "white-rook-piece.png" : "dark-rook-piece.png";
    }
}
