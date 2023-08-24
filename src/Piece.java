public interface Piece {
    boolean isValidMove(int currentX, int currentY, int newX, int newY, Piece[][] board, ChessBoard chessBoard);
    boolean isWhite();
    String getImageName();
}
