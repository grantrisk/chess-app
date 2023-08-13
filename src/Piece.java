public interface Piece {
    boolean isValidMove(int currentX, int currentY, int newX, int newY, Piece[][] board);
    boolean isWhite();
    String getImageName();
}
