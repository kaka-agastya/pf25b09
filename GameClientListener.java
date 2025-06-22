
public interface GameClientListener {
    void onConnected(String message);
    void onDisconnected(String message);
    void onPlayerAssigned(String role); // "X" or "O"
    void onTurnChange(boolean isMyTurn, String playerSymbol);
    void onMoveReceived(String playerSymbol, int row, int col);
    void onGameOver(State finalState);
    void onInvalidMove(String message);
    void onMessage(String message);
    void onConnectionError(String message);
}
