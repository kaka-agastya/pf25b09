
public enum State { 
    PLAYING("Playing"), 
    DRAW("It's a Draw"), 
    CROSS_WON("'X' Won"), 
    NOUGHT_WON("'O' Won");

    private String displayName;

    private State(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
