package com.example.wordquizgame;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class GameActivity extends ActionBarActivity {

    private static final String TAG = "GameActivity";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
