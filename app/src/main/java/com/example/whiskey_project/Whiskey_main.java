package com.example.whiskey_project;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.SeekBar;
import android.widget.TextView;


public class Whiskey_main extends Activity {

    private Whiskey_timer whiskeyTimer;
    private SeekBar seekBar;
    private TextView counterText, groupText, sessionText, trialText;
    private Handler handler = new Handler();
    int counterTemp = 0;
    int trialNum = 1;
    long[] trialTimes = {0, 0, 0, 0};

    long trialTimeAvg = 0;
    private boolean isVolumeDownPressed;
    private boolean isVolumeUpPressed;
    private boolean firstKeyEvent;
    int trialLoopValue = 0; // Shared Loop value

    // parameters from Setup dialog
    String participantCode, sessionCode, groupCode, volSide, hand;

    // numbers that users will have to get in the trials
    int[] resultNum = {23, 94, 36, 19};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initialize();

        Log.d("groupCode", "groupCode: " + groupCode);

        /*
        Different group number will appear depending on which
        one was selected in setup.
        Practice groups will have different result numbers.
         */
        if (groupCode.equals("G01")) {
            groupText.setText("Group 1");
        } else {
            groupText.setText("Group 2");
        }

        if (groupCode.equals("G02")) {
            groupText.setText("Group 2");
        }
        if (sessionCode.equals("S01")){
            sessionText.setText("Session 1: Seekbar Practice");
            resultNum[0] = 85;
            resultNum[1] = 12;
            resultNum[2] = 61;
            resultNum[3] = 43;
        } else if (sessionCode.equals("S02")){
            sessionText.setText("Session 2: Seekbar");
        } else if (sessionCode.equals(("S03"))) {
            seekBar.setVisibility(seekBar.INVISIBLE);
            sessionText.setText("Session 3: Volume Practice");
            resultNum[0] = 85;
            resultNum[1] = 12;
            resultNum[2] = 61;
            resultNum[3] = 43;
        } else {
            seekBar.setVisibility(seekBar.INVISIBLE);
            sessionText.setText("Session 4: Volume");
        }

        Log.d("resultNum", "resultNum: " + resultNum[0] + ", " + resultNum[1] + ", " + resultNum[2] + ", " + resultNum[3]);

