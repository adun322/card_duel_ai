package fun.adun.pokerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LevelChoiceActivity extends AppCompatActivity {

    private Button btnLevel3;
    private Button btnQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_choice);

        btnLevel3 = findViewById(R.id.level3);
        btnQuiz = findViewById(R.id.quiz);

        btnLevel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LevelChoiceActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        btnQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LevelChoiceActivity.this, QuizActivity.class);
                startActivity(intent);
            }
        });

    }
}
