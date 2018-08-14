package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

import yahtzee.YahtzeeScore;
import yahtzee.YahtzeeScorecard;

public class YahtzeeGUI extends javax.swing.JFrame {
    private final javax.swing.JLabel[] highscoreLabels, diceLabels, diceIcons;
    private final java.awt.Checkbox[] diceCheckboxes;
    private final javax.swing.JButton[] scoreButtons;
    private final String HIGHSCORE_FILE_NAME = "highscores.dat";
    private final int MAX_TURNS = 13;
    private final int DICE_FACES = 6;
    private final int REROLLS_ALLOWED = 2;
    
    private Queue<YahtzeeScore> highscores = new PriorityQueue<>();
    private ArrayList<Integer> reactivationQueue = new ArrayList<>();
    private YahtzeeScorecard scorecard = new YahtzeeScorecard();
    private String playerName;
    private int[] roll = new int[5];
    private int turn = 0;
    private int rerolls = REROLLS_ALLOWED;
    private int yahtzees = 0;
    private boolean grantedLowerBonus = false;
   
    
    
    /**
     * Build the GUI and initiate the first turn
     */
    public YahtzeeGUI() {
        initComponents();
        
        // Get player name
        playerName = JOptionPane.showInputDialog("Enter your name:");
        
        panGame.setBorder(javax.swing.BorderFactory.createTitledBorder("Game - " + playerName));
        
        // Allocate element collections for convenient iteration
        diceLabels = new javax.swing.JLabel[] {
            labDice1, labDice2, labDice3, labDice4, labDice5
        };
        highscoreLabels = new javax.swing.JLabel[] {
            labHighscore1, labHighscore2, labHighscore3, labHighscore4, labHighscore5, labHighscore6, labHighscore7, labHighscore8
        };
        diceIcons = new javax.swing.JLabel[] {
            labDiceIcon1, labDiceIcon2, labDiceIcon3, labDiceIcon4, labDiceIcon5
        };
        diceCheckboxes = new java.awt.Checkbox[] {
            btDiceKeep1, btDiceKeep2, btDiceKeep3, btDiceKeep4, btDiceKeep5
        };
        scoreButtons = new javax.swing.JButton[] {
            btScoreAces, btScoreTwos, btScoreThrees, btScoreFours, btScoreFives, btScoreSixes, btScoreThreeKind, btScoreFourKind, btScoreFullHouse, btScoreSmallStraight, btScoreLargeStraight, btScoreYahtzee, btScoreChance
        };
        
        // Initiate first turn
        loadHighscores();
        advanceTurn();
    }
    
    
    
    /**
     * Score in a specific category given current roll
     * 
     * @param category 
     */
    private void scoreInCategory(int category) {
        if (category == 11 && scorecard.eligibleForCategory(11, roll))
            yahtzees++;
        
        scorecard.scoreInCategory(category, roll);
        scoreButtons[category].setEnabled(false);
        
        while (reactivationQueue.size() > 0)
            scoreButtons[reactivationQueue.remove(0)].setEnabled(true);
        
        // Lower section bonus
        if (scorecard.getLowerScore() >= 63 && !grantedLowerBonus) {
            grantedLowerBonus = true;
            scorecard.incrementBonus(50);
        }
        
        advanceTurn();
    }
    
    
    
    /**
     * Update the text in the scorecard buttons
     */
    private void updateScoreButtonTexts() {
        for (int i = 0; i < scoreButtons.length; i++)
            if (scoreButtons[i].isEnabled())
                scoreButtons[i].setText("Score " + scorecard.getPotentialCategoryScore(i, roll));
                
    }
    
    
    
