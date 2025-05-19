package fun.adun.pokerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnRegister, btnLogin;
    private TextView tvStatus;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация элементов интерфейса
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        tvStatus = findViewById(R.id.tvStatus);

        // Инициализация базы данных
        databaseHelper = new DatabaseHelper(this);

        // Обработка нажатия на кнопку "Зарегистрироваться"
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isInserted = databaseHelper.addUser(username, password);
                    if (isInserted) {
                        tvStatus.setText("Регистрация успешна!");
                        Toast.makeText(MainActivity.this, "Пользователь зарегистрирован", Toast.LENGTH_SHORT).show();
                    } else {
                        tvStatus.setText("Ошибка регистрации");
                        Toast.makeText(MainActivity.this, "Ошибка при регистрации", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Обработка нажатия на кнопку "Войти"
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                boolean isUserValid = databaseHelper.checkUser(username, password);
                if (isUserValid) {
                    tvStatus.setText("Вход выполнен!");
                    Toast.makeText(MainActivity.this, "Добро пожаловать!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LevelChoiceActivity.class);
                    startActivity(intent);
                } else {
                    tvStatus.setText("Ошибка входа");
                    Toast.makeText(MainActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}