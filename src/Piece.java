public interface Piece {
    boolean isValidMove(int currentX, int currentY, int newX, int newY, Piece[][] board, ChessBoard.CastlingState castlingState);
    boolean isWhite();
    String getImageName();
}
