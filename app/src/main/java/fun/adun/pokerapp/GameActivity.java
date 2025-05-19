package fun.adun.pokerapp;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    // UI элементы
    private LinearLayout computerCards;
    private LinearLayout playerCards;
    private Button foldButton;
    private Button callButton;
    private Button resetStatsButton;
    private TextView resultText;
    private TextView statsText;

    // Игровые данные
    private List<Integer> deck;
    private List<Integer> computerHand;
    private List<Integer> playerHand;

    // Статистика
    private int totalGames = 0;
    private int playerFolds = 0;
    private int correctMoves = 0;
    private int totalMoves = 0;
    private boolean isComputerTurnFirst = true;

    // Константы
    private final Random random = new Random();
    private final Handler handler = new Handler();
    private final int CARD_WIDTH_DP = 72;
    private final int CARD_HEIGHT_DP = 100;
    private final int CARD_MARGIN_DP = 2;
    private final int ROUND_DELAY_MS = 5000;

    // Константы комбинаций
    private static final int HIGH_CARD = 1;
    private static final int PAIR = 2;
    private static final int TWO_PAIRS = 3;
    private static final int THREE_OF_A_KIND = 4;
    private static final int STRAIGHT = 5;
    private static final int FLUSH = 6;
    private static final int FULL_HOUSE = 7;
    private static final int FOUR_OF_A_KIND = 8;
    private static final int STRAIGHT_FLUSH = 9;
    private static final int ROYAL_FLUSH = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initViews();
        loadStats();
        startNewRound();
    }

    private void initViews() {
        computerCards = findViewById(R.id.computerCards);
        playerCards = findViewById(R.id.playerCards);
        foldButton = findViewById(R.id.foldButton);
        callButton = findViewById(R.id.callButton);
        resetStatsButton = findViewById(R.id.resetStatsButton);
        resultText = findViewById(R.id.resultText);
        statsText = findViewById(R.id.statsText);

        foldButton.setOnClickListener(v -> playerFolds());
        callButton.setOnClickListener(v -> playerCalls());
        resetStatsButton.setOnClickListener(v -> resetStats());
    }

    private void startNewRound() {
        computerCards.removeAllViews();
        playerCards.removeAllViews();

        initializeDeck();
        Collections.shuffle(deck);

        computerHand = new ArrayList<>();
        playerHand = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            computerHand.add(deck.remove(0));
            playerHand.add(deck.remove(0));
        }

        showCards(false);

        if (isComputerTurnFirst) {
            computerDecision();
        } else {
            resultText.setText("Ваш ход! Вы ходите первым");
            enableButtons();
        }
        isComputerTurnFirst = !isComputerTurnFirst;
    }

    private void initializeDeck() {
        deck = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            deck.add(i);
        }
    }

    private void showCards(boolean showComputerCards) {
        computerCards.removeAllViews();
        playerCards.removeAllViews();

        for (int cardId : computerHand) {
            addCardToLayout(computerCards, cardId, showComputerCards);
        }

        for (int cardId : playerHand) {
            addCardToLayout(playerCards, cardId, true);
        }
    }

    private void addCardToLayout(LinearLayout layout, int cardId, boolean showFront) {
        ImageView card = new ImageView(this);
        card.setImageResource(showFront ? getCardResource(cardId) : R.drawable.card_back);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(CARD_WIDTH_DP),
                dpToPx(CARD_HEIGHT_DP)
        );
        params.setMargins(dpToPx(CARD_MARGIN_DP), 0, 0, 0);
        card.setLayoutParams(params);
        layout.addView(card);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private int getCardResource(int cardId) {
        String suit;
        String rank;

        switch (cardId / 13) {
            case 0: suit = "h"; break;
            case 1: suit = "d"; break;
            case 2: suit = "c"; break;
            case 3: suit = "s"; break;
            default: return R.drawable.card_back;
        }

        int rankValue = (cardId % 13) + 1;
        switch (rankValue) {
            case 1: rank = "a"; break;
            case 11: rank = "j"; break;
            case 12: rank = "q"; break;
            case 13: rank = "k"; break;
            default: rank = String.valueOf(rankValue);
        }

        String resourceName = suit + rank;
        return getResources().getIdentifier(resourceName, "drawable", getPackageName());
    }

    private void computerDecision() {
        disableButtons();
        resultText.setText("Компьютер думает...");

        handler.postDelayed(() -> {
            int handStrength = evaluateHand(computerHand);
            boolean willPlay = shouldComputerPlay(handStrength);

            if (willPlay) {
                resultText.setText("Компьютер играет. Ваш ход!");
                enableButtons();
            } else {
                totalGames++;
                showCards(true);
                resultText.setText("Компьютер сбросил карты.");
                updateStats();
                handler.postDelayed(this::startNewRound, ROUND_DELAY_MS);
            }
        }, 1500);
    }



    private boolean shouldComputerPlay(int handStrength) {
        switch (handStrength) {
            case ROYAL_FLUSH:
            case STRAIGHT_FLUSH:
            case FOUR_OF_A_KIND:
            case FULL_HOUSE:
            case FLUSH:
            case STRAIGHT:
                return true;
            case THREE_OF_A_KIND: return random.nextFloat() < 0.9;
            case TWO_PAIRS: return random.nextFloat() < 0.8;
            case PAIR: return random.nextFloat() < 0.6;
            default: return random.nextFloat() < 0.2;
        }
    }

    private int evaluateHand(List<Integer> hand) {
        if (isRoyalFlush(hand)) return ROYAL_FLUSH;
        if (isStraightFlush(hand)) return STRAIGHT_FLUSH;
        if (isFourOfAKind(hand)) return FOUR_OF_A_KIND;
        if (isFullHouse(hand)) return FULL_HOUSE;
        if (isFlush(hand)) return FLUSH;
        if (isStraight(hand)) return STRAIGHT;
        if (isThreeOfAKind(hand)) return THREE_OF_A_KIND;
        if (isTwoPairs(hand)) return TWO_PAIRS;
        if (isPair(hand)) return PAIR;
        return HIGH_CARD;
    }

    private boolean isRoyalFlush(List<Integer> hand) {
        if (!isFlush(hand)) return false;
        List<Integer> ranks = getSortedRanks(hand);
        return ranks.contains(0) && ranks.contains(9) && ranks.contains(10)
                && ranks.contains(11) && ranks.contains(12);
    }

    private boolean isStraightFlush(List<Integer> hand) {
        return isFlush(hand) && isStraight(hand);
    }

    private boolean isFourOfAKind(List<Integer> hand) {
        int[] ranks = new int[13];
        for (int card : hand) {
            ranks[card % 13]++;
            if (ranks[card % 13] == 4) return true;
        }
        return false;
    }

    private boolean isFullHouse(List<Integer> hand) {
        int[] ranks = new int[13];
        boolean hasThree = false;
        boolean hasTwo = false;

        for (int card : hand) {
            ranks[card % 13]++;
        }

        for (int count : ranks) {
            if (count == 3) hasThree = true;
            if (count == 2) hasTwo = true;
        }

        return hasThree && hasTwo;
    }

    private boolean isFlush(List<Integer> hand) {
        int suit = hand.get(0) / 13;
        for (int card : hand) {
            if (card / 13 != suit) return false;
        }
        return true;
    }

    private boolean isStraight(List<Integer> hand) {
        List<Integer> ranks = getSortedRanks(hand);

        for (int i = 0; i < ranks.size() - 1; i++) {
            if (ranks.get(i + 1) - ranks.get(i) != 1) {
                if (ranks.contains(0) && ranks.contains(1) && ranks.contains(2)
                        && ranks.contains(3) && ranks.contains(12)) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    private boolean isThreeOfAKind(List<Integer> hand) {
        int[] ranks = new int[13];
        for (int card : hand) {
            ranks[card % 13]++;
            if (ranks[card % 13] == 3) return true;
        }
        return false;
    }

    private boolean isTwoPairs(List<Integer> hand) {
        int pairs = 0;
        int[] ranks = new int[13];

        for (int card : hand) {
            ranks[card % 13]++;
        }

        for (int count : ranks) {
            if (count == 2) pairs++;
        }

        return pairs >= 2;
    }

    private boolean isPair(List<Integer> hand) {
        int[] ranks = new int[13];
        for (int card : hand) {
            ranks[card % 13]++;
            if (ranks[card % 13] == 2) return true;
        }
        return false;
    }

    private List<Integer> getSortedRanks(List<Integer> hand) {
        List<Integer> ranks = new ArrayList<>();
        for (int card : hand) {
            ranks.add(card % 13);
        }
        Collections.sort(ranks);
        return ranks;
    }

    private void playerCalls() {
        totalMoves++;
        disableButtons();
        showCards(true);

        int computerScore = evaluateHand(computerHand);
        int playerScore = evaluateHand(playerHand);

        boolean isCorrectMove = (playerScore > computerScore) ||
                (playerScore == computerScore && compareKickers(playerHand, computerHand) > 0);

        if (isCorrectMove) {
            correctMoves++;
        }

        totalGames++;

        String computerCombination = getCombinationName(computerScore);
        String playerCombination = getCombinationName(playerScore);

        if (computerScore > playerScore) {
            resultText.setText(String.format("Компьютер побеждает! (%s против %s)",
                    computerCombination, playerCombination));
        } else if (playerScore > computerScore) {
            resultText.setText(String.format("Вы победили! (%s против %s)",
                    playerCombination, computerCombination));
        } else {
            if (compareKickers(playerHand, computerHand) > 0) {
                resultText.setText(String.format("Вы победили! (%s против %s)",
                        playerCombination, computerCombination));
            } else if (compareKickers(playerHand, computerHand) == 0){
                resultText.setText(String.format("Ничья! (%s против %s)",
                        playerCombination, computerCombination));
            } else {
                resultText.setText(String.format("Компьютер побеждает! (%s против %s)",
                        playerCombination, computerCombination));
            }
        }

        updateStats();
        handler.postDelayed(this::startNewRound, ROUND_DELAY_MS);
    }

    private int compareKickers(List<Integer> hand1, List<Integer> hand2) {
        List<Integer> ranks1 = getSortedRanks(hand1);
        List<Integer> ranks2 = getSortedRanks(hand2);

        for (int i = ranks1.size() - 1; i >= 0; i--) {
            int diff = ranks1.get(i) - ranks2.get(i);
            if (diff != 0) return diff;
        }
        return 0;
    }

    private void playerFolds() {
        totalMoves++;
        disableButtons();
        showCards(true);

        int computerScore = evaluateHand(computerHand);
        int playerScore = evaluateHand(playerHand);

        boolean isCorrectFold = (playerScore < computerScore) ||
                (playerScore == computerScore && compareKickers(playerHand, computerHand) < 0);

        if (isCorrectFold) {
            correctMoves++;
        }

        totalGames++;
        playerFolds++;
        resultText.setText("Вы сбросили карты. Компьютер побеждает!");
        updateStats();
        handler.postDelayed(this::startNewRound, ROUND_DELAY_MS);
    }

    private String getCombinationName(int combination) {
        switch (combination) {
            case ROYAL_FLUSH: return "Роял-флэш";
            case STRAIGHT_FLUSH: return "Стрит-флэш";
            case FOUR_OF_A_KIND: return "Каре";
            case FULL_HOUSE: return "Фулл-хаус";
            case FLUSH: return "Флэш";
            case STRAIGHT: return "Стрит";
            case THREE_OF_A_KIND: return "Тройка";
            case TWO_PAIRS: return "Две пары";
            case PAIR: return "Пара";
            default: return "Старшая карта";
        }
    }

    private void updateStats() {
        float accuracyRate = totalMoves == 0 ? 0 : (correctMoves * 100f / totalMoves);

        String stats = String.format(
                "Всего игр: %d\n" +
                        "Сбросов игрока: %d\n" +
                        "Точность: %.1f%%",
                totalGames,
                playerFolds,
                accuracyRate
        );

        statsText.setText(stats);
        saveStats();
    }

    private void resetStats() {
        totalGames = 0;
        playerFolds = 0;
        correctMoves = 0;
        totalMoves = 0;
        isComputerTurnFirst = true;
        updateStats();
        Toast.makeText(this, "Статистика сброшена", Toast.LENGTH_SHORT).show();
    }

    private void saveStats() {
        SharedPreferences prefs = getSharedPreferences("PokerStats", MODE_PRIVATE);
        prefs.edit()
                .putInt("totalGames", totalGames)
                .putInt("playerFolds", playerFolds)
                .putInt("correctMoves", correctMoves)
                .putInt("totalMoves", totalMoves)
                .putBoolean("isComputerTurnFirst", isComputerTurnFirst)
                .apply();
    }

    private void loadStats() {
        SharedPreferences prefs = getSharedPreferences("PokerStats", MODE_PRIVATE);
        totalGames = prefs.getInt("totalGames", 0);
        playerFolds = prefs.getInt("playerFolds", 0);
        correctMoves = prefs.getInt("correctMoves", 0);
        totalMoves = prefs.getInt("totalMoves", 0);
        isComputerTurnFirst = prefs.getBoolean("isComputerTurnFirst", true);
    }

    private void enableButtons() {
        foldButton.setEnabled(true);
        callButton.setEnabled(true);
    }

    private void disableButtons() {
        foldButton.setEnabled(false);
        callButton.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveStats();
    }
}