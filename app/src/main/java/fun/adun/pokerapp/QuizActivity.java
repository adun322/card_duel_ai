package fun.adun.pokerapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QuizActivity extends AppCompatActivity {

    private TextView questionText;
    private RadioGroup answersGroup;
    private Button submitButton;
    private TextView resultText;
    private TextView timerText;
    private TextView statsText;

    private List<Question> questions;
    private Question currentQuestion;
    private int correctAnswers = 0;
    private int totalQuestionsAttempted = 0;
    private long lastAttemptTime = 0;
    private static final long COOLDOWN_PERIOD = TimeUnit.HOURS.toMillis(1); // 1 час

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initViews();
        loadStats();
        initQuestions();
        showNewQuestion();
        updateTimer();
    }

    private void initViews() {
        questionText = findViewById(R.id.questionText);
        answersGroup = findViewById(R.id.answersGroup);
        submitButton = findViewById(R.id.submitButton);
        resultText = findViewById(R.id.resultText);
        timerText = findViewById(R.id.timerText);
        statsText = findViewById(R.id.statsText);

        submitButton.setOnClickListener(v -> checkAnswer());
    }

    private void initQuestions() {
        questions = new ArrayList<>();

        // Добавляем вопросы (вопрос, правильный ответ, варианты)
        questions.add(new Question(
                "Какая вероятность выпадения каре?",
                "0.024%",
                new String[]{"0.5%", "1.2%", "5%"}
        ));

        questions.add(new Question(
                "Какова вероятность получить флеш?",
                "0.197%",
                new String[]{"1.5%", "3.2%", "0.5%"}
        ));

        questions.add(new Question(
                "Сколько существует различных комбинаций карт в покере?",
                "2,598,960",
                new String[]{"100,000", "1,000,000", "5,000,000"}
        ));

        Collections.shuffle(questions);
    }

    private void showNewQuestion() {
        if (questions.isEmpty()) {
            questionText.setText("Все вопросы пройдены!");
            answersGroup.setVisibility(View.GONE);
            submitButton.setEnabled(false);
            return;
        }

        currentQuestion = questions.remove(0);
        questionText.setText(currentQuestion.getQuestion());

        List<String> answers = new ArrayList<>();
        answers.add(currentQuestion.getCorrectAnswer());
        for (String wrongAnswer : currentQuestion.getWrongAnswers()) {
            answers.add(wrongAnswer);
        }
        Collections.shuffle(answers);

        for (int i = 0; i < answersGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) answersGroup.getChildAt(i);
            radioButton.setText(answers.get(i));
        }

        answersGroup.clearCheck();
        resultText.setVisibility(View.GONE);
    }

    private void checkAnswer() {
        int selectedId = answersGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Выберите ответ", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        boolean isCorrect = selectedRadioButton.getText().equals(currentQuestion.getCorrectAnswer());

        totalQuestionsAttempted++;
        if (isCorrect) {
            correctAnswers++;
            resultText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            resultText.setText("Правильно!");
        } else {
            resultText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            resultText.setText("Неверно! Правильный ответ: " + currentQuestion.getCorrectAnswer());
        }

        resultText.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);
        lastAttemptTime = System.currentTimeMillis();
        saveStats();

        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                showNewQuestion();
                submitButton.setEnabled(canAnswer());
                updateStats();
            }
        }.start();
    }

    private boolean canAnswer() {
        long timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime;
        return timeSinceLastAttempt > COOLDOWN_PERIOD;
    }

    private void updateTimer() {
        new CountDownTimer(Long.MAX_VALUE, 1000) {
            public void onTick(long millisUntilFinished) {
                long timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime;
                long remainingTime = COOLDOWN_PERIOD - timeSinceLastAttempt;

                if (remainingTime > 0) {
                    String time = String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(remainingTime),
                            TimeUnit.MILLISECONDS.toMinutes(remainingTime) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(remainingTime) % TimeUnit.MINUTES.toSeconds(1));
                    timerText.setText("Следующий вопрос через: " + time);
                    submitButton.setEnabled(false);
                } else {
                    timerText.setText("Можно отвечать!");
                    submitButton.setEnabled(true);
                }
            }
            public void onFinish() {}
        }.start();
    }

    private void updateStats() {
        String stats = String.format("Правильных ответов: %d/%d (%.1f%%)",
                correctAnswers,
                totalQuestionsAttempted,
                totalQuestionsAttempted > 0 ? (correctAnswers * 100f / totalQuestionsAttempted) : 0);
        statsText.setText(stats);
    }

    private void saveStats() {
        SharedPreferences prefs = getSharedPreferences("QuizStats", MODE_PRIVATE);
        prefs.edit()
                .putInt("correctAnswers", correctAnswers)
                .putInt("totalQuestionsAttempted", totalQuestionsAttempted)
                .putLong("lastAttemptTime", lastAttemptTime)
                .apply();
    }

    private void loadStats() {
        SharedPreferences prefs = getSharedPreferences("QuizStats", MODE_PRIVATE);
        correctAnswers = prefs.getInt("correctAnswers", 0);
        totalQuestionsAttempted = prefs.getInt("totalQuestionsAttempted", 0);
        lastAttemptTime = prefs.getLong("lastAttemptTime", 0);
        updateStats();
    }

    private static class Question {
        private final String question;
        private final String correctAnswer;
        private final String[] wrongAnswers;

        public Question(String question, String correctAnswer, String[] wrongAnswers) {
            this.question = question;
            this.correctAnswer = correctAnswer;
            this.wrongAnswers = wrongAnswers;
        }

        public String getQuestion() { return question; }
        public String getCorrectAnswer() { return correctAnswer; }
        public String[] getWrongAnswers() { return wrongAnswers; }
    }
}