    /**
     * Update the text in the scorecard totals
     */
    private void updateScoreTotalTexts() {
        int lowerTotal = 0;
        
        for (int i = 0; i < 6; i++) {
            int sc = scorecard.getCategoryScore(i);
            lowerTotal += (sc == -1 ? 0 : sc);
        }
        
        int upperTotal = 0;
        
        for (int i = 6; i < 13; i++) {
            int sc = scorecard.getCategoryScore(i);
            upperTotal += (sc == -1 ? 0 : sc);
        }
        
        labLowerScore.setText("Lower: " + lowerTotal);
        labUpperScore.setText("Upper: " + upperTotal);
        labBonusScore.setText("Bonus: " + scorecard.getBonusScore());
        labTotalScore.setText("Score: " + scorecard.getTotalScore());
    }
    
    
    
    /**
     * Roll the dice and decrement rerolls if necessary
     * 
     * @param initial 
     */
    private void roll(boolean initial) {
        // Roll dice
        int forceNum = 0; // (int)(Math.random() * 6) + 1;
        
        for (int i = 0; i < roll.length; i++)
            if (!diceCheckboxes[i].getState()) {
                int num = (int)(Math.random() * 6) + 1;
                roll[i] = (forceNum == 0 ? num : forceNum);
                diceLabels[i].setText("" + roll[i]);
            }
        
        updateScoreButtonTexts();
        updateDiceIcons();
        
        // Multiple yahtzees bonus
        if (scorecard.eligibleForCategory(11, roll) && yahtzees > 0) {
            scorecard.incrementBonus(100);

            // Force correct scoring
            int yahtzeeFace = roll[0];

            if (scorecard.getCategoryScore(yahtzeeFace - 1) == -1)
                for (int i = 0; i < 13; i++)
                    if (i != yahtzeeFace - 1 && scoreButtons[i].isEnabled()) {
                        scoreButtons[i].setEnabled(false);
                        reactivationQueue.add(i);
                    }
        }
        
        if (initial)
            return;
        
        // Update rerolls
        rerolls--;
        
        btReroll.setText("Reroll (" + rerolls + ")");
        
        if (rerolls == 0)
            btReroll.setEnabled(false);
    }
    
    
    
    /**
     * Increment turns by 1 and reset the necessary game features
     */
    private void advanceTurn() {
        turn++;
        
        if (turn <= MAX_TURNS)
            labTurn.setText("Turn " + turn);
        else {
            labTurn.setText("Done!");
            btReroll.setEnabled(false);
            
            updateScoreTotalTexts();
            saveHighscores();
            
            return;
        }
        
        rerolls = REROLLS_ALLOWED;
        
        for (java.awt.Checkbox box : diceCheckboxes)
            box.setState(false);
        
        btReroll.setEnabled(true);
        btReroll.setText("Reroll (" + rerolls + ")");
        
        roll(true);
        updateScoreTotalTexts();
    }
    
    
    
    /**
     * Load highscores from the designated highscore file
     */
    private void loadHighscores() {
        Scanner reader;
        File file = new File(HIGHSCORE_FILE_NAME);
        
        try {
            file.createNewFile();
        } catch (IOException e) {}
        
        // Read from file
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            return;
        }
        
        while (reader.hasNextLine()) {
            String[] data = reader.nextLine().split("-");
            highscores.add(new YahtzeeScore(data[0], Integer.parseInt(data[1])));
        }
        
        reader.close();
        
