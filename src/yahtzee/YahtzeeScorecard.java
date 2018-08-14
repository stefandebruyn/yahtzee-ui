package yahtzee;

public class YahtzeeScorecard {
    private int[] card = new int[13];
    private int bonus;
    
    
    
    public YahtzeeScorecard() {
        for (int i = 0; i < card.length; i++)
            card[i] = -1;
    }

    
    
    /**
     * Check if a roll will score in some category
     * 
     * @param category Category index
     * @param roll Dice values
     * @return Eligibility
     */
    public static boolean eligibleForCategory(int category, int[] roll) {
        // Tally face counts
        int[] counts = new int[6];
        
        for (int i : roll)
            counts[i-1]++;

        // Calculate length of largest sequence
        int inARow = 0, record = 0;

        for (int i = 0; i < counts.length; i++)
            if (counts[i] > 0)
                inARow++;
            else {
                if (inARow > record)
                    record = inARow;
                
                inARow = 0;
            }
        
        if (inARow > record)
            record = inARow;

        // Categories 1-6
        if (category >= 0 && category < 6)
                return true;

        switch (category) {
            // Three of a kind
            case 6:
                return (arrayContains(counts, 3));

            // Four of a kind
            case 7:
                return (arrayContains(counts, 4));

            // Full house
            case 8:
                return (arrayContains(counts, 3) && arrayContains(counts, 2));

            // Small straight
            case 9:
                return (record >= 4);

            // Large straight
            case 10:
                return (record >= 5);

            // Yahtzee
            case 11:
                return (arrayContains(counts, 5));

            // Chance
            case 12:
                return true;
        }

        return false;
    }
    
    
    
    /**
     * Get the score that a certain roll would yield in a certain category
     * 
     * @param category Category index
     * @param roll Dice values
     * @return Potential score
     */
    public int getPotentialCategoryScore(int category, int[] roll) {
        // Calculate dice sum
        int diceSum = 0;

        for (int i : roll)
            diceSum += i;

        // Categories 1-6
        if (category >= 0 && category < 6) {
            int score = 0;

            for (int i : roll)
                if (i == category + 1)
                    score += category + 1;

            return score;
        }

        switch (category) {
            // Three of a kind
            case 6:
                if (eligibleForCategory(category, roll))
                    return diceSum;
            break;

            // Four of a kind
            case 7:
                if (eligibleForCategory(category, roll))
                    return diceSum;
            break;

            // Full house
            case 8:
                if (eligibleForCategory(category, roll))
                    return 25;
            break;

            // Small straight
            case 9:
                if (eligibleForCategory(category, roll))
                    return 30;
            break;

            // Large straight
            case 10:
                if (eligibleForCategory(category, roll))
                    return 40;
            break;

            // Yahtzee
            case 11:
                if (eligibleForCategory(category, roll))
                    return 50;
            break;

            // Chance
            case 12:
                if (eligibleForCategory(category, roll))
                    return diceSum;
            break;
        }

        return 0;
    }

    
    
    /**
     * Score in a specific category
     * 
     * @param category Category index
     * @param roll Dice values
     * @return 0 for already scored, 1 for ineligible, 2 for success
     */
    public int scoreInCategory(int category, int[] roll) {
        // Verify eligibility
        if (card[category] != -1)
            return 0;
        else if (!eligibleForCategory(category, roll))
            return 1;

        // Augment score
        card[category] = getPotentialCategoryScore(category, roll);
        return 2;
    }

    
    
    /**
     * Get the score in some category
     * 
     * @param category Category index
     * @return Score
     */
    public int getCategoryScore(int category) { return card[category]; }
    
    
    
    /**
     * @return Lower section score
     */
    public int getLowerScore() {
        int total = 0;
        
        for (int i = 0; i < 6; i++)
            total += card[i];
        
        return total;
    }

    
    
    /**
     * Get the total score on the scorecard
     * 
     * @return Total score
     */
    public int getTotalScore() {
        int sum = 0;

        for (int category : card)
            sum += (category == -1 ? 0 : category);

        return sum + bonus;
    }
    
    
    
    /**
     * Add to the bonus score
     */
    public void incrementBonus(int inc) { bonus += inc; }
    
    
    
    /**
     * @return Bonus score
     */
    public int getBonusScore() { return bonus; }

    
    
    /**
     * Check if an int[] contains some int
     * 
     * @param arr Array
     * @param target Target
     * @return Whether or not target was found in array
     */
    private static boolean arrayContains(int[] arr, int target) {
        for (int i : arr)
            if (i == target)
                return true;

        return false;
    }
}
