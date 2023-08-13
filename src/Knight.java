/**
 * @param isWhite Determine if the piece is white or black
 */
public record Knight(boolean isWhite) implements Piece {

    @Override
    public boolean isValidMove(int currentX, int currentY, int newX, int newY, Piece[][] board) {
        int xDistance = Math.abs(newX - currentX);
        int yDistance = Math.abs(newY - currentY);

        boolean isDestinationOccupiedSameColor = board[newX][newY] != null && board[newX][newY].isWhite() == this.isWhite;

        return !isDestinationOccupiedSameColor && ((xDistance == 2 && yDistance == 1) || (xDistance == 1 && yDistance == 2));
    }

    @Override
    public String getImageName() {
        return isWhite ? "white-knight-piece.png" : "dark-knight-piece.png";
    }
}
