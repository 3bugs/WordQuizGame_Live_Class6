package com.example.wordquizgame;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.wordquizgame.db.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;


public class GameActivity extends ActionBarActivity {

    private static final String TAG = "GameActivity";
    private static final int NUMBER_OF_QUESTIONS = 5;

    private int difficulty;
    private int numChoices;

    private ArrayList<String> fileNameList;
    private ArrayList<String> quizWordList;
    private ArrayList<String> choiceWords;

    private String answerFileName;
    private int totalGuesses;
    private int score;

    private Random random;
    private Handler handler;

    private TextView questionNumberTextView;
    private ImageView questionImageView;
    private TableLayout buttonTableLayout;
    private TextView answerTextView;

    private Animation shakeAnimation;

    private static final String SCORE_KEY = "score";
    private Bundle oldState;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SCORE_KEY, score);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        oldState = savedInstanceState;

        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        shakeAnimation.setRepeatCount(3);

        questionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
        questionImageView = (ImageView) findViewById(R.id.questionImageView);
        buttonTableLayout = (TableLayout) findViewById(R.id.buttonTableLayout);
        answerTextView = (TextView) findViewById(R.id.answerTextView);

        Intent intent = getIntent();
        difficulty = intent.getIntExtra(MainActivity.DIFF_KEY, 0);

        switch (difficulty) {
            case 0:
                numChoices = 2;
                break;
            case 1:
                numChoices = 4;
                break;
            case 2:
                numChoices = 6;
                break;
        }

        fileNameList = new ArrayList<String>();
        quizWordList = new ArrayList<String>();
        choiceWords = new ArrayList<String>();

        random = new Random();
        handler = new Handler();

        getImageFileName();
    }

    private void getImageFileName() {
        String[] categories = new String[] {
                "animals", "body", "colors", "numbers", "objects"
        };

        AssetManager assets = getAssets();

        for (String category : categories) {
            try {
                String[] fileNames = assets.list(category);

                for (String fileName : fileNames) {
                    fileNameList.add(fileName.replace(".png", ""));
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error listing filenames in " + category);
            }
        }

        Log.i(TAG, "***** รายชื่อไฟล์ทั้งหมด *****");
        for (String fileName: fileNameList) {
            Log.i(TAG, fileName);
        }

        startQuiz();
    }

    private void startQuiz() {
        totalGuesses = 0;

        if (oldState != null) {
            score = oldState.getInt(SCORE_KEY);
        } else {
            score = 0;
        }

        quizWordList.clear();

        while (quizWordList.size() < NUMBER_OF_QUESTIONS) {
            int randomIndex = random.nextInt(fileNameList.size());
            String fileName = fileNameList.get(randomIndex);

            if (quizWordList.contains(fileName) == false) {
                quizWordList.add(fileName);
            }
        }

        Log.i(TAG, "***** ชื่อไฟล์สำหรับตั้งคำถามที่สุ่มได้ *****");
        for (String fileName : quizWordList) {
            Log.i(TAG, fileName);
        }

        loadNextQuestion();
    }

    private void loadNextQuestion() {
        answerTextView.setText(null);

        answerFileName = quizWordList.remove(0);

        String msg = String.format("คำถามข้อที่ %d จากทั้งหมด %d ข้อ", score + 1,
                NUMBER_OF_QUESTIONS);
        questionNumberTextView.setText(msg);

        loadQuestionImage();
        prepareChoiceWords();
    }

    private void loadQuestionImage() {
        String category = answerFileName.substring(0, answerFileName.indexOf('-'));
        String filePath = category + "/" + answerFileName + ".png";

        AssetManager assets = getAssets();
        InputStream stream;

        try {
            stream = assets.open(filePath);

            Drawable image = Drawable.createFromStream(stream, filePath);
            questionImageView.setImageDrawable(image);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading file: " + filePath);
        }

    }

    private void prepareChoiceWords() {
        choiceWords.clear();

        while (choiceWords.size() < numChoices) {
            int randomIndex = random.nextInt(fileNameList.size());
            String randomWord = getWord(fileNameList.get(randomIndex));

            if (choiceWords.contains(randomWord) == false
                    && randomWord.equals(getWord(answerFileName)) == false) {
                choiceWords.add(randomWord);
            }
        }

        int randomIndex = random.nextInt(choiceWords.size());
        choiceWords.set(randomIndex, getWord(answerFileName));

        Log.i(TAG, "***** คำศัพท์ตัวเลือกที่สุ่มได้ *****");
        for (String word : choiceWords) {
            Log.i(TAG, word);
        }

        createChoiceButtons();
    }

    private void createChoiceButtons() {
        for (int row = 0; row < buttonTableLayout.getChildCount(); row++) {
            TableRow tr = (TableRow) buttonTableLayout.getChildAt(row);
            tr.removeAllViews();
        }

        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int row = 0; row < numChoices / 2; row++) {
            TableRow tr = (TableRow) buttonTableLayout.getChildAt(row);

            for (int column = 0; column < 2; column++) {
                Button guessButton = (Button) inflater.inflate(R.layout.guess_button, tr, false);

                guessButton.setText(choiceWords.get((row * 2) + column));
                guessButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submitGuess((Button) v);
                    }
                });

                tr.addView(guessButton);
            }
        }
    }

    private MediaPlayer mp;

    private void submitGuess(Button button) {
        String guessWord = button.getText().toString();
        String answerWord = getWord(answerFileName);

        totalGuesses++;

        // ตอบถูก
        if (guessWord.equals(answerWord)) {
            mp = MediaPlayer.create(this, R.raw.applause);
            mp.setVolume(0.5f, 0.5f);
            mp.start();

            score++;

            answerTextView.setText(answerWord + " ถูกต้องนะครับ");
            answerTextView.setTextColor(
                    getResources().getColor(android.R.color.holo_green_dark)
            );

            disableAllButtons();

            // เล่นครบทุกข้อแล้ว (จบเกม)
            if (score == NUMBER_OF_QUESTIONS) {

                saveScore();

                String msg = String.format(
                        "จำนวนครั้งที่ทาย: %d\nเปอร์เซ็นต์ความถูกต้อง: %.1f",
                        totalGuesses,
                        (100 * NUMBER_OF_QUESTIONS) / (double) totalGuesses
                );

                new AlertDialog.Builder(this)
                        .setTitle("สรุปผล")
                        .setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("เริ่มเกมใหม่", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startQuiz();
                            }
                        })
                        .setNegativeButton("กลับหน้าหลัก", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
            // ยังเล่นไม่ครบทุกข้อ
            else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mp.stop();
                        loadNextQuestion();
                    }
                }, 2000);
            }
        }
        // ตอบผิด
        else {
            questionImageView.setAnimation(shakeAnimation);
            button.setAnimation(shakeAnimation);

            mp = MediaPlayer.create(this, R.raw.fail3);
            mp.start();

            button.setEnabled(false);

            answerTextView.setText("ผิดครับ ลองใหม่นะครับ");
            answerTextView.setTextColor(
                    getResources().getColor(android.R.color.holo_red_dark)
            );
        }
    }

    private void saveScore() {
        double score = (100 * NUMBER_OF_QUESTIONS) / (double) totalGuesses;

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_SCORE, score);
        cv.put(DatabaseHelper.COL_DIFFICULTY, difficulty);

        long result = db.insert(DatabaseHelper.TABLE_NAME, null, cv);
        if (result == -1) {
            Log.e(TAG, "Error inserting data into database");
        }

    }

    private void disableAllButtons() {
        for (int row = 0; row < buttonTableLayout.getChildCount(); row++) {
            TableRow tr = (TableRow) buttonTableLayout.getChildAt(row);

            for (int column = 0; column < tr.getChildCount(); column++) {
                Button button = (Button) tr.getChildAt(column);
                button.setEnabled(false);
            }
        }
    }

    private String getWord(String fileName) {
        return fileName.substring(fileName.indexOf('-') + 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        Music.play(this, R.raw.game);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        Music.stop();
    }

}




