package yahtzee;

public class YahtzeeScore implements Comparable<YahtzeeScore> {
    private String playerName;
    private int score;
    
    
    
    /**
     * Wrapper for a highscore
     * 
     * @param playerName Player name
     * @param score Score
     */
    public YahtzeeScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }
    
    
    
    /**
     * @return Player name
     */
    public String getPlayerName() { return playerName; }
    
    
    
    /**
     * @return Score
     */
    public int getScore() { return score; }
    
    
    
    /**
     * Get ordering
     * 
     * @param other Score to compare to
     * @return Where this score comes relative to the other in a highscore list
     */
    @Override public int compareTo(YahtzeeScore other) { return (other.getScore() - score); }
    
    
    
    /**
     * @return String representation
     */
    @Override public String toString() { return playerName + "   -   " + score; }
}