        trialText.setText("Trial " + trialNum + ": Get the number " + resultNum[0]);

    }

    private void initialize() {
        // get parameters from setup
        Bundle b = getIntent().getExtras();
        participantCode = b.getString("participantCode");
        sessionCode = b.getString("sessionCode");
        volSide = b.getString("volSide");
        groupCode = b.getString("groupCode");
        hand = b.getString("hand");

        counterText = (TextView) findViewById(R.id.counterText);
        groupText = (TextView) findViewById(R.id.groupNum);
        sessionText = (TextView) findViewById(R.id.sessionNum);
        trialText = (TextView) findViewById(R.id.trialNum);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        // setup seekBar listener
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        whiskeyTimer = new Whiskey_timer();
        firstKeyEvent = true;
    }

    // SeekBar/Slider function
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // Update the TextView with the current value of the SeekBar
            counterText.setText(progress + "%");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (firstKeyEvent == true) {
                firstKeyEvent = false;
                whiskeyTimer.start();
            }

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d("Seek", "Seekbar Progress: " + seekBar.getProgress());

            handler.postDelayed(() -> {
                if (seekBar.getProgress() == resultNum[trialLoopValue]) {
                    if (trialLoopValue < 3) {
                        trialNum++;
                        trialText.setText("Trial " + trialNum + ": Get the number " + resultNum[trialLoopValue + 1]);
                    }
                    whiskeyTimer.stop();
                    Log.d("Result", "Success!");

                    firstKeyEvent = true; // Set true for next trial
                    trialTimes[trialLoopValue] = whiskeyTimer.elapsedTime();
                    Log.d("TimerOutput", "Elapsed time: " + whiskeyTimer.elapsedTime() + " milliseconds");
                    trialTimes[trialLoopValue] = whiskeyTimer.elapsedTime();
                    trialLoopValue++;
                    seekBar.setProgress(0);
                    if (trialLoopValue == resultNum.length) {
                        sendToReport();
                    }
                }
            }, 500);
        }
    };

    // Volume Key function
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int action = e.getAction();
        int keyCode = e.getKeyCode();
        if (sessionCode.equals(("S01")) || sessionCode.equals(("S02"))) {
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    if (firstKeyEvent == true) {
                        firstKeyEvent = false;
                        whiskeyTimer.start();
                    }

                    if (counterTemp > 0) {
                        counterTemp--;
                        isVolumeDownPressed = true;
                        counterText.setText(counterTemp + "%");
                        handler.postDelayed(counterUpdater, 100); // Initial delay before acceleration starts
                    }
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    if (firstKeyEvent == true) {
                        firstKeyEvent = false;
                        whiskeyTimer.start();
                    }

                    if (counterTemp < 100) {
                        counterTemp++;
                        isVolumeUpPressed = true;
                        counterText.setText(counterTemp + "%");
                        handler.postDelayed(counterUpdater, 100); // Initial delay before acceleration starts
                    }
                    return true;
                }
                break;
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {


        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.d("Result", "KEY_VOLUME_DOWN");
            // Stop the acceleration when the button is released
            isVolumeDownPressed = false;
            handler.removeCallbacks(counterUpdater);
            // Post a delayed callback to check after 500ms
            handler.postDelayed(() -> {
                if (trialLoopValue < resultNum.length && counterTemp == resultNum[trialLoopValue]) { //Replace second counterTemp with actual test value
                    if (trialLoopValue < 3) {
                        trialNum++;
                        trialText.setText("Trial " + trialNum + ": Get the number " + resultNum[trialLoopValue + 1]);
                    }
                    whiskeyTimer.stop();
                    Log.d("Result", "Success!");
                    Log.d("TimerOutput", "Elapsed time: " + whiskeyTimer.elapsedTime() + " milliseconds");
                    trialTimes[trialLoopValue] = whiskeyTimer.elapsedTime();
                    firstKeyEvent = true; // Set true for next trial
                    trialLoopValue++;
                    counterTemp = 0;
                    counterText.setText(0 + "%");
                    if (trialLoopValue == resultNum.length) {
                        sendToReport();
                    }
                }

            }, 500);
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("Result", "KEY_VOLUME_UP");
            // Stop the acceleration when the button is released
            isVolumeUpPressed = false;
            handler.removeCallbacks(counterUpdater);
            // Post a delayed callback to check after 800ms
            handler.postDelayed(() -> {
                if (trialLoopValue < resultNum.length && counterTemp == resultNum[trialLoopValue]) { //Replace second counterTemp with actual test value
                    if (trialLoopValue < 3) {
                        trialNum++;
                        trialText.setText("Trial " + trialNum + ": Get the number " + resultNum[trialLoopValue + 1]);
                    }
                    whiskeyTimer.stop();
                    Log.d("Result", "Success!");
                    Log.d("TimerOutput", "Elapsed time: " + whiskeyTimer.elapsedTime() + " milliseconds");
                    trialTimes[trialLoopValue] = whiskeyTimer.elapsedTime();
                    firstKeyEvent = true; // Set true for next trial
                    trialLoopValue++;
                    counterTemp = 0;
                    counterText.setText(0 + "%");
                    if (trialLoopValue == resultNum.length) {
                        sendToReport();
                    }
                }

            }, 500);

        }
        return super.onKeyUp(keyCode, event);
    }

    private Runnable counterUpdater = new Runnable() {
        @Override
        public void run() {
            if (isVolumeDownPressed && counterTemp > 0) {
                counterTemp--;
                counterText.setText(counterTemp + "%");
                // Adjust the delay to control the acceleration speed
                handler.postAtTime(this, SystemClock.uptimeMillis() + 100);
            }

            if (isVolumeUpPressed && counterTemp < 100) {
                counterTemp++;
                counterText.setText(counterTemp + "%");
                // Adjust the delay to control the acceleration speed
                handler.postAtTime(this, SystemClock.uptimeMillis() + 100);
            }
        }
    };

    public void sendToReport() {
        Log.d("Result", "Sending to Whiskey_report");
        trialTimeAvg = (trialTimes[0] + trialTimes[1] + trialTimes[2] + trialTimes[3]) / 4;
        Bundle b2 = new Bundle();
        b2.putString("participantCode", participantCode);
        b2.putString("sessionCode", sessionCode);
        b2.putString("groupCode", groupCode);
        b2.putString("volSide", volSide);
        b2.putString("hand", hand);
        b2.putLongArray("trialTimes", trialTimes);
        b2.putLong("trialTimeAvg", trialTimeAvg);

        Intent i = new Intent(Whiskey_main.this, Whiskey_report.class);
        i.putExtras(b2);
        startActivity(i);
    }


}