        // Update UI
        updateHighscoreUI();
    }
    
    
    
    /**
     * Add current scorecard score to highscores and write to disk
     */
    private void saveHighscores() {
        // Enqueue score
        YahtzeeScore score = new YahtzeeScore(playerName, scorecard.getTotalScore());
        highscores.add(score);
        
        while (highscores.size() > highscoreLabels.length)
            highscores.poll();
        
        // Write
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(HIGHSCORE_FILE_NAME));
            Iterator<YahtzeeScore> iterator = highscores.iterator();
            
            while (iterator.hasNext()) {
                YahtzeeScore high = iterator.next();
                writer.println(high.getPlayerName() + "-" + high.getScore());
            }
            
            writer.close();
            
        } catch (IOException e) {}
        
        // Update UI
        updateHighscoreUI();
    }
    
    
    
    /**
     * Update scoreboard
     */
    private void updateHighscoreUI() {
        Queue<YahtzeeScore> clone = new PriorityQueue<>(highscores);
        
        for (int i = 0; i < highscoreLabels.length; i++) {
            String str = "No highscore";
            
            if (!clone.isEmpty())
                str = clone.poll().toString();
            
            highscoreLabels[i].setText(str);
        }
    }
    
    
    
    /**
     * Update dice icons
     */
    private void updateDiceIcons() {
        for (int i = 0; i < diceIcons.length; i++)
            diceIcons[i].setIcon(createImageIcon("/images/face" + roll[i] + ".png"));
    }
    
    
    
    /**
     * Create an image icon from file path
     * 
     * @param path File path
     * @return ImageIcon representation
     */
    private ImageIcon createImageIcon(String path) {
        java.net.URL source = getClass().getResource(path);
        
        if (source != null)
            return new ImageIcon(source);
        
        return null;
    }
    
    
    
    /**
     * Boot the game
     * 
     * @param args 
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(YahtzeeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(YahtzeeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(YahtzeeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(YahtzeeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new YahtzeeGUI().setVisible(true);
            }
        });
    }
    
    
    
    /**
     * Build UI
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panDice = new javax.swing.JPanel();
        btDiceKeep1 = new java.awt.Checkbox();
        btDiceKeep2 = new java.awt.Checkbox();
        btDiceKeep3 = new java.awt.Checkbox();
        btDiceKeep4 = new java.awt.Checkbox();
        btDiceKeep5 = new java.awt.Checkbox();
        labDice1 = new javax.swing.JLabel();
        labDice2 = new javax.swing.JLabel();
        labDice3 = new javax.swing.JLabel();
        labDice4 = new javax.swing.JLabel();
        labDice5 = new javax.swing.JLabel();
        labDiceIcon1 = new javax.swing.JLabel();
        labDiceIcon2 = new javax.swing.JLabel();
        labDiceIcon3 = new javax.swing.JLabel();
        labDiceIcon4 = new javax.swing.JLabel();
        labDiceIcon5 = new javax.swing.JLabel();
        panScorecard = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        labAces = new java.awt.Label();
        labFours = new java.awt.Label();
        labTwos = new java.awt.Label();
        labThrees = new java.awt.Label();
        labFives = new java.awt.Label();
        labSixes = new java.awt.Label();
        btScoreAces = new javax.swing.JButton();
        btScoreTwos = new javax.swing.JButton();
        btScoreThrees = new javax.swing.JButton();
        btScoreFours = new javax.swing.JButton();
        btScoreFives = new javax.swing.JButton();
        btScoreSixes = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        labThreeKind = new java.awt.Label();
        labFourKind = new java.awt.Label();
        labFullHouse = new java.awt.Label();
        labSmallStraight = new java.awt.Label();
        labLargeStraight = new java.awt.Label();
        labYahtzee = new java.awt.Label();
        labChance = new java.awt.Label();
        btScoreThreeKind = new javax.swing.JButton();
        btScoreFourKind = new javax.swing.JButton();
        btScoreSmallStraight = new javax.swing.JButton();
        btScoreLargeStraight = new javax.swing.JButton();
        btScoreYahtzee = new javax.swing.JButton();
        btScoreChance = new javax.swing.JButton();
        btScoreFullHouse = new javax.swing.JButton();
        labLowerScore = new javax.swing.JLabel();
        labUpperScore = new javax.swing.JLabel();
        labBonusScore = new javax.swing.JLabel();
        labTotalScore = new javax.swing.JLabel();
        panGame = new javax.swing.JPanel();
        labTurn = new java.awt.Label();
        btReroll = new javax.swing.JButton();
        btRestart = new javax.swing.JButton();
        panHighScores = new javax.swing.JPanel();
        labHighscore1 = new javax.swing.JLabel();
        labHighscore2 = new javax.swing.JLabel();
        labHighscore3 = new javax.swing.JLabel();
        labHighscore4 = new javax.swing.JLabel();
        labHighscore5 = new javax.swing.JLabel();
        labHighscore6 = new javax.swing.JLabel();
        labHighscore7 = new javax.swing.JLabel();
        labHighscore8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        panDice.setBorder(javax.swing.BorderFactory.createTitledBorder("Dice"));
        panDice.setName("Dice"); // NOI18N

        btDiceKeep1.setLabel("Keep");

        btDiceKeep2.setLabel("Keep");

        btDiceKeep3.setLabel("Keep");

        btDiceKeep4.setLabel("Keep");

        btDiceKeep5.setLabel("Keep");

        labDice1.setText("Dice 1");

        labDice2.setText("Dice 2");

        labDice3.setText("Dice 3");

        labDice4.setText("Dice 4");

        labDice5.setText("Dice 5");

        javax.swing.GroupLayout panDiceLayout = new javax.swing.GroupLayout(panDice);
        panDice.setLayout(panDiceLayout);
        panDiceLayout.setHorizontalGroup(
            panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panDiceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panDiceLayout.createSequentialGroup()
                        .addComponent(labDice5)
                        .addGap(18, 18, 18)
                        .addComponent(labDiceIcon5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btDiceKeep5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panDiceLayout.createSequentialGroup()
                        .addComponent(labDice1)
                        .addGap(18, 18, 18)
                        .addComponent(labDiceIcon1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btDiceKeep1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panDiceLayout.createSequentialGroup()
                        .addComponent(labDice4)
                        .addGap(18, 18, 18)
                        .addComponent(labDiceIcon4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btDiceKeep4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panDiceLayout.createSequentialGroup()
                        .addComponent(labDice2)
                        .addGap(18, 18, 18)
                        .addComponent(labDiceIcon2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btDiceKeep2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panDiceLayout.createSequentialGroup()
                        .addComponent(labDice3)
                        .addGap(18, 18, 18)
                        .addComponent(labDiceIcon3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btDiceKeep3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panDiceLayout.setVerticalGroup(
            panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panDiceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panDiceLayout.createSequentialGroup()
                        .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panDiceLayout.createSequentialGroup()
                                .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(panDiceLayout.createSequentialGroup()
                                        .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(btDiceKeep1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(labDice1)
                                                .addComponent(labDiceIcon1)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btDiceKeep2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labDice2)
                                        .addComponent(labDiceIcon2)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btDiceKeep3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(labDice3)
                                .addComponent(labDiceIcon3)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btDiceKeep4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labDice4)
                        .addComponent(labDiceIcon4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btDiceKeep5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panDiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labDice5)
                        .addComponent(labDiceIcon5)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panScorecard.setBorder(javax.swing.BorderFactory.createTitledBorder("Scorecard"));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Upper"));

        labAces.setAlignment(java.awt.Label.RIGHT);
        labAces.setText("Aces");

        labFours.setAlignment(java.awt.Label.RIGHT);
        labFours.setText("Fours");

        labTwos.setAlignment(java.awt.Label.RIGHT);
        labTwos.setText("Twos");

        labThrees.setAlignment(java.awt.Label.RIGHT);
        labThrees.setText("Threes");

        labFives.setAlignment(java.awt.Label.RIGHT);
        labFives.setText("Fives");

        labSixes.setAlignment(java.awt.Label.RIGHT);
        labSixes.setText("Sixes");

        btScoreAces.setText("jButton6");
        btScoreAces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreAcesActionPerformed(evt);
            }
        });

        btScoreTwos.setText("jButton5");
        btScoreTwos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreTwosActionPerformed(evt);
            }
        });

        btScoreThrees.setText("jButton4");
        btScoreThrees.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreThreesActionPerformed(evt);
            }
        });

        btScoreFours.setText("jButton3");
        btScoreFours.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreFoursActionPerformed(evt);
            }
        });

        btScoreFives.setText("jButton2");
        btScoreFives.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreFivesActionPerformed(evt);
            }
        });

        btScoreSixes.setText("jButton1");
        btScoreSixes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreSixesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labAces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labTwos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labThrees, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labFours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labFives, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labSixes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btScoreAces, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btScoreTwos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btScoreThrees, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btScoreFours, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btScoreFives, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btScoreSixes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labAces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreAces))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labTwos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreTwos, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labThrees, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreThrees, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labFours, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreFours, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labFives, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreFives, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labSixes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreSixes, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Lower"));

        labThreeKind.setAlignment(java.awt.Label.RIGHT);
        labThreeKind.setText("Three of a kind");

        labFourKind.setAlignment(java.awt.Label.RIGHT);
        labFourKind.setText("Four of a kind");

        labFullHouse.setAlignment(java.awt.Label.RIGHT);
        labFullHouse.setText("Full house");

        labSmallStraight.setAlignment(java.awt.Label.RIGHT);
        labSmallStraight.setText("Small straight");

        labLargeStraight.setAlignment(java.awt.Label.RIGHT);
        labLargeStraight.setText("Large straight");

        labYahtzee.setAlignment(java.awt.Label.RIGHT);
        labYahtzee.setText("Yahtzee");

        labChance.setAlignment(java.awt.Label.RIGHT);
        labChance.setText("Chance");

        btScoreThreeKind.setText("jButton13");
        btScoreThreeKind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreThreeKindActionPerformed(evt);
            }
        });

        btScoreFourKind.setText("jButton12");
        btScoreFourKind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreFourKindActionPerformed(evt);
            }
        });

        btScoreSmallStraight.setText("jButton10");
        btScoreSmallStraight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreSmallStraightActionPerformed(evt);
            }
        });

        btScoreLargeStraight.setText("jButton9");
        btScoreLargeStraight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreLargeStraightActionPerformed(evt);
            }
        });

        btScoreYahtzee.setText("jButton8");
        btScoreYahtzee.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreYahtzeeActionPerformed(evt);
            }
        });

        btScoreChance.setText("jButton7");
        btScoreChance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreChanceActionPerformed(evt);
            }
        });

        btScoreFullHouse.setText("jButton11");
        btScoreFullHouse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScoreFullHouseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labChance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labFourKind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labThreeKind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labFullHouse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labSmallStraight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labLargeStraight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labYahtzee, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btScoreChance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btScoreYahtzee, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btScoreFourKind, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                    .addComponent(btScoreFullHouse, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btScoreSmallStraight, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btScoreLargeStraight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btScoreThreeKind, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btScoreThreeKind)
                    .addComponent(labThreeKind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labFourKind, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreFourKind, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labFullHouse, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreFullHouse, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labSmallStraight, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreSmallStraight, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labLargeStraight, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreLargeStraight, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labYahtzee, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreYahtzee, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labChance, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btScoreChance, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        labLowerScore.setText("Lower");

        labUpperScore.setText("Upper");

        labBonusScore.setText("Bonus");

        labTotalScore.setText("Score");

        javax.swing.GroupLayout panScorecardLayout = new javax.swing.GroupLayout(panScorecard);
        panScorecard.setLayout(panScorecardLayout);
        panScorecardLayout.setHorizontalGroup(
            panScorecardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panScorecardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panScorecardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panScorecardLayout.createSequentialGroup()
                        .addGroup(panScorecardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(labBonusScore, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                            .addComponent(labUpperScore, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labLowerScore, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(83, 83, 83)
                        .addComponent(labTotalScore, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panScorecardLayout.setVerticalGroup(
            panScorecardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panScorecardLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labLowerScore)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labUpperScore)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panScorecardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labBonusScore)
                    .addComponent(labTotalScore))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panGame.setBorder(javax.swing.BorderFactory.createTitledBorder("Game"));

        labTurn.setText("Turn");

        btReroll.setText("Reroll");
        btReroll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRerollActionPerformed(evt);
            }
        });

        btRestart.setText("Restart");
        btRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRestartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panGameLayout = new javax.swing.GroupLayout(panGame);
        panGame.setLayout(panGameLayout);
        panGameLayout.setHorizontalGroup(
            panGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panGameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labTurn, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btRestart, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                    .addComponent(btReroll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panGameLayout.setVerticalGroup(
            panGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panGameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labTurn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btReroll))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btRestart)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        panHighScores.setBorder(javax.swing.BorderFactory.createTitledBorder("High Scores"));

        labHighscore1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labHighscore1.setText("Highscore 1");

        labHighscore2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labHighscore2.setText("Highscore 2");

        labHighscore3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labHighscore3.setText("Highscore 3");

        labHighscore4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labHighscore4.setText("Highscore 4");

        labHighscore5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labHighscore5.setText("Highscore 5");

        labHighscore6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labHighscore6.setText("Highscore 6");

        labHighscore7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labHighscore7.setText("Highscore 7");

        labHighscore8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labHighscore8.setText("Highscore 8");

        javax.swing.GroupLayout panHighScoresLayout = new javax.swing.GroupLayout(panHighScores);
        panHighScores.setLayout(panHighScoresLayout);
        panHighScoresLayout.setHorizontalGroup(
            panHighScoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panHighScoresLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panHighScoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(labHighscore1, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                    .addComponent(labHighscore2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighscore3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighscore4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighscore5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighscore6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighscore7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighscore8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        panHighScoresLayout.setVerticalGroup(
            panHighScoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panHighScoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labHighscore1)
                .addGap(18, 18, 18)
                .addComponent(labHighscore2)
                .addGap(18, 18, 18)
                .addComponent(labHighscore3)
                .addGap(18, 18, 18)
                .addComponent(labHighscore4)
                .addGap(18, 18, 18)
                .addComponent(labHighscore5)
                .addGap(18, 18, 18)
                .addComponent(labHighscore6)
                .addGap(18, 18, 18)
                .addComponent(labHighscore7)
                .addGap(18, 18, 18)
                .addComponent(labHighscore8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(panGame, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panDice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(panHighScores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(panScorecard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panScorecard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panDice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(panGame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panHighScores, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    /**
     * Reroll
     */
    private void btRerollActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRerollActionPerformed
        roll(false);
    }//GEN-LAST:event_btRerollActionPerformed

    
    
    /**
     * Resetting the game
     */
    private void btRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRestartActionPerformed
        // Reset disabled buttons
        btReroll.setEnabled(true);
        
        for (javax.swing.JButton button : scoreButtons)
            button.setEnabled(true);
        
        // Reset checkboxes
        for (java.awt.Checkbox box : diceCheckboxes)
            box.setState(false);
        
        // Reset globals, wipe UI, initiate first turn
        turn = 0;
        rerolls = REROLLS_ALLOWED;
        yahtzees = 0;
        grantedLowerBonus = false;
        scorecard = new YahtzeeScorecard();
        
        advanceTurn();
        
    }//GEN-LAST:event_btRestartActionPerformed

    
    
    /**
     * Category scoring buttons
     */
    private void btScoreAcesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreAcesActionPerformed
        scoreInCategory(0);
    }//GEN-LAST:event_btScoreAcesActionPerformed

    private void btScoreTwosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreTwosActionPerformed
        scoreInCategory(1);
    }//GEN-LAST:event_btScoreTwosActionPerformed

    private void btScoreThreesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreThreesActionPerformed
        scoreInCategory(2);
    }//GEN-LAST:event_btScoreThreesActionPerformed

    private void btScoreFoursActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreFoursActionPerformed
        scoreInCategory(3);
    }//GEN-LAST:event_btScoreFoursActionPerformed

    private void btScoreFivesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreFivesActionPerformed
        scoreInCategory(4);
    }//GEN-LAST:event_btScoreFivesActionPerformed

    private void btScoreSixesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreSixesActionPerformed
        scoreInCategory(5);
    }//GEN-LAST:event_btScoreSixesActionPerformed

    private void btScoreThreeKindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreThreeKindActionPerformed
        scoreInCategory(6);
    }//GEN-LAST:event_btScoreThreeKindActionPerformed

    private void btScoreFourKindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreFourKindActionPerformed
        scoreInCategory(7);
    }//GEN-LAST:event_btScoreFourKindActionPerformed

    private void btScoreFullHouseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreFullHouseActionPerformed
        scoreInCategory(8);
    }//GEN-LAST:event_btScoreFullHouseActionPerformed

    private void btScoreSmallStraightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreSmallStraightActionPerformed
        scoreInCategory(9);
    }//GEN-LAST:event_btScoreSmallStraightActionPerformed

    private void btScoreLargeStraightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreLargeStraightActionPerformed
        scoreInCategory(10);
    }//GEN-LAST:event_btScoreLargeStraightActionPerformed

    private void btScoreYahtzeeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreYahtzeeActionPerformed
        scoreInCategory(11);
    }//GEN-LAST:event_btScoreYahtzeeActionPerformed

    private void btScoreChanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScoreChanceActionPerformed
        scoreInCategory(12);
    }//GEN-LAST:event_btScoreChanceActionPerformed
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Checkbox btDiceKeep1;
    private java.awt.Checkbox btDiceKeep2;
    private java.awt.Checkbox btDiceKeep3;
    private java.awt.Checkbox btDiceKeep4;
    private java.awt.Checkbox btDiceKeep5;
    private javax.swing.JButton btReroll;
    private javax.swing.JButton btRestart;
    private javax.swing.JButton btScoreAces;
    private javax.swing.JButton btScoreChance;
    private javax.swing.JButton btScoreFives;
    private javax.swing.JButton btScoreFourKind;
    private javax.swing.JButton btScoreFours;
    private javax.swing.JButton btScoreFullHouse;
    private javax.swing.JButton btScoreLargeStraight;
    private javax.swing.JButton btScoreSixes;
    private javax.swing.JButton btScoreSmallStraight;
    private javax.swing.JButton btScoreThreeKind;
    private javax.swing.JButton btScoreThrees;
    private javax.swing.JButton btScoreTwos;
    private javax.swing.JButton btScoreYahtzee;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private java.awt.Label labAces;
    private javax.swing.JLabel labBonusScore;
    private java.awt.Label labChance;
    private javax.swing.JLabel labDice1;
    private javax.swing.JLabel labDice2;
    private javax.swing.JLabel labDice3;
    private javax.swing.JLabel labDice4;
    private javax.swing.JLabel labDice5;
    private javax.swing.JLabel labDiceIcon1;
    private javax.swing.JLabel labDiceIcon2;
    private javax.swing.JLabel labDiceIcon3;
    private javax.swing.JLabel labDiceIcon4;
    private javax.swing.JLabel labDiceIcon5;
    private java.awt.Label labFives;
    private java.awt.Label labFourKind;
    private java.awt.Label labFours;
    private java.awt.Label labFullHouse;
    private javax.swing.JLabel labHighscore1;
    private javax.swing.JLabel labHighscore2;
    private javax.swing.JLabel labHighscore3;
    private javax.swing.JLabel labHighscore4;
    private javax.swing.JLabel labHighscore5;
    private javax.swing.JLabel labHighscore6;
    private javax.swing.JLabel labHighscore7;
    private javax.swing.JLabel labHighscore8;
    private java.awt.Label labLargeStraight;
    private javax.swing.JLabel labLowerScore;
    private java.awt.Label labSixes;
    private java.awt.Label labSmallStraight;
    private java.awt.Label labThreeKind;
    private java.awt.Label labThrees;
    private javax.swing.JLabel labTotalScore;
    private java.awt.Label labTurn;
    private java.awt.Label labTwos;
    private javax.swing.JLabel labUpperScore;
    private java.awt.Label labYahtzee;
    private javax.swing.JPanel panDice;
    private javax.swing.JPanel panGame;
    private javax.swing.JPanel panHighScores;
    private javax.swing.JPanel panScorecard;
    // End of variables declaration//GEN-END:variables

    
}
