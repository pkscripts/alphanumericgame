package com.microappservice.alphabet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    ImageButton btn_find;
    ImageButton btn_status;
    TextView lbl_highscore;
    TextView lbl_score;
    TextView lbl_char;

    TextToSpeech t1;
    static MediaPlayer mp;

    static SharedPreferences sharedPref;
    public static int i_high_score = 0;
    int score = 0;
    boolean is_wrong = false;
    boolean is_puzzle = false;
    static String expected_str = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t1=null;
        // get or create SharedPreferences
        sharedPref = getSharedPreferences("alphadata", MODE_PRIVATE);
        i_high_score = sharedPref.getInt("high_score", 0);

        /*
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if(year>2024)
        {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        */

        btn_status = findViewById(R.id.btn_status);

        btn_find = findViewById(R.id.btn_find);
        btn_find.setVisibility(View.INVISIBLE);
        lbl_char  = findViewById(R.id.lbl_char);
        lbl_char.setTextColor(Color.GRAY);

        lbl_highscore = findViewById(R.id.lbl_highscore);
        lbl_highscore.setText("High score: " + Integer.toString(i_high_score));

        lbl_score = findViewById(R.id.lbl_score);
        lbl_score.setTextColor(Color.GREEN);

        lbl_highscore.setVisibility(View.INVISIBLE);
        lbl_score.setVisibility(View.INVISIBLE);

        init_tts();
        /*
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        */
        btn_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_puzzle = !is_puzzle;
                lbl_char.setText("");
                if(is_puzzle) {
                    btn_status.setBackgroundResource(R.drawable.ic_puzzle);
                    btn_find.setVisibility(View.VISIBLE);
                    lbl_highscore.setVisibility(View.VISIBLE);
                    lbl_score.setVisibility(View.VISIBLE);
                    find();
                    //t1.speak("Puzzle mode on", TextToSpeech.QUEUE_FLUSH, null);
                }
                else {
                    expected_str = "";
                    btn_status.setBackgroundResource(R.drawable.ic_speak);
                    btn_find.setVisibility(View.INVISIBLE);
                    lbl_highscore.setVisibility(View.INVISIBLE);
                    lbl_score.setVisibility(View.INVISIBLE);
                    resetbuttons();
                    //t1.speak("Speaker mode on", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find();
            }
        });
        resetbuttons();
    }

    private  void init_tts(){
        if(t1==null) {
            t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        t1.setLanguage(Locale.UK);
                    }
                }
            });
        }
    }

    public void find()
    {
        if(is_puzzle) {
            resetbuttons();
            Random random = new Random();
            char randomizedCharacter = (char) (random.nextInt(26) + 'a');
            expected_str = Character.toString(randomizedCharacter).toUpperCase();
            lbl_char.setText(expected_str);
            String audio_msg = "Find. " + expected_str;
            //t1.speak("Find. " + expected_str, TextToSpeech.QUEUE_FLUSH, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t1.speak(audio_msg,TextToSpeech.QUEUE_FLUSH,null,null);
            } else {
                t1.speak(audio_msg, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public void playcorrect()
    {
        try {
                mp = MediaPlayer.create(this, R.raw.correct);
                mp.setLooping(false);
                mp.start();
                //mp.stop();
        }catch (Exception e){
            lbl_highscore.setText(e.getMessage());
        }
    }

    public void playwrong()
    {
        try{
            mp = MediaPlayer.create(this, R.raw.wrong);
            mp.setLooping(false);
            mp.start();
        //mp.stop();
        }catch (Exception e){
            lbl_highscore.setText(e.getMessage());
        }
    }

    public void resetbuttons()
    {
        try {
            ArrayList<View> allButtons;
            allButtons = ((ConstraintLayout) findViewById(R.id.layout_id)).getTouchables();
            for (View view: allButtons)
            {
                Button btn = ((Button) view);
                btn.setTextColor(Color.MAGENTA);
                btn.setBackgroundColor(Color.LTGRAY);
            }
            //t1.speak("Count. " + Integer.toString(allButtons.size()), TextToSpeech.QUEUE_FLUSH, null);
        }catch (Exception e){
            //t1.speak("Error", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void onAlphaClicked(View view) {
        String actual = ((Button) view).getText().toString();
        if(is_puzzle)
        {
            if(!expected_str.matches("")) {
                if (actual.contains(expected_str)) {
                    //t1.speak("Correct answer.", TextToSpeech.QUEUE_FLUSH, null);
                    playcorrect();
                    ((Button) view).setTextColor(Color.WHITE);
                    ((Button) view).setBackgroundColor(Color.GREEN);
                    expected_str = "";
                    if(is_wrong) {
                        is_wrong = false;
                        return;
                    }
                    lbl_char.setText("");
                    score = score + 1;
                    if (score > i_high_score) {
                        i_high_score = score;
                        sharedPref.edit().putInt("high_score", i_high_score).commit();
                        lbl_highscore.setText("High score: " + Integer.toString(i_high_score));
                    }
                    lbl_score.setText("Score: " + Integer.toString(score));
                } else {
                    is_wrong = true;
                    score = 0; //Hard game
                    lbl_score.setText("Score: " + Integer.toString(score));
                    ((Button) view).setTextColor(Color.WHITE);
                    ((Button) view).setBackgroundColor(Color.RED);
                    playwrong();
                    //t1.speak(actual.split(" ")[0] + ". wrong answer", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        }
        else
        {
            actual = actual.split(" ")[0];
            lbl_char.setText(actual);
            //t1.speak(actual, TextToSpeech.QUEUE_FLUSH, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t1.speak(actual,TextToSpeech.QUEUE_FLUSH,null,null);
            } else {
                t1.speak(actual, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        t1=null;
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            init_tts();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }


}