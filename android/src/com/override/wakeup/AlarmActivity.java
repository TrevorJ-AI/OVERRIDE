package com.override.wakeup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class AlarmActivity extends Activity {

    private TextView problemText;
    private EditText answerInput;
    private TextView feedbackText;
    private int expectedAnswer;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_alarm);

        problemText = (TextView) findViewById(R.id.problemText);
        answerInput = (EditText) findViewById(R.id.answerInput);
        feedbackText = (TextView) findViewById(R.id.feedbackText);
        Button submitButton = (Button) findViewById(R.id.submitButton);

        newProblem();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });
    }

    private void newProblem() {
        int a = 10 + random.nextInt(90);
        int b = 10 + random.nextInt(90);
        int op = random.nextInt(3);
        String opSymbol;
        switch (op) {
            case 0:
                expectedAnswer = a + b;
                opSymbol = "+";
                break;
            case 1:
                expectedAnswer = a - b;
                opSymbol = "-";
                break;
            default:
                // Keep multiplication friendly for half-asleep brains.
                a = 2 + random.nextInt(11);
                b = 2 + random.nextInt(11);
                expectedAnswer = a * b;
                opSymbol = "x";
                break;
        }
        problemText.setText(a + " " + opSymbol + " " + b + " = ?");
        answerInput.setText("");
    }

    private void checkAnswer() {
        String input = answerInput.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            return;
        }
        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            feedbackText.setText("Numbers only.");
            return;
        }
        if (value == expectedAnswer) {
            Intent stopIntent = new Intent(this, AlarmService.class);
            stopIntent.setAction(AlarmService.ACTION_STOP);
            startService(stopIntent);
            finish();
        } else {
            feedbackText.setText("Wrong. Try again.");
            newProblem();
        }
    }

    @Override
    public void onBackPressed() {
        // Solving the math is the only way out.
    }
}
